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

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.google.common.collect.ImmutableMultimap;
import junit.framework.TestCase;
import solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletRequest;
import solutions.trsoftware.commons.server.servlet.testutil.LiveServletTestCase;
import solutions.trsoftware.commons.server.servlet.testutil.RequestEchoServlet;
import solutions.trsoftware.commons.server.testutil.EmbeddedTomcatServer;
import solutions.trsoftware.commons.shared.annotations.ExcludeFromSuite;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.Collections.*;

/**
 * @author Alex
 * @since 12/17/2020
 */
public class RequestCopyTest extends TestCase {

  private ImmutableMultimap<String, String> mockHeaders;

  public void setUp() throws Exception {
    super.setUp();
    // NOTE: adding 2 separate Cookie headers, even though that's not standards-compliant (header names should not be duplicated, according to https://tools.ietf.org/html/rfc7230#section-3.2.2)
    mockHeaders = ImmutableMultimap.<String, String>builder()
        .put("Accept-Language", "en-US,en;q=0.5")
        .put("Accept-Encoding", "gzip, deflate, br")
        .put("Connection", "keep-alive")
        .put("Upgrade-Insecure-Requests", "1")
        // NOTE: using different date formats in the following headers to test HSR.getDateHeader on the 3 different date formats supported in HTTP/1.1
        .put("If-Modified-Since", "Sunday, 06-Nov-94 08:49:37 GMT")
        .put("Date", "Sun, 06 Nov 1994 08:49:37 GMT")
        .put("If-Unmodified-Since", "Sun Nov  6 08:49:37 1994")
        .put("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178")
        // NOTE: adding 2 separate Cookie headers, even though that's not standards-compliant (header names should not be duplicated, according to https://tools.ietf.org/html/rfc7230#section-3.2.2)
        .put("Cookie", "name=value; name2=value2")
        .put("Cookie", "foo=bar")
        .build();
  }

  public void tearDown() throws Exception {
    mockHeaders = null;
    super.tearDown();
  }

  public void testGetHeader() throws Exception {
    RequestCopy requestCopy = new RequestCopy(new DummyHttpServletRequest().setHeaders(mockHeaders));
    assertEquals("en-US,en;q=0.5", requestCopy.getHeader("Accept-Language"));
    // should be case-insensitive
    assertEquals("en-US,en;q=0.5", requestCopy.getHeader("accept-language"));
    // should return the 1st of multiple values
    assertEquals("name=value; name2=value2", requestCopy.getHeader("Cookie"));
    // should return null if header not present
    assertNull(requestCopy.getHeader("FOO"));
  }

  public void testGetHeaders() throws Exception {
    RequestCopy requestCopy = new RequestCopy(new DummyHttpServletRequest().setHeaders(mockHeaders));
    assertEquals(singletonList("en-US,en;q=0.5"), list(requestCopy.getHeaders("Accept-Language")));
    // should be case-insensitive
    assertEquals(singletonList("en-US,en;q=0.5"), list(requestCopy.getHeaders("accept-language")));
    // should return all values for a header specified multiple times
    assertEquals(Arrays.asList("name=value; name2=value2", "foo=bar"), list(requestCopy.getHeaders("Cookie")));
    // should return empty enumeration if header not present
    assertEquals(emptyList(), list(requestCopy.getHeaders("FOO")));
  }

  public void testGetHeaderNames() throws Exception {
    RequestCopy requestCopy = new RequestCopy(new DummyHttpServletRequest().setHeaders(mockHeaders));
    assertEquals(mockHeaders.keySet().stream().map(String::toLowerCase).collect(Collectors.toList()),
        list(requestCopy.getHeaderNames()));
    // should return an empty enumeration if no headers present
    assertEquals(emptyList(), list(new RequestCopy(new DummyHttpServletRequest()).getHeaderNames()));
  }

  public void testGetIntHeader() throws Exception {
    RequestCopy requestCopy = new RequestCopy(new DummyHttpServletRequest().setHeaders(mockHeaders));
    assertEquals(1, requestCopy.getIntHeader("upgrade-insecure-requests"));
    // should return -1 if header not present
    assertEquals(-1, requestCopy.getIntHeader("FOO"));
    // should throw exception if header value can't be parsed as int
    AssertUtils.assertThrows(NumberFormatException.class, (Runnable)() -> requestCopy.getIntHeader("Cookie"));
  }

  public void testGetDateHeader() throws Exception {
    RequestCopy requestCopy = new RequestCopy(new DummyHttpServletRequest().setHeaders(mockHeaders));
    assertEquals(784111777000L, requestCopy.getDateHeader("Date"));
    assertEquals(784111777000L, requestCopy.getDateHeader("if-modified-since"));  // should be case-insensitive
    assertEquals(784111777000L, requestCopy.getDateHeader("IF-UNMODIFIED-SINCE")); // should be case-insensitive
    assertEquals(-1L, requestCopy.getDateHeader("FOO"));  // header not present
    AssertUtils.assertThrows(IllegalArgumentException.class, () -> requestCopy.getDateHeader("Upgrade-Insecure-Requests"));  // header value can't be parsed using any of the supported date formats
  }

  @Slow
  @ExcludeFromSuite
  public void testWithLiveServlet() throws Exception {
    // NOTE: this test case doesn't check any assertions; but it can be used for manual debugging (e.g. to check how Tomcat parses request headers)
    try (EmbeddedTomcatServer embeddedTomcat = new EmbeddedTomcatServer();
         WebClient webClient = new WebClient()) {
      EmbeddedTomcatServer.ServletHandle servletHandle = embeddedTomcat.addServlet(new RequestEchoServlet(), "/echo");
      embeddedTomcat.start();
      // 1) request with X-Forwarded-For header
      {
        WebRequest request = new WebRequest(new URL(servletHandle.getUrlBuilder().toString()));
        request.setAdditionalHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178");
        TextPage page = webClient.getPage(request);
        String response = LiveServletTestCase.printResponse(page);
      }
    }
  }
}