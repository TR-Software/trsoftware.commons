/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.util;

import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import solutions.trsoftware.commons.client.bridge.util.UrlEncoder;

import java.util.*;

import static solutions.trsoftware.commons.client.util.StringUtils.join;
import static solutions.trsoftware.commons.client.util.StringUtils.notBlank;

/**
 * Date: Jun 19, 2008 Time: 11:34:55 AM
 *
 * @author Alex
 */
public abstract class WebUtils {

  // TODO: remove the duplicated code in these methods (Perhaps replace these methods with a URLBuilder class)

  /**
   * Generates a string in the form "key1=value1&key2=value2...", without url-encoding the components.
   */
  public static String urlQueryString(Map<String, String> paramMap) {
    StringBuilder str = new StringBuilder(256);
    Iterator<Map.Entry<String,String>> iter = paramMap.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<String, String> param = iter.next();
      str.append(param.getKey()).append("=").append(param.getValue());
      if (iter.hasNext())
        str.append("&");
    }
    return str.toString();
  }

  /**
   * Generates a string in the form "key1=value1&key2=value2...". Will URL-encode all the names and values.
   * @param paramMap will be used to construct the query string; not modified by this method
   */
  public static String urlQueryStringEncode(Map<String, String> paramMap) {
    // 1) create an encoded version of the parameters
    LinkedHashMap<String, String> encodedParams = new LinkedHashMap<String, String>();  // using LHM to preserve the ordering of the parameters
    UrlEncoder urlEncoder = UrlEncoder.get();
    for (Map.Entry<String, String> entry : paramMap.entrySet()) {
      encodedParams.put(urlEncoder.encode(entry.getKey()), urlEncoder.encode(entry.getValue()));
    }
    // 2) delegate to the non-encoding version of this method
    return urlQueryString(encodedParams);
  }

  /**
   * @see #urlQueryStringEncode(Map)
   */
  public static String urlQueryStringEncode(String... keyValuePairs) {
    return urlQueryStringEncode(MapUtils.stringLinkedHashMap(keyValuePairs));
  }

  /**
   * Appends a query string in the form "?key1=value1&key2=value2..." to the
   * given URL.
   * @param keyValueList key1, value1, ..., keyN, valueN
   */
  public static String urlWithQueryString(String url, String... keyValueList) {
    // the given list should have an even number of args
    if (ArrayUtils.isEmpty(keyValueList))
      return "";
    if (keyValueList.length % 2 != 0)
      throw new IllegalArgumentException("Uneven number of arguments.");
    boolean key = true;
    StringBuilder str = new StringBuilder(256);
    str.append(url);
    for (int i = 0; i < keyValueList.length; i++) {
      if (i % 2 == 0)
        str.append(i == 0 ? "?" : "&").append(keyValueList[i]);
      else
        str.append("=").append(keyValueList[i]);
    }
    return str.toString();
  }

  /**
   * Returns a URL based on the current page URL, but with the given parameter
   * added (or replaced if was already present).
   * @param newValue the new value of the URL parameter; pass null if the
   * parameter is to be removed from the URL.
   * @deprecated This implementation is buggy; should instead use {@link UrlBuilder}
   * obtained by calling {@link Window.Location#createUrlBuilder()}
   */
  public static String replaceUrlParameter(String paramName, String newValue) {
    // 1) figure out what parameters we want in the new URL
    Map<String, List<String>> originalParams = Window.Location.getParameterMap();
    Map<String, String> newParams = new HashMap<String, String>();
    if (newValue != null)
      newParams.put(paramName, newValue);
    // add all the other parameters
    for (String p : originalParams.keySet()) {
      if (!p.equals(paramName))
        // TODO: this is probably a mistake: URL query params are not joined by ',' - they are listed separately
        newParams.put(p, join(",", originalParams.get(p)));
    }
    // 2) construct a new query string
    String newQS = urlQueryString(newParams);
    // 3) build up the new URL
    StringBuilder url = new StringBuilder().append(Window.Location.getProtocol())
        .append("//")
        .append(Window.Location.getHost())
        .append(Window.Location.getPath());  // NOTE: the ;jsessionid=X part of the URL (if any) is considered part of the path
    if (notBlank(newQS)) {
      url.append("?").append(newQS);
    }
    url.append(Window.Location.getHash());
    return url.toString();
  }

  /**
   * Similar to Window.Location.getParameterMap(), but returns only the parameters
   * with the given keys and returns string values for each parameter instead
   * of lists of values.
   * @return the values of the given parameters which are not null and not empty
   * strings.
   */
  public static Map<String, String> getUrlParameterMap(Iterable<String> keys) {
    LinkedHashMap<String, String> ret = new LinkedHashMap<String, String>();
    for (String key : keys) {
      String value = Window.Location.getParameter(key);
      if (notBlank(value)) {
        ret.put(key, value);
      }
    }
    return ret;
  }

}
