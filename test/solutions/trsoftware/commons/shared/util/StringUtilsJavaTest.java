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

package solutions.trsoftware.commons.shared.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;

import java.util.*;

import static solutions.trsoftware.commons.shared.util.StringUtils.*;

/**
 * Date: Jul 7, 2008 Time: 2:34:57 PM
 *
 * @author Alex
 */
public class StringUtilsJavaTest extends TestCase {

  public void testTemplate() throws Exception {
    assertEquals("x-y+x", template("$1-$2+$1", "x", "y"));
    assertEquals("\\w+@example.com", template("$1@$2.com", "\\w+", "example"));
    assertEquals("foo$bar", template("$1$$2", "foo", "bar"));
    assertEquals("$foo$$bar", template("$$1$$$2", "foo", "bar"));
    assertEquals("$foo$$bar$", template("$$1$$$2$", "foo", "bar"));
    assertEquals("$xfoo$$bar$", template("$x$1$$$2$", "foo", "bar"));
    assertEquals("foo$xfoo$$bar$", template("foo$x$1$$$2$", "foo", "bar"));
  }

  public void testStripTrailing() throws Exception {
    assertEquals("foo", stripTrailing("foo,", ","));
    assertEquals("foo", stripTrailing("foo", ","));
    assertEquals("foo", stripTrailing("foobar", "bar"));
    assertEquals("", stripTrailing("foobar", "foobar"));
    assertEquals("", stripTrailing("", "foobar"));
    assertEquals("", stripTrailing("", ""));
    assertEquals("foo", stripTrailing("foo", ""));
  }

  public void testRepeat() throws Exception {
    // test the version taking a char arg
    assertEquals("", repeat('a', 0));
    assertEquals("a", repeat('a', 1));
    assertEquals("aa", repeat('a', 2));
    assertEquals("aaa", repeat('a', 3));

    // test overloaded method taking a string arg
    assertEquals("", repeat("ab", 0));
    assertEquals("ab", repeat("ab", 1));
    assertEquals("abab", repeat("ab", 2));
    assertEquals("ababab", repeat("ab", 3));
  }

  public void testConstantNameToTitleCase() throws Exception {
    assertEquals("Foo bar", constantNameToTitleCase("FOO_BAR"));
    assertEquals("Foo bar baz", constantNameToTitleCase("FOO_BAR_BAZ"));
    assertEquals("Foo", constantNameToTitleCase("FOO"));
    assertEquals("Foo bar", constantNameToTitleCase("FOO_BAR_"));
    assertEquals("Foo bar baz", constantNameToTitleCase("FOO_BAR_BAZ__"));
    assertEquals("Foo", constantNameToTitleCase("__FOO__"));
    assertEquals("", constantNameToTitleCase(""));
    assertEquals("", constantNameToTitleCase("_"));
    assertEquals("", constantNameToTitleCase("__"));
  }

  /**
   * Test a few strings of length 0..10 and check them for length, alphabet, and
   * degree of randomness
   */
  public void testRandString() throws Exception {
    int iterations = 5;
    for (int len = 0; len < 10; len++) {
      for (int i = 0; i < iterations; i++) {
        String result = randString(len);
        System.out.println(template("randString($1) = $2", len, result));
        assertEquals(len, result.length());
        // make sure the string characters are mostly different and are all letters in [A-Za-z]
        assertTrue(result.matches("[A-Za-z]*"));
        if (len > 5)
          assertTrue(toCharacterSet(result).size() >= result.length() / 2);
      }
    }
    // Make sure that randomString(int) generates all possible characters in [A-Za-z]
    SortedSet<Character> charSet = new TreeSet<Character>();
    // there are 26*2 characters in this range; 10K iterations should be enough to generate them all
    for (int i = 0; i < 10000; i++) {
      charSet.addAll(toCharacterSet(randString(10)));
    }
    assertEquals(26 * 2, charSet.size());
    System.out.println("Successfully generated all possible characters in [A-Za-z]: " + charSet.toString());
  }

