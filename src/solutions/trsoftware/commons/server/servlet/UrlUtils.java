package solutions.trsoftware.commons.server.servlet;

import solutions.trsoftware.commons.shared.util.ArrayUtils;
import solutions.trsoftware.commons.shared.util.MapUtils;

import java.net.URL;
import java.util.Map;

import static solutions.trsoftware.commons.server.util.ServerStringUtils.urlEncode;

/**
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
   * Rewrites the path and query string of the given URL.
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

}
