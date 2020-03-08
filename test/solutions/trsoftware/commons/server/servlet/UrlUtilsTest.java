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

import com.google.common.collect.ImmutableMultimap;
import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import java.net.URL;

import static solutions.trsoftware.commons.server.servlet.UrlUtils.*;
import static solutions.trsoftware.commons.shared.util.MapUtils.stringLinkedHashMap;

/**
 * @author Alex
 * @since 11/14/2017
 */
public class UrlUtilsTest extends TestCase {

  public void testUrlQueryString() throws Exception {
    assertEquals("foo=1&bar=baz", urlQueryString(
            stringLinkedHashMap("foo", "1", "bar", "baz")));
    assertEquals("size=3%2F4&phone%23%3D=617+123+4567", urlQueryString(
            stringLinkedHashMap("size", "3/4", "phone#=", "617 123 4567")));
    // now test the overloaded method that takes an array
    assertEquals("foo=1&bar=baz", urlQueryString("foo", "1", "bar", "baz"));
    assertEquals("size=3%2F4&phone%23%3D=617+123+4567", urlQueryString(
            "size", "3/4", "phone#=", "617 123 4567"));
  }

  public void testRewrite() throws Exception {
    String[] protocols = new String[]{"http", "https"};
    for (String protocol : protocols) {
      URL url = new URL(protocol + "://www.example.com/foo/bar?foo=bar&bar=baz");
      assertEquals(protocol + "://www.example.com/new/path?p1=foo&p2=bar",
          rewrite(url, "/new/path", "p1", "foo", "p2", "bar"));
    }
  }

  public void testReplaceQueryStringParameter() throws Exception {
    assertEquals("bar=foo&baz=1",
        replaceQueryStringParameter("foo=bar&baz=1", "foo", "bar", "bar", "foo"));
    assertEquals("baz=2&baz=1",
        replaceQueryStringParameter("foo=bar&baz=1", "foo", "bar", "baz", "2"));
    assertEquals("foo=bar&baz=2",
        replaceQueryStringParameter("foo=bar&baz=1", "baz", "1", "baz", "2"));
    assertEquals("foo=bar&baz=1",
        replaceQueryStringParameter("foo=bar&baz=1", "baz", "2", "baz", "3"));  // param/value combination not present, so url returned unmodified
    assertEquals("foo=bar&baz=1",
        replaceQueryStringParameter("foo=bar&baz=1", "baz", "bar", "baz", "2")); // param/value combination not present, so url returned unmodified

    // now test the overloaded version of the method the same way
    assertEquals("foo=foo&baz=1",
        replaceQueryStringParameter("foo=bar&baz=1", "foo", "bar", "foo"));
    assertEquals("foo=bar&baz=1",
        replaceQueryStringParameter("foo=bar&baz=1", "baz", "2", "3"));  // param/value combination not present, so url returned unmodified
    assertEquals("foo=bar&baz=1",
        replaceQueryStringParameter("foo=bar&baz=1", "baz", "bar", "2")); // param/value combination not present, so url returned unmodified
  }

  public void testParseQueryString() throws Exception {
    // 1) with 2 single-valued params
    assertEquals(ImmutableMultimap.of("code", "xKyEiW9ku9S5", "id", "tr:foobar"),
        parseQueryString(new URL("http://localhost:8080/confirm_email?code=xKyEiW9ku9S5&id=tr%3Afoobar")));
    // 2) with 2 params, one of which having multiple values
    assertEquals(ImmutableMultimap.of("code", "xKyEiW9ku9S5", "id", "tr:foobar", "id", "foo bar", "id", "foo bar"),
        parseQueryString(new URL("http://localhost:8080/confirm_email?code=xKyEiW9ku9S5&id=tr%3Afoobar&id=foo+bar&id=foo%20bar")));
    // 3) with 2 params, one of which having no value (unusual but possible; see https://stackoverflow.com/q/4557387)
    assertEquals(ImmutableMultimap.of("code", "xKyEiW9ku9S5", "id", ""),
        parseQueryString(new URL("http://localhost:8080/confirm_email?code=xKyEiW9ku9S5&id")));
    assertEquals(ImmutableMultimap.of("code", "xKyEiW9ku9S5", "id", ""),
        parseQueryString(new URL("http://localhost:8080/confirm_email?code=xKyEiW9ku9S5&id=")));
    // 4) with a single param having no value
    assertEquals(ImmutableMultimap.of("id", ""),
        parseQueryString(new URL("http://localhost:8080/confirm_email?id")));
    assertEquals(ImmutableMultimap.of("id", ""),
        parseQueryString(new URL("http://localhost:8080/confirm_email?id=")));
    // 5) with a single param whose name and value are both %-encoded
    assertEquals(ImmutableMultimap.of("foo bar", "hello world"),
        parseQueryString(new URL("http://localhost:8080/confirm_email?foo+bar=hello%20world")));
  }

  /**
   * Tests that the results of {@link UrlUtils#urlEncode(String)} can be converted back using
   * {@link UrlUtils#urlDecode(String)}
   */
  public void testUrlEncode() throws Exception {
    for (int i = 0; i < 10_000; i++) {
      String original = RandomUtils.randString(40);
      String encoded = UrlUtils.urlEncode(original);
      String decoded = UrlUtils.urlDecode(encoded);
      String msg = String.format("'%s' -> '%s' -> '%s'", original, encoded, decoded);
      assertEquals(msg, original, decoded);
    }
  }

  public void testUrlDecode() throws Exception {
    // check that it's able to decode spaces encoded as '+' chars as well as '%20'
    assertEquals("foo bar", UrlUtils.urlDecode("foo+bar"));
    assertEquals("foo bar", UrlUtils.urlDecode("foo%20bar"));
  }
}