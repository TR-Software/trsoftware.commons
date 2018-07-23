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
import solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletRequest;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.util.MapUtils;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static solutions.trsoftware.commons.server.servlet.ServletUtils.*;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertSameSequence;

public class ServletUtilsTest extends TestCase {

  public void testRequestParametersAsSortedStringMap() throws Exception {
    {
      Map<String, String[]> goodMap = new HashMap<>();
      goodMap.put("foo", new String[]{"a"});
      goodMap.put("bar", new String[]{"b"});
      assertEquals(
          MapUtils.stringMap("foo", "a", "bar", "b"),
          getRequestParametersAsSortedStringMap(goodMap));
    }
    {
      final Map<String, String[]> badMap = new HashMap<>();
      badMap.put("foo", new String[]{"a"});
      badMap.put("bar", new String[]{"b", "c"});
      AssertUtils.assertThrows(IllegalArgumentException.class, (Runnable)() -> getRequestParametersAsSortedStringMap(badMap));
    }
  }

  public void testExtractFirstPathElement() throws Exception {
    assertEquals("foo", extractFirstPathElement(new DummyHttpServletRequest("/foo/")));
    assertEquals("foo", extractFirstPathElement(new DummyHttpServletRequest("/foo/gameserv")));
    assertEquals("foo", extractFirstPathElement(new DummyHttpServletRequest("/foo/gameserv/")));
    assertEquals("foo", extractFirstPathElement(new DummyHttpServletRequest("/foo/bar/gameserv")));
    assertEquals("foo", extractFirstPathElement(new DummyHttpServletRequest("/foo/bar/gameserv/")));

    assertEquals("bar", extractFirstPathElement(new DummyHttpServletRequest("foo/bar/gameserv/")));

    // the following URIs don't have a first path element
    assertNull("foo", extractFirstPathElement(new DummyHttpServletRequest("/")));
    assertNull("foo", extractFirstPathElement(new DummyHttpServletRequest("/foo")));
    assertNull("foo", extractFirstPathElement(new DummyHttpServletRequest("/foo.html")));
  }

  public void testExtractAllPathElements() throws Exception {
    assertSameSequence(asEnumeration(), extractAllPathElements(new DummyHttpServletRequest("/")));
    assertSameSequence(asEnumeration(), extractAllPathElements(new DummyHttpServletRequest("")));
    AssertUtils.assertThrows(NullPointerException.class,
        (Runnable)() -> extractAllPathElements(new DummyHttpServletRequest((String)null)));
    assertSameSequence(asEnumeration("foo"), extractAllPathElements(new DummyHttpServletRequest("/foo/")));
    assertSameSequence(asEnumeration("foo"), extractAllPathElements(new DummyHttpServletRequest("/foo/")));
    assertSameSequence(asEnumeration("foo", "gameserv"), extractAllPathElements(new DummyHttpServletRequest("/foo/gameserv")));
    assertSameSequence(asEnumeration("foo", "gameserv"), extractAllPathElements(new DummyHttpServletRequest("/foo/gameserv/")));
    assertSameSequence(asEnumeration("foo", "bar", "gameserv"), extractAllPathElements(new DummyHttpServletRequest("/foo/bar/gameserv")));
    assertSameSequence(asEnumeration("foo", "bar", "gameserv"), extractAllPathElements(new DummyHttpServletRequest("/foo/bar/gameserv/")));
    assertSameSequence(asEnumeration("foo", "bar", "gameserv"), extractAllPathElements(new DummyHttpServletRequest("/foo/bar//gameserv/")));
    assertSameSequence(asEnumeration("foo", "bar", "gameserv"), extractAllPathElements(new DummyHttpServletRequest("//foo/bar//gameserv/")));
    assertSameSequence(asEnumeration("foo", "bar", "gameserv"), extractAllPathElements(new DummyHttpServletRequest("//foo/bar//gameserv//")));
  }

  public static Enumeration asEnumeration(final Object... items) {
    return new Enumeration() {
      int i = 0;

      public boolean hasMoreElements() {
        return i < items.length;
      }

      public Object nextElement() {
        return items[i++];
      }
    };
  }

  public void testReplaceQueryStringParameter() throws Exception {
    for (String protocol : new String[]{"http", "https"}) {
      DummyHttpServletRequest dummyRequest = new DummyHttpServletRequest(protocol + "://example.com/page", "foo=bar&baz=1");
      assertEquals(protocol + "://example.com/page?bar=foo&baz=1",
          replaceQueryStringParameter(dummyRequest, "foo", "bar", "bar", "foo"));
      assertEquals(protocol + "://example.com/page?baz=2&baz=1",
          replaceQueryStringParameter(dummyRequest, "foo", "bar", "baz", "2"));
      assertEquals(protocol + "://example.com/page?foo=bar&baz=2",
          replaceQueryStringParameter(dummyRequest, "baz", "1", "baz", "2"));
      assertEquals(protocol + "://example.com/page?foo=bar&baz=1",
          replaceQueryStringParameter(dummyRequest, "baz", "2", "baz", "3"));  // param/value combination not present, so url returned unmodified
      assertEquals(protocol + "://example.com/page?foo=bar&baz=1",
          replaceQueryStringParameter(dummyRequest, "baz", "bar", "baz", "2")); // param/value combination not present, so url returned unmodified
    }
  }

  public void testAddIndexedMultivaluedParams() throws Exception {
    Map<String, String> map = MapUtils.sortedMap("foo", "bar");
    assertSame(map, addIndexedMultivaluedParams(map, "x", "a", 25, "foo"));
    // should have added 3 new entries (("x0", "a"), ("x1", 25), and ("x2", "foo"))
    // to the given map
    assertEquals(4, map.size());
    assertEquals("a", map.get("x0"));
    assertEquals("25", map.get("x1"));
    assertEquals("foo", map.get("x2"));
  }

  public void testReadIndexedMultivaluedParams() throws Exception {
    assertEquals(
        Arrays.asList("a", "25", "foo"),
        readIndexedMultivaluedParams(
            new DummyHttpServletRequest(MapUtils.hashMap("foo", "bar", "x2", "foo", "x0", "a", "x1", "25")),
            "x"));
    assertEquals(
        Arrays.<String>asList(),  // missing "x0", so returns nothing
        readIndexedMultivaluedParams(
            new DummyHttpServletRequest(MapUtils.hashMap("foo", "bar", "x2", "foo", "x1", "25")),
            "x"));
    assertEquals(
        Arrays.<String>asList("a"),  // missing "x1", so returns just the value for x0
        readIndexedMultivaluedParams(
            new DummyHttpServletRequest(MapUtils.hashMap("foo", "bar", "x2", "foo", "x0", "a")),
            "x"));
  }

  public void testGetBaseUrl() throws Exception {
    for (String protocol : new String[]{"http", "https"}) {
      assertEquals(protocol + "://localhost:8088", getBaseUrl(
          new DummyHttpServletRequest().setUrl(protocol + "://localhost:8088/errorPage").setUri("/errorPage")).toString());
      assertEquals(protocol + "://localhost:8088", getBaseUrl(
          new DummyHttpServletRequest().setUrl(protocol + "://localhost:8088/").setUri("/")).toString());
      assertEquals(protocol + "://example.com", getBaseUrl(
          new DummyHttpServletRequest().setUrl(protocol + "://example.com/foo/bar").setUri("/")).toString());
    }
  }

}