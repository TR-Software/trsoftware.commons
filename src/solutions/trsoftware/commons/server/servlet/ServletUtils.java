/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.server.servlet;

import solutions.trsoftware.commons.client.util.WebUtils;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Date: Jul 20, 2007
 * Time: 7:12:40 PM
 *
 * @author Alex
 */
public abstract class ServletUtils {

  private static final ThreadLocal<RequestCopy> threadLocalRequestCopy = new ThreadLocal<>();
  public static final String USER_AGENT_HEADER = "User-Agent";

  public static void setThreadLocalRequestCopy(RequestCopy request) {
    if (request == null)
      threadLocalRequestCopy.remove();
    else
      threadLocalRequestCopy.set(request);
  }

  public static RequestCopy getThreadLocalRequestCopy() {
    return threadLocalRequestCopy.get();
  }

  /** Adds response headers that tell the browser not to cache this response */
  public static void disableResponseBrowserCaching(HttpServletRequest request, HttpServletResponse response) {
    response.setHeader("Pragma", "no-cache"); //HTTP 1.0
    // IE6 has a bug that prevents using "no-cache" in Cache-Control when the response is gzipped
    // see http://support.microsoft.com/kb/321722 and http://www.akmattie.net/blog/2007/11/10/javascript-frames-ie-6-gzip-no-cache-header-trouble/
    if (ServletUtils.isIE6(request))
      response.setHeader("Cache-Control", "max-age=0, no-store, must-revalidate, no-transform"); //HTTP 1.1
    else
      response.setHeader("Cache-Control", "no-cache, max-age=0, no-store, must-revalidate, no-transform"); //HTTP 1.1
    // NOTE: the no-transform directive was added for reasons described in http://stackoverflow.com/questions/4113268/how-to-stop-javascript-injection-from-vodafone-proxy
    response.setDateHeader("Expires", 0); //prevents caching at the proxy server
  }

  private static boolean findStringInUserAgentHeader(HttpServletRequest request, String str) {
    String userAgentString = getUserAgentHeader(request);
    return userAgentString != null && userAgentString.toLowerCase().contains(str);
  }

  /**
   * @return The value of the request's {@value #USER_AGENT_HEADER} header, or {@code null} if the request
   * doesn't have this header.
   */
  public static String getUserAgentHeader(HttpServletRequest request) {
    return request.getHeader(USER_AGENT_HEADER);
  }

  public static boolean isIE6(HttpServletRequest request) {
    return findStringInUserAgentHeader(request, "msie 6");
  }

  public static boolean isIE(HttpServletRequest request) {
    return findStringInUserAgentHeader(request, "msie");
  }

  public static boolean isFirefox(HttpServletRequest request) {
    return findStringInUserAgentHeader(request, "firefox");
  }

  /**
   * Sets a P3P "compact policy" header with TypeRacer's compacy privacy policy description.
   * This is required by IE browsers to enable reading cross-domain iframe cookies.
   * See compact.txt and other files in the public/privacy dir.  They can be
   * edited using IBM's privacy policy editor, installed in C:/Programming/Tools/p3pGenerator
   * or downloaded from http://www.alphaworks.ibm.com/tech/p3peditor. 
   */
  public static void setP3PHeader(HttpServletResponse response) {
    response.setHeader("P3P", "policyref=\"/privacy/p3p.xml\"," +
        "CP=\"CAO DSP COR CURa ADMa DEVa TAIa OUR BUS IND PHY ONL UNI COM NAV DEM PRE LOC\"");
  }

  /**
   * The Servlet API represents {@link HttpServletRequest} parameters with a {@code Map<String, String[]>},
   * which is often redundant, because most of the time there is only one value for every parameter.
   *
   * @return The a name-value mapping of the parameters, sorted by name
   * @throws IllegalArgumentException if one of the {@code String[]} value arrays in the given map contains more than one element.
   */
  public static SortedMap<String, String> getRequestParametersAsSortedStringMap(Map<String, String[]> requestParamMap) {
    SortedMap<String, String> singleValueMap = new TreeMap<>();
    for (Map.Entry<String, String[]> entry : requestParamMap.entrySet()) {
      String[] valueArr = entry.getValue();
      if (valueArr.length > 1)
        throw new IllegalArgumentException(""+entry.getKey() + ": " + Arrays.toString(valueArr) + " contains more than one value.");
      else
        singleValueMap.put(entry.getKey(), valueArr[0]);
    }
    return singleValueMap;
  }

  /**
   * A more convenient version of {@link HttpServletRequest#getParameterMap()}, returning a {@code Map<String, String>}
   * instead of {@code Map<String, String[]>}.  This is more convenient because most of the time there is only
   * one value for any request parameter.
   * <p>
   * <strong>Warning:</strong> will throw {@link IllegalArgumentException} if the request actually does contain
   * multi-valued parameters.
   *
   * @return The a name-value mapping of the parameters, sorted by name
   * @throws IllegalArgumentException if one of the value arrays from {@link HttpServletRequest#getParameterMap()}
   * contains more than one element.
   */
  public static SortedMap<String, String> getRequestParametersAsSortedStringMap(HttpServletRequest request) {
    return getRequestParametersAsSortedStringMap(request.getParameterMap());
  }

  /**
   * Retrieves a selected subset of request parameters.
   *
   * @param names the names of the parameters to return
   * @return the name-value mapping for the selected parameters, in the same order they appear in the {@code names} arg.
   * <b>Note:</b> if the request contains multiple values for any of these parameters, will include only the first value.
   * @see WebUtils#getUrlParameterMap(Iterable)
   */
  @Nonnull
  public static LinkedHashMap<String, String> getRequestParameters(HttpServletRequest request, Iterable<String> names) {
    LinkedHashMap<String, String> ret = new LinkedHashMap<>();
    for (String name : names) {
      String value = request.getParameter(name);
      if (value != null)
        ret.put(name, value);
    }
    return ret;
  }