  public void testToCamelCase() throws Exception {
    assertEquals(null, toCamelCase(null, "_"));
    assertEquals("", toCamelCase("", "_"));
    assertEquals("mystring", toCamelCase("MYSTRING", "_"));
    assertEquals("myString", toCamelCase("MY_STRING", "_"));
    assertEquals("myString", toCamelCase("my_string", "_"));
    assertEquals("myStringHere", toCamelCase("MY_STRING_HERE", "_"));
    assertEquals("myStringHere", toCamelCase("MY_STRING_HERE_", "_"));
    assertEquals("MyStringHere", toCamelCase("_MY_STRING_HERE", "_"));
    assertEquals("MyStringHere", toCamelCase("_MY_STRING_HERE_", "_"));
    assertEquals("MyStringHere", toCamelCase("_MY_STRING__HERE_", "_"));
    assertEquals("MyStringHere", toCamelCase("_MY_STRING__HERE__", "_"));
    assertEquals("MyStringHere", toCamelCase("_MY___STRING__HERE__", "_"));
    assertEquals("MyStringHere", toCamelCase("____MY___STRING__HERE____", "_"));
    assertEquals("myStringHere", toCamelCase("MY______STRING__HERE__", "_"));
    // now try some other word separators:
    assertEquals("myString", toCamelCase("my-string", "-"));
    assertEquals("myString", toCamelCase("my%string", "%"));
    assertEquals("myString", toCamelCase("my---string", "---"));
    assertEquals("myStr-ing", toCamelCase("my---str-ing", "---"));
    // now try using a regex as the word separator:
    assertEquals("myStrIng", toCamelCase("my0str1ing", "\\d"));
    assertEquals("myStrIng", toCamelCase("myXstrYing", "X|Y"));
    assertEquals("myStrIng", toCamelCase("myXstrYYYYing", "X+|Y+"));
  }

  public void testNotBlank() throws Exception {
    assertTrue(notBlank("a"));
    assertFalse(notBlank(""));
    assertFalse(notBlank(null));
  }

  public void testIsBlank() throws Exception {
    assertFalse(isBlank("a"));
    assertTrue(isBlank(""));
    assertTrue(isBlank(null));
  }

  public void testCount() throws Exception {
    assertEquals(0, count(null, 'a'));
    assertEquals(0, count("", 'a'));
    assertEquals(1, count("a", 'a'));
    assertEquals(5, count("abra cadabra", 'a'));
  }

  public void testAbbreviate() throws Exception {
    // 1 - test the default version of the method
    // 1.1 - test some unusual arg values
    for (int i = -100; i < 100; i++) {
      assertNull(abbreviate(null, i));  // a null input always returns null regardless of the 2nd arg
      // if maxLen is not a positive integer, should always return an empty string, for any non-null value of the 1st arg
      if (i <= 0) {
        assertEquals("", abbreviate("", i));
        assertEquals("", abbreviate("a", i));
        assertEquals("", abbreviate("ab", i));
        assertEquals("", abbreviate("abc def", i));
      }
    }
    // 1.2 - now test it with some reasonable arg values:
    assertEquals("abcdefg", abbreviate("abcdefg", 8));
    assertEquals("abcdefg", abbreviate("abcdefg", 7));
    assertEquals("abc...", abbreviate("abcdefg", 6));
    assertEquals("a...", abbreviate("abcdefg", 4));
    assertEquals("a..", abbreviate("abcdefg", 3));  // should always leave at least 1 char from the original string, but trim the suffix if needed/possible
    assertEquals("a.", abbreviate("abcdefg", 2));
    assertEquals("a", abbreviate("abcdefg", 1));
    assertEquals("", abbreviate("abcdefg", 0));

    // 2 - test the overloaded version of the method taking a custom suffix
    // 2.1 - test some unusual arg values
    for (int i = -100; i < 100; i++) {
      for (String suffix : new String[]{null, "", ".", "..", "..."}) {
        assertNull(abbreviate(null, i));  // a null input always returns null regardless of the 2nd and 3rd args
        // if maxLen is not a positive integer, should always return an empty string, for any non-null value of the 1st arg
        if (i <= 0) {
          assertEquals("", abbreviate("", i, suffix));
          assertEquals("", abbreviate("a", i, suffix));
          assertEquals("", abbreviate("ab", i, suffix));
          assertEquals("", abbreviate("abc def", i, suffix));
        }
      }
    }
    // 2.2 -now test it with some reasonable arg values:
    assertEquals("abcd", abbreviate("abcd", 5, ""));
    assertEquals("abcd", abbreviate("abcd", 4, ""));
    assertEquals("abc", abbreviate("abcd", 3, ""));
    assertEquals("ab", abbreviate("abcd", 2, ""));
    assertEquals("a", abbreviate("abcd", 1, ""));
    assertEquals("", abbreviate("abcd", 0, ""));

    assertEquals("abcd", abbreviate("abcd", 5, "."));
    assertEquals("abcd", abbreviate("abcd", 4, "."));
    assertEquals("ab.", abbreviate("abcd", 3, "."));
    assertEquals("a.", abbreviate("abcd", 2, "."));
    assertEquals("a", abbreviate("abcd", 1, "."));  // should always leave at least 1 char from the original string, but trim the suffix if needed/possible
    assertEquals("", abbreviate("abcd", 0, "."));

    assertEquals("abcd", abbreviate("abcd", 5, ".."));
    assertEquals("abcd", abbreviate("abcd", 4, ".."));
    assertEquals("a..", abbreviate("abcd", 3, ".."));
    assertEquals("a.", abbreviate("abcd", 2, ".."));  // should always leave at least 1 char from the original string, but trim the suffix if needed/possible
    assertEquals("a", abbreviate("abcd", 1, ".."));
    assertEquals("", abbreviate("abcd", 0, ".."));
  }

