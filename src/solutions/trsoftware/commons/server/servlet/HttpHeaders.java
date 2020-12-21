/*
 * Copyright 2020 TR Software Inc.
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

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * Adapter for a {@link Multimap} representation of HTTP request headers.  Can be used to implement
 * the {@link HttpServletRequest} header accessor methods.
 */
public class HttpHeaders {
  private final Multimap<String, String> headers;

  /**
   * @param headers the headers as a multimap; all keys should be lowercase to allow case-insensitive lookups by name
   */
  public HttpHeaders(@Nonnull Multimap<String, String> headers) {
    // make sure all keys are lowercase
    if (headers.keySet().stream().anyMatch(s -> !StringUtils.isLowercase(s))) {
      // create a new multimap, with all keys converted to lowercase
      ImmutableListMultimap.Builder<String, String> builder = ImmutableListMultimap.builder();
      for (Map.Entry<String, String> entry : headers.entries()) {
        String key = entry.getKey();
        String value = entry.getValue();
        if (key != null && value != null) {
          // immutable multimaps don't allow null keys/values; we can safely omit these without changing the semantics of this class
          builder.put(key.toLowerCase(), value);
        }
      }
      headers = builder.build();
    }
    this.headers = headers;
  }

  public HttpHeaders(@Nonnull HttpServletRequest request) {
    headers = ServletUtils.getRequestHeadersAsMultimap(request);
  }

  /**
   * @see HttpServletRequest#getDateHeader(String)
   */
  public long getDateHeader(String name) {
    // NOTE: this code was partly borrowed from org.apache.catalina.connector.Request
    String value = getHeader(name);
    if (value == null)
      return -1L;

    // Attempt to convert the date header in a variety of formats
    long result = HttpDateParser.parseDate(value);
    if (result != -1L)
      return result;
    throw new IllegalArgumentException(value);
  }

  /**
   * @see HttpServletRequest#getHeader(String)
   */
  public String getHeader(String name) {
    Collection<String> values = getHeaderValues(name);
    return Iterables.getFirst(values, null);
  }

  /**
   * @see HttpServletRequest#getHeaders(String)
   */
  public Enumeration<String> getHeaders(String name) {
    return Collections.enumeration(getHeaderValues(name));
  }

  /**
   * Performs a case-insensitive lookup in the {@link #headers} multimap.
   *
   * @return a (possibly-empty) list of values for the given header
   */
  @Nonnull
  private Collection<String> getHeaderValues(String name) {
    return headers.get(name.toLowerCase());
  }

  /**
   * @see HttpServletRequest#getHeaderNames()
   */
  public Enumeration<String> getHeaderNames() {
    return Collections.enumeration(headers.keySet());
  }

  /**
   * @see HttpServletRequest#getIntHeader(String)
   */
  public int getIntHeader(String name) {
    String value = getHeader(name);
    return value == null ? -1 : Integer.parseInt(value);
  }

  public Multimap<String, String> getHeadersAsMultimap() {
    return headers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    HttpHeaders that = (HttpHeaders)o;

    return headers.equals(that.headers);
  }

  @Override
  public int hashCode() {
    return headers.hashCode();
  }


}