  /**
   * @return the first path element of the requested URL
   * (e.g. http://special.typeracer.com/foo/gameserv => "foo")
   * or null if the path doesn't contain at least one path element
   * (e.g. http://special.typeracer.com/gameserv or http://special.typeracer.com)
   */
  public static String extractFirstPathElement(HttpServletRequest request) {
    String uri = request.getRequestURI();
    // we use low-level string operations because they're faster than regex
    int slash1Pos = uri.indexOf('/');
    if (slash1Pos < 0)
      return null;
    int firstCharPos = slash1Pos + 1;
    int slash2Pos = uri.indexOf('/', firstCharPos);
    if (slash2Pos < 0)
      return null;
    return uri.substring(firstCharPos, slash2Pos);
  }

  /**
   * @return an array of all the path elements of the requested URL
   * (e.g. http://special.typeracer.com/foo/gameserv => ["foo", "gameserv"]
   * or null if the path doesn't contain at least one path element
   * (e.g. http://special.typeracer.com)
   */
  public static StringTokenizer extractAllPathElements(HttpServletRequest request) {
    return new StringTokenizer(request.getRequestURI(), "/", false);
  }

  /**
   * Sends a 301 (permanent) redirect to the specified URL.
   * By contrast request.sendRedirect sends a 302 (temporary) redirect. 
   */
  public static void sendPermanentRedirect(HttpServletResponse response, String newLocation) {
    //send HTTP 301 status code to browser
    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
    //send user to new location
    response.setHeader("Location", newLocation);
  }

  /** Generates a URL equivalent to the requested URL but with the given parameter replaced in the query string */
  public static String replaceQueryStringParameter(HttpServletRequest request, String originalParamName, String originalValue, String newParamName, String newValue) {
    return request.getRequestURL().append('?').append(UrlUtils.replaceQueryStringParameter(
        request.getQueryString(), originalParamName, originalValue, newParamName, newValue)).toString();
  }

  /**
   * @return an instance of {@link URL}, derived from invoking {@link HttpServletRequest#getRequestURL()} on the given
   * {@code request}
   */
  public static URL getRequestURL(HttpServletRequest request) {
    // TODO: as a perf optimization, could save the parsed URL object in a request attribute for future reference
    try {
      return new URL(request.getRequestURL().toString());
    }
    catch (MalformedURLException e) {
      // this should never happen, because the request presumably contains a valid URL string
      throw new RuntimeException(e);
    }
  }

  /**
   * @return an instance of {@link URL}, derived from invoking {@link HttpServletRequest#getRequestURL()} on
   * {@link #threadLocalRequestCopy}
   *
   * @throws IllegalStateException if {@link #setThreadLocalRequestCopy(RequestCopy)} hasn't
   * been invoked by the thread handling the current request.
   */
  public static URL getThreadLocalRequestURL() throws IllegalStateException {
    RequestCopy threadLocalRequest = threadLocalRequestCopy.get();
    if (threadLocalRequest == null)
      throw new IllegalStateException("thread-local RequestCopy not available");
    return getRequestURL(threadLocalRequest);
  }

  /** @return the requested URL minus the path (e.g. http://example.com/foo -> http://example.com) */
  public static StringBuffer getBaseUrl(HttpServletRequest request) {
    StringBuffer url = request.getRequestURL();
    // NOTE: we don't simply strip off the query string because that creates a discrepancy between different versions of tomcat (see https://issues.apache.org/bugzilla/show_bug.cgi?id=28222 )
    // instead, we just strip off everything after the 3rd slash in the URL (which seems to work for all imaginable types of http urls)
    int indexOfSlashBeforePath = url.indexOf("/", url.indexOf("//") + 2);
    url.delete(indexOfSlashBeforePath, url.length());
    return url;
  }

  /**
   * Sets multiple parameters with the same name prefix.
   * Example: given ("x", "a", 25, "foo"),
   * will add entries ("x0", "a"), ("x1", 25), and ("x2", "foo") to the given map.
   * @return the same map, for method chaining.
   */
  public static <T extends Map<String, String>> T addIndexedMultivaluedParams(T map, String namePrefix, Object... values) {
    for (int i = 0; i < values.length; i++) {
      map.put(namePrefix+i, String.valueOf(values[i]));
    }
    return map;
  }

  /**
   * Reverses the function of addIndexedMultivaluedParams.
   * Example: If the request contains parameters "x0"="a", "x1"="25" and "x2"="foo",
   * calling this method with args (request, "x") will return a list containing
   * the values {"a","25","foo"}.
   * NOTE: parameter names must start with 0 and there must not be any gaps in the
   * ordinal suffixes of the parameter names:
   * Exemple 2: given params x1=1 and x2=2, will return [] because there is no "x0"
   * Exemple 3: given params x0=0 and x2=2, will return ["0"] because there is no "x1"
   * @return a list of the parameter values, or an empty list if no parameters are present
   * in the request.
   */
  public static ArrayList<String> readIndexedMultivaluedParams(HttpServletRequest request, String paramNamePrefix) {
    ArrayList<String> ret = new ArrayList<>();
    for (int i = 0; i < Integer.MAX_VALUE; i++) {
      String value = request.getParameter(paramNamePrefix + i);
      if (value == null)
        break; // no more indexed parameters specified
      ret.add(value);
    }
    return ret;
  }
}