  public void testTruncate() throws Exception {
    for (int i = 0; i < 100; i++) {
      assertNull(truncate(null, i));
    }
    for (int i = 0; i < 100; i++) {
      assertEquals("", truncate("", i));
    }
    assertEquals("", truncate("abcdef", 0));
    assertEquals("a", truncate("abcdef", 1));
    assertEquals("ab", truncate("abcdef", 2));
    assertEquals("abc", truncate("abcdef", 3));
    assertEquals("abcd", truncate("abcdef", 4));
    assertEquals("abcde", truncate("abcdef", 5));
    assertEquals("abcdef", truncate("abcdef", 6));
    assertEquals("abcdef", truncate("abcdef", 7));
    assertEquals("abcdef", truncate("abcdef", 8));
    assertEquals("abcdef", truncate("abcdef", 9));
  }

  public void testJoin() throws Exception {
    assertEquals("a,b,c,d", StringUtils.<String>join(",", "a", "b", "c", "d"));
    assertEquals("a,b,c,d", join(",", Arrays.asList("a", "b", "c", "d")));
    assertEquals("a - b - c - d", join(" - ", "a", "b", "c", "d"));
    assertEquals("a", join(" - ", "a"));
    assertEquals("", join(" - ", ""));
    assertEquals("One,Two,Three, Two,Two,Three, Three,Two,Three, One", join(",Two,Three, ", "One", "Two", "Three", "One"));  // musical counting in 3/4 ;)
    // test optionally specifying a different delimiter for the last element
    assertEquals("a, b, c, and d", join(", ", ", and ", Arrays.asList("a", "b", "c", "d").iterator()));
    assertEquals("a, b, or c", join(", ", ", or ", Arrays.asList("a", "b", "c").iterator()));
    assertEquals("a, and b", join(", ", ", and ", Arrays.asList("a", "b").iterator()));
    assertEquals("a", join(", ", ", and ", Collections.singletonList("a").iterator()));
    assertEquals("", join(", ", ", and ", Collections.emptyList().iterator()));
  }

  public void testLastIntegerInString() throws Exception {
    assertEquals(0, (int)lastIntegerInString("0"));
    assertEquals(1, (int)lastIntegerInString("1"));
    assertEquals(1, (int)lastIntegerInString("a1"));
    assertEquals(12, (int)lastIntegerInString("a12"));
    assertEquals(1, (int)lastIntegerInString("1b"));
    assertEquals(12, (int)lastIntegerInString("12b"));
    assertEquals(0, (int)lastIntegerInString("a0b"));
    assertEquals(90, (int)lastIntegerInString("a90bc"));
    assertEquals(999, (int)lastIntegerInString("ab999c"));
    assertEquals(9, (int)lastIntegerInString("a12b9c"));

    assertNull(lastIntegerInString(""));
    assertNull(lastIntegerInString("a"));
    assertNull(lastIntegerInString("abc"));
    assertNull(lastIntegerInString("aba asdf cwer &(*&!"));
  }

  public void testQuantity() throws Exception {
    assertEquals("1 second", quantity(1, "second"));
    assertEquals("2 seconds", quantity(2, "second"));
    assertEquals("0 seconds", quantity(0, "second"));
    assertEquals("-1 seconds", quantity(-1, "second"));
  }

