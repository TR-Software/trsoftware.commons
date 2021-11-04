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

package solutions.trsoftware.commons.server.servlet;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterators;
import solutions.trsoftware.commons.client.util.WebUtils;
import solutions.trsoftware.commons.server.servlet.listeners.HttpSessionMutexListener;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author Alex
 * @since Jul 20, 2007
 */
public abstract class ServletUtils {

  private static final ThreadLocal<RequestCopy> threadLocalRequestCopy = new ThreadLocal<>();

  public static final String USER_AGENT_HEADER = "User-Agent";

  /**
   * De-facto standard header for identifying the originating IP address of a client connecting to a web server
   * through an HTTP proxy or a load balancer.
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-For">MDN Reference</a>
   * @see <a href="https://en.wikipedia.org/wiki/X-Forwarded-For">Wikipedia</a>
   */
  public static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";

  /**
   * {@link #getRequestURL(HttpServletRequest)} will save the parsed {@link URL} in the request under this attribute
   * name.
   */
  public static final String PARSED_URL_ATTR = ServletUtils.class.getName() + ".requestURL";

  /**
   * {@link #getSessionMutex(HttpSession)} will use this session attribute to save
   * a mutex object that can be used for synchronizing access to a session.
   */
  public static final String SESSION_MUTEX_ATTRIBUTE = ServletUtils.class.getName() + ".SESSION_MUTEX";


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
   *     doesn't have this header.
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
   * Sets a P3P "compact policy" header with TypeRacer's compact privacy policy description.
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
   * @throws IllegalArgumentException if one of the {@code String[]} value arrays in the given map contains more than
   *                                  one element.
   */
  public static SortedMap<String, String> getRequestParametersAsSortedStringMap(Map<String, String[]> requestParamMap) {
    SortedMap<String, String> singleValueMap = new TreeMap<>();
    for (Map.Entry<String, String[]> entry : requestParamMap.entrySet()) {
      String[] valueArr = entry.getValue();
      if (valueArr.length > 1)
        throw new IllegalArgumentException("" + entry.getKey() + ": " + Arrays.toString(valueArr) + " contains more than one value.");
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
   *                                  contains more than one element.
   */
  public static SortedMap<String, String> getRequestParametersAsSortedStringMap(HttpServletRequest request) {
    return getRequestParametersAsSortedStringMap(request.getParameterMap());
  }

  /**
   * Retrieves a selected subset of request parameters.
   *
   * @param names the names of the parameters to return
   * @return the name-value mapping for the selected parameters, in the same order they appear in the {@code names} arg.
   *     <b>Note:</b> if the request contains multiple values for any of these parameters, will include only the first
   *     value.
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
   * Creates a multimap of the headers contained in the given request.  All header names will be converted to lowercase,
   * to facilitate case-insensitive lookup.
   *
   * @return a (possibly-empty) immutable multimap containing all the headers and their values
   */
  public static ImmutableListMultimap<String, String> getRequestHeadersAsMultimap(HttpServletRequest request) {
    ImmutableListMultimap.Builder<String, String> builder = ImmutableListMultimap.builder();
    Enumeration<String> headerNames = request.getHeaderNames();
    if (headerNames != null) {
      while (headerNames.hasMoreElements()) {
        String name = headerNames.nextElement();
        Enumeration<String> values = request.getHeaders(name);
        builder.putAll(name.toLowerCase(), () -> Iterators.forEnumeration(values));
      }
    }
    return builder.build();
  }

  /**
   * @return the first path element of the requested URL
   *     (e.g. http://special.typeracer.com/foo/gameserv => "foo")
   *     or null if the path doesn't contain at least one path element
   *     (e.g. http://special.typeracer.com/gameserv or http://special.typeracer.com)
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
   *     (e.g. http://special.typeracer.com/foo/gameserv => ["foo", "gameserv"]
   *     or null if the path doesn't contain at least one path element
   *     (e.g. http://special.typeracer.com)
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
   * Parses the URL string given by {@link HttpServletRequest#getRequestURL()} into a {@link URL} object,
   * and caches the result as a request attribute for future reference.  If a cached instance is already available,
   * will return that instead of parsing the URL again.
   * <p>
   * NOTE: this is the same as calling {@link #getRequestURL(HttpServletRequest, boolean)} with {@code cache = true}.
   *
   * @return an instance of {@link URL}, derived from invoking {@link HttpServletRequest#getRequestURL()} on the given
   *     {@code request}
   * @see #PARSED_URL_ATTR
   */
  public static URL getRequestURL(HttpServletRequest request) {
    return getRequestURL(request, true);
  }

  /**
   * Parses the URL string given by {@link HttpServletRequest#getRequestURL()} into a {@link URL} object,
   * and optionally saves the result as a request attribute for future reference (as a performance optimization,
   * to avoid the overhead of parsing it again later in the request lifecycle).
   * <p>
   * Some simple benchmarking shows that the cached version is about 10x faster (see {@code
   * ServletUtilsTest.testGetRequestURLBenchmark()})
   *
   * @param cache if {@code true}, will save the result into a request attribute
   *     (as a performance optimization, to avoid the overhead of parsing it multiple times);
   *     otherwise will neither cache the result in a request attribute, nor attempt to use a cached result even if
   *     it might be available (this might be slightly faster and save a bit of memory if a parsed {@link URL}
   *     is needed only once throughout the request lifecycle)
   * @return an instance of {@link URL}, derived from invoking {@link HttpServletRequest#getRequestURL()} on the given
   *     {@code request}
   * @see #PARSED_URL_ATTR
   */
  public static URL getRequestURL(HttpServletRequest request, boolean cache) {
    if (cache) {
      /*
        perf optimization: we save the parsed URL object in a request attribute for future reference
        (to avoid the overhead of parsing it multiple times)
      */
      Object parsedURL = request.getAttribute(PARSED_URL_ATTR);
      if (parsedURL != null) {
        if (parsedURL instanceof URL)
          return (URL)parsedURL;
        else if (parsedURL instanceof Throwable) {
          // must have got a MalformedURLException last time we tried to parse it (which is very unlikely)
          throw new RuntimeException((Throwable)parsedURL);
        }
      }
    }

    try {
      URL url = new URL(request.getRequestURL().toString());
      if (cache) {
        // save the parsed URL in the request, to avoid the parsing overhead next time it's needed
        request.setAttribute(PARSED_URL_ATTR, url);
      }
      return url;
    }
    catch (MalformedURLException e) {
      // this should never happen, because the request presumably contains a valid URL (otherwise it wouldn't have made it here)
      if (cache) {
        request.setAttribute(PARSED_URL_ATTR, e);
      }
      throw new RuntimeException(e);
    }
  }

  /**
   * @return an instance of {@link URL}, derived from invoking {@link HttpServletRequest#getRequestURL()} on
   *     {@link #threadLocalRequestCopy}
   * @throws IllegalStateException if {@link #setThreadLocalRequestCopy(RequestCopy)} hasn't
   *                               been invoked by the thread handling the current request.
   */
  public static URL getThreadLocalRequestURL() throws IllegalStateException {
    RequestCopy threadLocalRequest = threadLocalRequestCopy.get();
    if (threadLocalRequest == null)
      throw new IllegalStateException("thread-local RequestCopy not available");
    return getRequestURL(threadLocalRequest);
  }

  /** @return the requested URL minus the path (e.g. http://example.com/foo -> http://example.com) */
  public static StringBuffer getBaseURL(HttpServletRequest request) {
    /* TODO(11/2/2021): instead of string parsing, might be safer to use the URL object returned by
         ServletUtils.getRequestURL(HttpServletRequest); the performance cost should be negligible
         since getRequestURL caches the URL object
     */
    StringBuffer url = request.getRequestURL();
    // NOTE: we don't simply strip off the query string because that creates a discrepancy between different versions of tomcat (see https://issues.apache.org/bugzilla/show_bug.cgi?id=28222 )
    // instead, we just strip off everything after the 3rd slash in the URL (which seems to work for all imaginable types of http urls)
    int indexOfSlashBeforePath = url.indexOf("/", url.indexOf("//") + 2);
    url.delete(indexOfSlashBeforePath, url.length());
    return url;
  }

  /**
   * Should be used instead of {@link HttpServletRequest#getRemoteAddr()} to get the originating client IP address,
   * taking into account the possibility that the client request is being forwarded by a proxy or load balancer.
   * <p>
   * This is particularly important when Cloudflare is being used as a frontend for the web app, in which case
   * {@link HttpServletRequest#getRemoteAddr()} will always return the IP address of a Cloudflare proxy.
   *
   * @return the client IP address extracted from the {@value #X_FORWARDED_FOR_HEADER} header, if present, otherwise
   *     {@link HttpServletRequest#getRemoteAddr()}
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-For">X-Forwarded-For Header</a>
   * @see <a href="https://support.cloudflare.com/hc/en-us/articles/200170986-How-does-Cloudflare-handle-HTTP-Request-headers-">
   *     How does Cloudflare handle HTTP Request headers?</a>
   */
  public static String getClientIpAddress(HttpServletRequest request) {
    String xff = request.getHeader(X_FORWARDED_FOR_HEADER); // comma-separated list of IP addresses (see https://en.wikipedia.org/wiki/X-Forwarded-For#Format)
    if (StringUtils.notBlank(xff)) {
      List<String> ipList = StringUtils.splitAndTrim(xff, ",");
      // the first address in this list is the originating client IP
      if (!ipList.isEmpty()) {
        return ipList.get(0);
      }
    }
    return request.getRemoteAddr();
    /*
    TODO: should we add support for the other variants of this header, like the standards-compliant "Forwarded", "Via",
      "CF-Connecting-IP", etc.?
      @see https://en.wikipedia.org/wiki/X-Forwarded-For#Alternatives_and_variations
           https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Via
           https://support.cloudflare.com/hc/en-us/articles/200170986-How-does-Cloudflare-handle-HTTP-Request-headers-
    */
  }

  /**
   * Sets multiple parameters with the same name prefix.
   * Example: given ("x", "a", 25, "foo"),
   * will add entries ("x0", "a"), ("x1", 25), and ("x2", "foo") to the given map.
   *
   * @return the same map, for method chaining.
   */
  public static <T extends Map<String, String>> T addIndexedMultivaluedParams(T map, String namePrefix, Object... values) {
    for (int i = 0; i < values.length; i++) {
      map.put(namePrefix + i, String.valueOf(values[i]));
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
   *
   * @return a list of the parameter values, or an empty list if no parameters are present
   *     in the request.
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

  /**
   * Generates a string with info about the given GWT-RPC request for logging purposes.
   *
   * @return info about the given GWT-RPC request for logging purposes.
   */
  public static String printGwtRequestInfo(HttpServletRequest request, String permutationStrongName) {
    return appendGwtRequestInfo(new StringBuilder(), request, permutationStrongName).toString();
  }

  /**
   * Appends info about the given GWT-RPC request to the given {@link StringBuilder} for logging purposes.
   *
   * @return the given {@link StringBuilder} after appending the request info to it
   */
  public static StringBuilder appendGwtRequestInfo(StringBuilder str, HttpServletRequest request, String permutationStrongName) {
    str.append("permutation ").append(permutationStrongName)
        .append(" from ").append(getClientIpAddress(request))
        .append(" [").append(getUserAgentHeader(request)).append("]");
    return str;
  }

  /**
   * Returns the best available mutex for the given session:
   * that is, an object to synchronize on for the given session.
   * <p>
   * Returns the session mutex attribute if available; usually,
   * this means that the {@link HttpSessionMutexListener} needs to be defined
   * in {@code web.xml}. Falls back to the {@link HttpSession} itself
   * if no mutex attribute found.
   * <p>
   * The session mutex is guaranteed to be the same object during
   * the entire lifetime of the session, available under the key defined
   * by the {@link #SESSION_MUTEX_ATTRIBUTE} constant. It serves as a
   * safe reference to synchronize on for locking on the current session.
   * <p>
   * In many cases, the {@link HttpSession} reference itself is a safe mutex
   * as well, since it will always be the same object reference for the
   * same active logical session. However, this is not guaranteed across
   * different servlet containers; the only 100% safe way is a session mutex.
   * For example, when running Tomcat with persistent or replicated sessions,
   * the {@link HttpSession} could be a different object deserialized from the datastore.
   *
   * @param session the HttpSession to find a mutex for
   * @return the mutex object (never {@code null})
   * @see #SESSION_MUTEX_ATTRIBUTE
   * @see HttpSessionMutexListener
   */
  // NOTE: this code borrowed from Spring (https://github.com/spring-projects/spring-framework/blob/9be327985b61588d5f8c7050a5558ef36a33b321/spring-web/src/main/java/org/springframework/web/util/WebUtils.java#L417-L444)
  public static Object getSessionMutex(HttpSession session) {
    Objects.requireNonNull(session, "Session must not be null");
    Object mutex = session.getAttribute(SESSION_MUTEX_ATTRIBUTE);
    if (mutex == null) {
      mutex = session;
    }
    return mutex;
  }

}
