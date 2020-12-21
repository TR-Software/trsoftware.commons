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
import junit.framework.TestCase;
import solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletRequest;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.Collections.*;

/**
 * @author Alex
 * @since 12/19/2020
 */
public class HttpHeadersTest extends TestCase {

  private ImmutableListMultimap<String, String> mockHeaders;

  public void setUp() throws Exception {
    super.setUp();
    // NOTE: adding 2 separate Cookie headers, even though that's not standards-compliant (header names should not be duplicated, according to https://tools.ietf.org/html/rfc7230#section-3.2.2)
    mockHeaders = ImmutableListMultimap.<String, String>builder()
        .putAll("Accept-Language", "en-US,en;q=0.5")
        .putAll("Accept-Encoding", "gzip, deflate, br")
        .putAll("Connection", "keep-alive")
        .putAll("Upgrade-Insecure-Requests", "1")
        .putAll("If-Modified-Since", "Mon, 18 Jul 2016 02:36:04 GMT")
        .putAll("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178")
        // NOTE: adding 2 separate Cookie headers, even though that's not really standards-compliant (header names should not be duplicated, according to https://tools.ietf.org/html/rfc7230#section-3.2.2)
        .putAll("Cookie", "name=value; name2=value2")
        .putAll("Cookie", "foo=bar")
        .build();
  }

  public void tearDown() throws Exception {
    mockHeaders = null;
    super.tearDown();
  }

  public void testGetHeader() throws Exception {
//    RequestCopy requestCopy = new RequestCopy(new DummyHttpServletRequest().setHeaders(mockHeaders));
//    RequestCopy requestCopy = new RequestCopy(new DummyHttpServletRequest().setHeaders(mockHeaders));
    HttpHeaders headers = new HttpHeaders(mockHeaders);
    assertEquals("en-US,en;q=0.5", headers.getHeader("Accept-Language"));
    // should be case-insensitive
    assertEquals("en-US,en;q=0.5", headers.getHeader("accept-language"));
    // should return the 1st of multiple values
    assertEquals("name=value; name2=value2", headers.getHeader("Cookie"));
    // should return null if header not present
    assertNull(headers.getHeader("FOO"));
  }

  public void testGetHeaders() throws Exception {
    HttpHeaders headers = new HttpHeaders(mockHeaders);
    assertEquals(singletonList("en-US,en;q=0.5"), list(headers.getHeaders("Accept-Language")));
    // should be case-insensitive
    assertEquals(singletonList("en-US,en;q=0.5"), list(headers.getHeaders("accept-language")));
    // should return all values for a header specified multiple times
    assertEquals(Arrays.asList("name=value; name2=value2", "foo=bar"), list(headers.getHeaders("Cookie")));
    // should return empty enumeration if header not present
    assertEquals(emptyList(), list(headers.getHeaders("FOO")));
  }

  public void testGetHeaderNames() throws Exception {
    HttpHeaders headers = new HttpHeaders(mockHeaders);
    assertEquals(mockHeaders.keySet().stream().map(String::toLowerCase).collect(Collectors.toList()),
        list(headers.getHeaderNames()));
    // should return an empty enumeration if no headers present
    assertEquals(emptyList(), list(new RequestCopy(new DummyHttpServletRequest()).getHeaderNames()));
  }

  public void testGetIntHeader() throws Exception {
    HttpHeaders headers = new HttpHeaders(mockHeaders);
    assertEquals(1, headers.getIntHeader("upgrade-insecure-requests"));
    // should return -1 if header not present
    assertEquals(-1, headers.getIntHeader("FOO"));
    // should throw exception if header value can't be parsed as int
    AssertUtils.assertThrows(NumberFormatException.class, (Runnable)() -> headers.getIntHeader("Cookie"));
  }

  public void testGetDateHeader() throws Exception {
    /*
      According to RFC 2616, HTTP servers are required to support all 3 of the following date formats
      (see https://tools.ietf.org/html/rfc2616#section-3.3)
     */
    ImmutableListMultimap<String, String> dateFormatExamples = ImmutableListMultimap.<String, String>builder()
        .put("Date", "Sun, 06 Nov 1994 08:49:37 GMT")  // RFC 822, updated by RFC 1123
        .put("If-Modified-Since", "Sunday, 06-Nov-94 08:49:37 GMT") // RFC 850, obsoleted by RFC 1036
        .put("If-Unmodified-Since", "Sun Nov  6 08:49:37 1994") // ANSI C's asctime() format
        .put("Upgrade-Insecure-Requests", "1")
        .build();
    HttpHeaders headers = new HttpHeaders(dateFormatExamples);

    assertEquals(784111777000L, headers.getDateHeader("Date"));
    assertEquals(784111777000L, headers.getDateHeader("if-modified-since"));  // should be case-insensitive
    assertEquals(784111777000L, headers.getDateHeader("IF-UNMODIFIED-SINCE")); // should be case-insensitive
    assertEquals(-1L, headers.getDateHeader("FOO"));  // header not present
    AssertUtils.assertThrows(IllegalArgumentException.class, () -> headers.getDateHeader("Upgrade-Insecure-Requests"));
  }

}