  public void testPluralize() throws Exception {
    // 1) test regular words
    assertEquals("foo", pluralize("foo", 1));
    assertEquals("foos", pluralize("foo", 2));
    assertEquals("foos", pluralize("foo", 0));
    assertEquals("Foos", pluralize("Foo", -1));  // the result should be capitalized if the argument is
    // 2) test irregular words
    assertEquals("their", pluralize("its", -1));
    assertEquals("Their", pluralize("Its", -1)); // the result should be capitalized if the argument is
    assertEquals("are", pluralize("is", -1));
    assertEquals("Are", pluralize("Is", -1)); // the result should be capitalized if the argument is
  }

  public void testCommonPrefix() throws Exception {
    // the examples have no common prefix
    assertEquals("", commonPrefix("", ""));
    assertEquals("", commonPrefix("", "b"));
    assertEquals("", commonPrefix("a", "b"));
    assertEquals("", commonPrefix("a", ""));
    assertEquals("", commonPrefix("a", "bb"));
    assertEquals("", commonPrefix("aaa", "b"));
    assertEquals("", commonPrefix("ac", "be"));
    assertEquals("", commonPrefix("a", ""));
    assertEquals("", commonPrefix("ab", ""));
    assertEquals("", commonPrefix("", "ab"));
    assertEquals("", commonPrefix("eabc", "abcd"));
    assertEquals("", commonPrefix("ea bc", "ab cd"));
    assertEquals("", commonPrefix("ea bc", "ab cd "));
    // do some positive examples now
    assertEquals("a", commonPrefix("a", "a"));
    assertEquals("a", commonPrefix("a", "ab"));
    assertEquals("ab", commonPrefix("ab", "ab"));
    assertEquals("a", commonPrefix("a", "abc"));
    assertEquals("ab", commonPrefix("ab", "abc"));
    assertEquals("a", commonPrefix("acb", "abc"));
    assertEquals("a", commonPrefix("acbe", "abc"));
    assertEquals("ab", commonPrefix("abe", "abc"));
    assertEquals("ab", commonPrefix("abe", "abcd"));
    assertEquals("abc", commonPrefix("abc", "abc"));
    for (final String s : new String[]{"", "a", "ab"}) {
      AssertUtils.assertThrows(NullPointerException.class, new Runnable() {
        public void run() {
          commonPrefix(null, s);
        }
      });
      AssertUtils.assertThrows(NullPointerException.class, new Runnable() {
        public void run() {
          commonPrefix(s, null);
        }
      });
    }
  }

  public void testCommonSuffix() throws Exception {
    // the examples have no common suffix
    assertEquals("", commonSuffix("", ""));
    assertEquals("", commonSuffix("", "b"));
    assertEquals("", commonSuffix("a", "b"));
    assertEquals("", commonSuffix("a", ""));
    assertEquals("", commonSuffix("a", "bb"));
    assertEquals("", commonSuffix("aaa", "b"));
    assertEquals("", commonSuffix("ac", "be"));
    assertEquals("", commonSuffix("a", ""));
    assertEquals("", commonSuffix("ab", ""));
    assertEquals("", commonSuffix("", "ab"));
    assertEquals("", commonSuffix("eabc", "abcd"));
    assertEquals("", commonSuffix("ea bc", "abcd "));
    assertEquals("", commonSuffix("ea bc", "abcd e"));
    // do some positive examples now
    assertEquals("a", commonSuffix("a", "a"));
    assertEquals("b", commonSuffix("b", "ab"));
    assertEquals("ab", commonSuffix("ab", "ab"));
    assertEquals("ab", commonSuffix(" ab", "ab"));
    assertEquals("ab", commonSuffix(" ab", "1234 3ab"));
    assertEquals("c", commonSuffix("c", "abc"));
    assertEquals("abc", commonSuffix("abc", "abc"));
    assertEquals("abc", commonSuffix("1234abc", "abc"));
    assertEquals("abc", commonSuffix("abc", "1234abc"));
    assertEquals("  a", commonSuffix("acb        a", "abc  a"));
    for (final String s : new String[]{"", "a", "ab"}) {
      AssertUtils.assertThrows(NullPointerException.class, new Runnable() {
        public void run() {
          commonSuffix(null, s);
        }
      });
      AssertUtils.assertThrows(NullPointerException.class, new Runnable() {
        public void run() {
          commonSuffix(s, null);
        }
      });
    }
  }

