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

import junit.framework.TestCase;

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
}