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

package solutions.trsoftware.commons.server.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import java.util.Collections;

import static java.util.Arrays.asList;
import static solutions.trsoftware.commons.server.util.ServerStringUtils.*;

public class ServerStringUtilsTest extends TestCase {

  public void testFindAll() throws Exception {
    assertEquals(asList("foo", "foo", "foo"),
        findAll("foo barr bar fooo bar foo r",
            "foo"));

    assertEquals(asList("{*actor*}", "{*wpm*}", "{*title*}", "{*applink*}"),
        findAll("{*actor*} typed {*wpm*} words-per-minute on a quote from {*title*}, setting a new personal record on {*applink*}",
            "\\{\\*\\w+\\*\\}"));

    assertEquals(asList("actor", "wpm", "title", "applink"),
        findAll("{*actor*} typed {*wpm*} words-per-minute on a quote from {*title*}, setting a new personal record on {*applink*}",
            "\\{\\*(\\w+)\\*\\}", 1));
  }

  public void testSprintf() throws Exception {
    assertEquals("Hello world", String.format("Hello %s", "world"));
    assertEquals("Hello world, #2", String.format("Hello %s, #%d", "world", 2));
    assertEquals("123.46", String.format("%.2f", 123.456));
    assertEquals("123.46", String.format("%6.2f", 123.456));
    assertEquals(" 123.46", String.format("%7.2f", 123.456));
    assertEquals("  123.46", String.format("%8.2f", 123.456));
  }

  public void testDeflateString() throws Exception {
    // try a one-line string that should compress well
    checkStringCompression("it's allright, it's allright, it's allright, she moves in mysterious ways.");
    // try a multi-line string that should compress well
    checkStringCompression("it's allright\nit's allright\nit's allright\nshe moves in mysterious ways.");
    // try a multi-line unicode string
    checkStringCompression("\u0412\u0441\u0435 \u0432 \u043F\u043E\u0440\u044F\u0434\u043A\u0435\n\u0412\u0441\u0435 \u0432 \u043F\u043E\u0440\u044F\u0434\u043A\u0435\n\u0412\u0441\u0435 \u0432 \u043F\u043E\u0440\u044F\u0434\u043A\u0435\n\u043E\u043D\u0430 \u0434\u0432\u0438\u0436\u0435\u0442\u0441\u044F \u0432 \u0442\u0430\u0438\u043D\u0441\u0442\u0432\u0435\u043D\u043D\u0430\u044F \u0441\u043F\u043E\u0441\u043E\u0431\u0430\u043C\u0438.");  // the same text in Russian
    // try a single-line unicode string
    checkStringCompression("\u5B83\u7684\u6240\u6709\u6743\uFF0C\u5B83\u662F\u6240\u6709\u6743\uFF0C\u5B83\u7684\u6240\u6709\u6743\uFF0C\u5979\u52A8\u4F5C\u662F\u4E0D\u53EF\u601D\u8BAE\u7684\u3002");  // the same thing in Chinese

  }

  private void checkStringCompression(String input) {
    byte[] compressedBytes = deflateString(input);
    // check that the compression reduced the size of the string
    assertTrue(compressedBytes.length < stringToBytesUtf8(input).length);
    // check that the compressoin is reversible
    assertEquals(input, inflateString(compressedBytes));
  }

  public void testSearchByEditDistance() throws Exception {
    assertEquals(asList("foo", "foolio"),
        searchByEditDistance(asList("foo", "bar", "baz", "foolio", "barrio", "bazzar"), "fool", 3));

    assertEquals(asList("foo", "foolio"),
        searchByEditDistance(asList("foo", "bar", "baz", "foolio", "barrio", "bazzar"), "fool", 2));

    assertEquals(asList("foo"),
        searchByEditDistance(asList("foo", "bar", "baz", "foolio", "barrio", "bazzar"), "fool", 1));

    assertEquals(Collections.<String>emptyList(),
        searchByEditDistance(asList("foo", "bar", "baz", "foolio", "barrio", "bazzar"), "fool", 0));

    assertEquals(asList("foo"),
        searchByEditDistance(asList("foo", "bar", "baz", "foolio", "barrio", "bazzar"), "foo", 0));

    // if we set the distance high enough, all strings should match
    // test that all the results are ranked by their proximity
    assertEquals(asList("foo", "foolio", "bar", "baz", "barrio", "bazzar"),
        searchByEditDistance(asList("foo", "bar", "baz", "foolio", "barrio", "bazzar"), "fool", 6));

    // test that all the results are ranked by their proximity again, this time more precisely
    assertEquals(asList("a", "aa", "ab", "abc", "abcd", "abcde", "abcde"),
        searchByEditDistance(asList("abc", "aa", "abcd", "abcde", "a", "ab", "abcde"), "a", 4));
  }

  public void testToUnicodeLiteral() throws Exception {
    assertEquals("\"What up \\u041f\\u0438\\u0434\\u0430\\u0440\\u0430\\u0441 \\ndogg?\"",
        toUnicodeLiteral("What up \u041f\u0438\u0434\u0430\u0440\u0430\u0441 \ndogg?"));
  }

  public void testToJavaNameIdentifier() throws Exception {
    assertEquals("foo_bar", toJavaNameIdentifier("foo+bar"));
    assertEquals("foo_bar", toJavaNameIdentifier("foo!bar"));
    assertEquals("_foo_bar", toJavaNameIdentifier("+foo!bar"));
    // check some reserved keywords
    assertEquals("_do", toJavaNameIdentifier("do"));
    assertEquals("_while", toJavaNameIdentifier("while"));
    assertEquals("_try", toJavaNameIdentifier("try"));
    assertEquals("_try", toJavaNameIdentifier("try"));
    // check empty strings
    assertEquals("_", toJavaNameIdentifier(""));
    assertEquals("_", toJavaNameIdentifier(null));
    // check strings of length 1
    assertEquals("_", toJavaNameIdentifier("_"));
    assertEquals("x", toJavaNameIdentifier("x"));
  }

  public void testMatch() throws Exception {
    // 1) no match
    assertNull(match("foo", "bar"));
    // 2) match but no capturing groups
    {
      String[] match = match("asdfqwer", "a.*q.*");
      assertEquals(1, match.length);
      assertEquals("asdfqwer", match[0]);
    }
    // 3) with capturing groups
    {
      String[] match = match("asdfqwer", "(a.*)(q.*)");
      assertEquals(3, match.length);
      assertEquals("asdfqwer", match[0]);
      assertEquals("asdf", match[1]);
      assertEquals("qwer", match[2]);
    }
  }

  public void test_isUrlSafe() throws Exception {
    assertTrue(isUrlSafe(""));
    assertTrue(isUrlSafe("asdf"));
    assertTrue(isUrlSafe("_asdf_-asdf_"));
    assertFalse(isUrlSafe("asdf/"));
    assertFalse(isUrlSafe("asdf="));
    assertFalse(isUrlSafe("asdf+asdf"));
  }

  /**
   * Tests that the results of {@link ServerStringUtils#urlEncode(String)} can be converted back using
   * {@link ServerStringUtils#urlDecode(String)}
   */
  public void testUrlEncode() throws Exception {
    for (int i = 0; i < 10_000; i++) {
      String original = RandomUtils.randString(40);
      String encoded = urlEncode(original);
      String decoded = urlDecode(encoded);
      String msg = String.format("'%s' -> '%s' -> '%s'", original, encoded, decoded);
      assertEquals(msg, original, decoded);
    }
  }

}