  public void testAppendSurrounded() throws Exception {
    assertEquals("'a'", appendSurrounded(new StringBuilder(), "a", "'").toString());
    assertEquals("'a'", appendSurrounded(new StringBuilder(), 'a', "'").toString());
    assertEquals("abc1234abc", appendSurrounded(new StringBuilder(), 1234, "abc").toString());
  }

  public void testAppendArgs() throws Exception {
    assertEquals("\"a\"", appendArgs(new StringBuilder(), "a").toString());
    assertEquals("'a'", appendArgs(new StringBuilder(), 'a').toString());
    assertEquals("1", appendArgs(new StringBuilder(), 1).toString());
    assertEquals("\"a\", 1", appendArgs(new StringBuilder(), "a", 1).toString());
    assertEquals("'a', 1", appendArgs(new StringBuilder(), 'a', 1).toString());
    assertEquals("[1, 2]", appendArgs(new StringBuilder(), (Object)new int[]{1, 2}).toString());
    assertEquals("[a, 1]", appendArgs(new StringBuilder(), (Object)new Object[]{"a", 1}).toString());  // the "a" isn't quoted because the array is passed to Arrays.toString
    assertEquals("\"foo\", \"a\", 'b', 123, 45.6, [1, 2], [[foo, bar], [3.4, 5.6]]", // the "foo" and "bar" aren't quoted because the nested arrays are passed to Arrays.toString
        appendArgs(new StringBuilder(), "foo", "a", 'b', 123, 45.6, new int[]{1, 2},
            new Object[]{Arrays.asList("foo", "bar"), Arrays.asList(3.4, 5.6)}).toString());
  }

  public void testMethodCallToString() throws Exception {
    assertEquals("a(1)", methodCallToString("a", 1));
    assertEquals("a(\"b\")", methodCallToString("a", "b"));
    assertEquals("a('b')", methodCallToString("a", 'b'));
    assertEquals("foo(\"a\", 'b', 123, 45.6, [1, 2], [[foo, bar], [3.4, 5.6]])", // the "foo" and "bar" aren't quoted because the nested arrays are passed to Arrays.toString
        methodCallToString("foo", "a", 'b', 123, 45.6, new int[]{1, 2},
            new Object[]{Arrays.asList("foo", "bar"), Arrays.asList(3.4, 5.6)}));
  }

  public void testReverse() throws Exception {
    assertEquals("", reverse(""));
    assertEquals("a", reverse("a"));
    assertEquals("ba", reverse("ab"));
    assertEquals("cba", reverse("abc"));
    assertEquals("dcba", reverse("abcd"));
    assertEquals("edcba", reverse("abcde"));
  }

  public void testSubstringBefore() throws Exception {
    assertEquals("", substringBefore("", "x"));
    assertEquals("a", substringBefore("a", "x"));
    assertEquals("a", substringBefore("ax", "x"));
    assertEquals("a", substringBefore("axa", "x"));
    assertEquals("a", substringBefore("axaxa", "x"));
    assertEquals("ab", substringBefore("abxaxa", "x"));
    assertEquals("abx", substringBefore("abxaxa", "ax"));
  }

  public void testSubstringAfter() throws Exception {
    assertEquals("", substringAfter("", "x"));
    assertEquals("a", substringAfter("a", "x"));
    assertEquals("a", substringAfter("xa", "x"));
    assertEquals("a", substringAfter("axa", "x"));
    assertEquals("a", substringAfter("abxa", "x"));
    assertEquals("ab", substringAfter("abxab", "x"));
    assertEquals("b", substringAfter("abxab", "xa"));
  }

  public void testSubstringBetween() throws Exception {
    assertEquals("", substringBetween("", "x", "X"));
    assertEquals("", substringBetween("x", "x", "X"));
    assertEquals("", substringBetween("X", "x", "X"));
    assertEquals("", substringBetween("xX", "x", "X"));
    assertEquals("a", substringBetween("xaX", "x", "X"));
    assertEquals("ab", substringBetween("xabX", "x", "X"));
    assertEquals("ab", substringBetween("cxabXd", "x", "X"));
    assertEquals("abd", substringBetween("cxabd", "x", "X"));
    assertEquals("cab", substringBetween("cabXd", "x", "X"));
  }

