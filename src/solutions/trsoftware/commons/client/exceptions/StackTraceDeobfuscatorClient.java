/*
 * Copyright 2021 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.commons.client.exceptions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import solutions.trsoftware.commons.shared.cache.FixedSizeLruCache;
import solutions.trsoftware.commons.shared.util.LogicUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * Encapsulates the logic for contacting the server to deobfuscate a Javascript stack trace from compiled GWT code.
 * <p>
 * Intended to be instantiated with {@link GWT#create(Class)}, so that inheriting modules can provide a different
 * implementation via deferred binding.
 *
 * @author Alex
 * @since Mar 30, 2013
 */
public class StackTraceDeobfuscatorClient {
  /*
  NOTE: sometimes infinite recursion occurs, making the stack traces grow uncontrollably, with no useful info added
  with each iteration (just an extra "Unknown.anonymous(Impl.java:70)" line at the bottom)
     Example:
       com.google.gwt.xhr.client.XMLHttpRequest.$clearOnReadyStateChange(XMLHttpRequest.java:164)
       com.google.gwt.http.client.RequestBuilder$1.onReadyStateChange(RequestBuilder.java:394)
       Unknown.anonymous(XMLHttpRequest.java:351)
       com.google.gwt.core.client.impl.Impl.apply(Impl.java:189)
       com.google.gwt.core.client.impl.Impl.entry0(Impl.java:242)
       Unknown.anonymous(Impl.java:70)
       [last line repeats with each iteration of the infinite recursion]
    (see doc/postmortems/2015_07_27_Tomcat_high_CPU_usage/2015_07_27_Tomcat_high_CPU_usage.txt)

  We take the following steps to mitigate the problem:
    - truncate the stack trace here before sending it to the server for deobfuscation (this will save bandwidth and CPU time required to parse the request into an array object)
    - include the exception's error message (so it can be printed in the server log to help us debug the culprit)
    - cache the last stack trace that was sent, and if the next one is the same but longer, ignore it silently if it occurs soon after the previous one
  */

  public abstract static class Callback {

    /**
     * Invoked as soon as we have the de-obfuscated stack trace for the given exception.
     *
     * @param ex The original exception whose stack trace is being de-obfuscated.
     * @param stackTrace The de-obfuscated stack trace for {@code ex} (i.e. the result of
     * {@link StackTraceDeobfuscatorService#deobfuscateStackTrace(StackTraceElement[], String, String)})
     */
    public abstract void onDeobfuscationResultAvailable(Throwable ex, String stackTrace);

    /**
     * Invoked if the RPC call to {@link StackTraceDeobfuscatorService} produced an exception.
     * @param caught The exception produced by the RPC call to {@link StackTraceDeobfuscatorService}
     * @see AsyncCallback#onFailure(Throwable)
     */
    public void onDeobfuscationFailure(Throwable caught) {
      GWT.log("Stack trace deobfuscation failed", caught);
    }
  }

  protected StackTraceDeobfuscatorServiceAsync service;
  protected int stackTraceSizeLimit;

  private static final String RESPONSE_PENDING = "__RP__";  // a cache entry will have this value until a response is received

  private FixedSizeLruCache<List<String>, String> responseCache
      = new FixedSizeLruCache<List<String>, String>(10);

  public StackTraceDeobfuscatorClient(String servletUrl, int stackTraceSizeLimit) {
    init(servletUrl, stackTraceSizeLimit);
  }

  public StackTraceDeobfuscatorClient(String servletUrl) {
    this(servletUrl, Integer.MAX_VALUE);
  }

  /**
   * Default constructor to be used with {@link GWT#create(Class)}.
   * <p>
   * <strong>Note:</strong> must call {@link #init(String, int)} afterwards
   */
  protected StackTraceDeobfuscatorClient() {
  }

