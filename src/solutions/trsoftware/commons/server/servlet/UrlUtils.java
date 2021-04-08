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

import com.google.common.collect.ImmutableMultimap;
import solutions.trsoftware.commons.client.bridge.util.URIComponentEncoder;
import solutions.trsoftware.commons.shared.util.ArrayUtils;
import solutions.trsoftware.commons.shared.util.MapUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Basic utils for working with URL strings.
 *
 * <p style="color: #6495ed; font-weight: bold;">
 *   TODO: some of this something like {@link com.google.gwt.http.client.UrlBuilder} or use the
 *   <a href="https://github.com/square/okhttp/blob/master/okhttp/src/main/java/okhttp3/HttpUrl.java">HttpUrl</a>
 *   class from the <a href="https://github.com/square/okhttp">OkHttp project</a>
 *   (see <a href="https://medium.com/square-corner-blog/okhttps-new-url-class-515460eea661">Square Engineering blog</a>)
 * </p>
 *
 * @author Alex
 * @since 11/14/2017
 */
public class UrlUtils {

  /**
   * Generates a string like {@code "key1=value1&key2=value2..."}. Will URL-encode all the names and values.
   * @param paramMap will be used to construct the query string; not modified by this method
   */
  public static String urlQueryString(Map<String, String> paramMap) {
    StringBuilder ret = new StringBuilder();
    int i = 0;
    for (String name : paramMap.keySet()) {
      if (i++ > 0)
        ret.append('&');
      ret.append(urlEncode(name)).append('=').append(urlEncode(paramMap.get(name)));
    }
    return ret.toString();
  }

  /**
   * Generates a string like {@code "key1=value1&key2=value2..."}. Will URL-encode all the names and values.
   * @param queryParams array like {@code ["param_i", "value_i", ...]}
   */
  public static String urlQueryString(String... queryParams) {
    return urlQueryString(MapUtils.stringLinkedHashMap(queryParams));
  }

  /**
   * Extracts the query string parameters from the given URL.
   *
   * @return the name-value mapping of the query parameters contained in the given URL
   *
   * @see MapUtils#asMap(com.google.common.collect.Multimap)
   */
  public static ImmutableMultimap<String, String> parseQueryString(URL url) {
    ImmutableMultimap.Builder<String, String> mapBuilder = ImmutableMultimap.builder();
    String query = url.getQuery();
    if (StringUtils.notEmpty(query)) {
      String[] nameValuePairs = query.split("&");
      for (String nvp : nameValuePairs) {
        int iEq = nvp.indexOf('=');
        String name, value;
        if (iEq != -1) {
          name = nvp.substring(0, iEq);
          value = nvp.substring(iEq+1);
        }
        else {
          // only name, no value (unusual but possible; see https://stackoverflow.com/q/4557387)
          name = nvp;
          value = "";
        }
        mapBuilder.put(urlDecode(name), urlDecode(value));
      }
    }
    return mapBuilder.build();
  }

  /**
   * Rewrites the path and query string of the given URL.
   * <p style="color: #6495ed; font-weight: bold;">
   *   TODO: write something like {@link com.google.gwt.http.client.UrlBuilder} or use the
   *   <a href="https://github.com/square/okhttp/blob/master/okhttp/src/main/java/okhttp3/HttpUrl.java">HttpUrl</a>
   *   class from the <a href="https://github.com/square/okhttp">OkHttp project</a>
   *   (see <a href="https://medium.com/square-corner-blog/okhttps-new-url-class-515460eea661">Square Engineering blog</a>)
   * </p>
   * @param url the URL to rewrite
   * @param newPath the new path
   * @param queryParams name-value array for the new query params, given as {@code ["param_i", "value_i", ...]}
   * @return A URL string with the same protocol, host, and port as the given {@code url}, with the path and query string
   * constructed from the given args.
   */
  public static String rewrite(URL url, String newPath, String... queryParams) {
    StringBuilder ret = new StringBuilder();
    ret.append(url.getProtocol()).append("://").append(url.getHost());
    int port = url.getPort();
    if (port >= 0)
      ret.append(':').append(port);
    ret.append(newPath);
    if (!ArrayUtils.isEmpty(queryParams)) {
      ret.append('?');
      ret.append(urlQueryString(queryParams));
    }
    return ret.toString();
  }

  /** Replaces a name=value pair in the given URL query string with a new one */
  public static String replaceQueryStringParameter(String queryString, String originalParamName, String originalValue, String newParamName, String newValue) {
    return queryString.replaceFirst(originalParamName + "=" + originalValue, newParamName + "=" + newValue);
  }

  /** Replaces a parameter value in the given URL query string with a new value */
  public static String replaceQueryStringParameter(String queryString, String paramName, String originalValue, String newValue) {
    return replaceQueryStringParameter(queryString, paramName, originalValue, paramName, newValue);
  }

  /** Decodes the given percent-encoded string using {@link java.net.URLDecoder} (UTF-8 encoding is assumed). */
  public static String urlDecode(String str) {
    try {
      return URLDecoder.decode(str, StringUtils.UTF8_CHARSET_NAME);
    }
    catch (UnsupportedEncodingException e) {
      // will never happen - all Java VMs support UTF-8
      throw new RuntimeException(e);
    }
    catch (IllegalArgumentException e) {
      return str; // return the original string if the decoding fails
    }
  }

  /**
   * Percent-encodes the given string using {@link java.net.URLEncoder} (UTF-8 encoding is used).
   * <p>
   * <strong>Warning:</strong> the escaping strategy implemented by {@link java.net.URLEncoder} does not
   * match the Javascript {@code encodeURIComponent} function and therefore the result is not guaranteed to be
   * compatible with the Javascript {@code decodeURIComponent} function.
   * In particular, this method should not be used for encoding cookie values that might be read client-side with
   * {@link com.google.gwt.user.client.Cookies} (which uses the native JS {@code decodeURIComponent} function).
   * To match the Javascript behavior, use {@link URIComponentEncoder#encode(String)} instead.
   *
   * @see URIComponentEncoder#encode(String)
   */
  public static String urlEncode(String str) {
    try {
      return URLEncoder.encode(str, StringUtils.UTF8_CHARSET_NAME);
    }
    catch (UnsupportedEncodingException e) {
      // will never happen - all Java VMs support UTF-8
      throw new RuntimeException(e);
    }
  }
}