  public void testSplitAndTrim() throws Exception {
    assertEquals(Arrays.asList("a", "b", "c"), splitAndTrim("a,b,c", ","));
    assertEquals(Arrays.asList("a", "b", "c"), splitAndTrim("  a  ,b,   c", ","));
    assertEquals(Arrays.asList("a", "b", "c"), splitAndTrim("a,  b  ,c,,", ","));
    assertEquals(Arrays.asList("a"), splitAndTrim("a", ","));
    assertEquals(Arrays.asList("a"), splitAndTrim("  a  ", ","));
    assertEquals(Arrays.asList("a"), splitAndTrim("  a , , ,,, ", ","));
    assertEquals(Collections.<String>emptyList(), splitAndTrim("", ","));
    assertEquals(Collections.<String>emptyList(), splitAndTrim("  ", ","));
    assertEquals(Collections.<String>emptyList(), splitAndTrim("  , , ,,, ", ","));
  }

  public void testSplit() throws Exception {
    // test a delimiter of length 1
    checkSplitResult(Arrays.asList("a", "b", "c"), "a,b,c", ",");
    // test a delimiter that is not a valid regex (String#split would either not work or throw exception in this case)
    checkSplitResult(Arrays.asList("a", "b", "c"), "a$b$c", "$", false);
    checkSplitResult(Arrays.asList("a", "b", "c"), "a(.b(.c", "(.", false);
    // test some delimiters of length > 1
    checkSplitResult(Arrays.asList("a", "b", "c"), "a__b__c", "__");
    checkSplitResult(Arrays.asList("a", "b", "c"), "a___b___c", "___");
    // test no delimiters
    checkSplitResult(Collections.singletonList(""), "", "_");
    checkSplitResult(Collections.singletonList("foo"), "foo", "_");
    // test repeated delimiters
    // (empty tokens should be returned in-between the delimiters, to match the functionality of String#split)
    checkSplitResult(Arrays.asList("a","","","b","","","c"), "a___b___c", "_");
    // test delimiters in the beginning and end of the string
    // (empty tokens should be returned at the beginning, but not at the end, to match the functionality of String#split)
    checkSplitResult(Arrays.asList("","","a","","","b","","","c","","",""), "__a___b___c___", "_");
  }

  /**
   * Asserts the expected result of {@link StringUtils#split(String, String)}
   * and optionally compares it against {@link String#split(String, int)} (with {@code limit = -1})
   * @param compareWithRegex whether to check the result against {@link String#split(String, int)} (with {@code limit = -1})
   */
  private static void checkSplitResult(List<String> expected, String str, String separator, boolean compareWithRegex) {
    assertEquals(expected, split(str, separator));
    if (compareWithRegex)
      assertEquals(expected, Arrays.asList(str.split(separator, -1)));
  }

  /**
   * Asserts the expected result of {@link StringUtils#split(String, String)}
   * and compares it against {@link String#split(String, int)} (with {@code limit = -1})
   */
  private static void checkSplitResult(List<String> expected, String str, String separator) {
    checkSplitResult(expected, str, separator, true);
  }

  public void testAsList() throws Exception {
    assertEquals(Collections.<Character>emptyList(), asList(""));
    assertEquals(Arrays.asList('a'), asList("a"));
    assertEquals(Arrays.asList('a', 'b'), asList("ab"));
    assertEquals(Arrays.asList('a', 'b', 'c'), asList("abc"));
  }

  public void testSorted() throws Exception {
    assertEquals("", sorted(""));
    assertEquals("x", sorted("x"));
    assertEquals("adfs", sorted("asdf"));
    assertEquals("eiopqrtuwy", sorted("qwertyuiop"));
  }

  public void testToCharacterSet() throws Exception {
    assertEquals(new ArrayList<Character>(toCharacterSet("asdfasdf")), Arrays.asList('a', 's', 'd', 'f'));
    assertEquals(new ArrayList<Character>(toCharacterSet("fasdfasdf")), Arrays.asList('f', 'a', 's', 'd'));
  }

  public void testValueToString() throws Exception {
    assertEquals("null", valueToString(null));
    assertEquals("123", valueToString(123));
    assertEquals("\"foo\"", valueToString("foo"));
  }

}