  /**
   * Subclass may override to customize the async proxy ({@link ServiceDefTarget} object)
   *
   * @param servletUrl will be passed to {@link ServiceDefTarget#setServiceEntryPoint(String)}
   * @param stackTraceSizeLimit will truncate stack traces longer than this (to save bandwidth; can pass {@link
   *     Integer#MAX_VALUE} to avoid truncation
   */
  protected void init(String servletUrl, int stackTraceSizeLimit) {
    if (servletUrl == null)
      throw new NullPointerException("StackTraceDeobfuscatorServlet URL cannot be null");
    service = GWT.create(StackTraceDeobfuscatorService.class);
    ((ServiceDefTarget)service).setServiceEntryPoint(servletUrl);
    this.stackTraceSizeLimit = stackTraceSizeLimit;
  }

  /**
   * @throws IllegalStateException if {@link #service} not initialized (which would happen if this instance was created
   * via {@link GWT#create(Class)} and the {@link #init(String, int)} method wasn't called afterwards).
   */
  protected void assertInitialized() {
    if (service == null || stackTraceSizeLimit == 0)
      throw new IllegalStateException(getClass().getName() + " not initialized. Did you forget to call init(String, int)?");
  }

  public void deobfuscateStackTrace(final Throwable ex, final Callback callback) {
    assertInitialized();
    List<StackTraceElement> stackTrace = Arrays.asList(ex.getStackTrace());
    if (stackTrace.isEmpty()) {
      // this might happen if the exception originated outside GWT code
      callback.onDeobfuscationResultAvailable(ex, "");
      return;
    }
    // 1) clean up the stack trace to create a compact canonical representation (that can be used as a cache key)
    // 1.a) truncate the stack trace on the client side to save bandwidth (also have the client not send the stack trace if it's the same as the last one, but simply has one extra element at the end)
    if (stackTrace.size() > stackTraceSizeLimit) {
      stackTrace = stackTrace.subList(0, stackTraceSizeLimit);
    }
    // 1.b) remove repeating duplicate entries at the bottom of the stack (see the comment at the top of this class for explanation)
    StackTraceElement lastElement = stackTrace.get(stackTrace.size() - 1);
    ListIterator<StackTraceElement> stackIter = stackTrace.listIterator(stackTrace.size() - 1); // start iteration at the end of the list
    while (stackIter.hasPrevious()) {
      StackTraceElement elt = stackIter.previous();
      if (LogicUtils.eq(elt, lastElement)) {
        // remove this element, since it matches the last one
        stackIter.remove();
      }
      else
        break;  // reached a non-duplicate element
    }
    final List<String> cacheKey = getCacheKey(ex);
    // 2) check the cache for this stack trace; make RPC call to server only if cache doesn't already contain a response
    String cachedValue = responseCache.get(cacheKey);
    if (cachedValue != null) {
      if (!cachedValue.equals(RESPONSE_PENDING)) {
        callback.onDeobfuscationResultAvailable(ex, cachedValue);
      }
    }
    else {
      responseCache.put(cacheKey, RESPONSE_PENDING);
      service.deobfuscateStackTrace(
          stackTrace.toArray(new StackTraceElement[stackTrace.size()]),
          ex.toString(),
          GWT.getModuleName(),
          new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
              callback.onDeobfuscationFailure(caught);
            }
            @Override
            public void onSuccess(String result) {
              responseCache.put(cacheKey, result);
              callback.onDeobfuscationResultAvailable(ex, result);
            }
          });
    }
  }

  /**
   * Compiled GWT code (in web mode) can't properly hash {@link StackTraceElement}s for some reason (invocations of
   * {@link StackTraceElement#hashCode()} return different values for the same entry in a different stack trace,
   * even if the two stack traces are logically identical)
   *
   * Therefore we use the {@link StackTraceElement#toString()} representations to ensure that we get the same has code
   * for a logically-equivalent stack trace.
   *
   * @return The stack trace of the given exception as a list of strings.
   */
  private List<String> getCacheKey(Throwable ex) {
    List<String> ret = new ArrayList<String>();
    for (StackTraceElement ste : ex.getStackTrace()) {
      ret.add(ste.toString());
    }
    return ret;
  }

}
