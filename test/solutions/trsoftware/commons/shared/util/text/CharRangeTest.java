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

package solutions.trsoftware.commons.shared.util.text;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.*;
import static solutions.trsoftware.commons.shared.util.iterators.IteratorTestCase.assertIteratedElements;

/**
 * @author Alex
 * @since 12/24/2017
 */
public class CharRangeTest extends TestCase {

  public void testConstructor() throws Exception {
    // should throw an exception if max is less than min
    assertThrows(new IllegalArgumentException("z > a"), new Runnable() {
      @Override
      public void run() {
        new CharRange('z', 'a');
      }
    });
  }

  public void testRanges() throws Exception {
    validate(new CharRange('a', 'a'), "a");
    validate(new CharRange('A', 'B'), "AB");
    validate(new CharRange('0', '9'), "0123456789");
  }


  private static void validate(CharRange charRange, String expected) {
    assertEquals(expected, charRange.toString());
    assertEquals(expected.length(), charRange.length());
    for (int i = 0; i < expected.length(); i++) {
      char c = expected.charAt(i);
      assertEquals(c, charRange.charAt(i));
      assertEquals(i, charRange.indexOf(c));
      assertTrue(charRange.contains(c));
    }
  }

  public void testIterator() throws Exception {
    assertIteratedElements(Collections.singletonList('a'), new CharRange('a', 'a').iterator());
    assertIteratedElements(Arrays.asList('A', 'B'), new CharRange('A', 'B').iterator());
    assertIteratedElements(Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'), new CharRange('0', '9').iterator());
  }

  public void testSubSequence() throws Exception {
    final CharRange range = new CharRange('a', 'z');
    final int len = range.length();
    // test some examples manually
    assertEquals(new CharRange('a', 'a'), range.subSequence(0, 1));
    assertEquals(new CharRange('b', 'c'), range.subSequence(1, 3));
    // test some invalid start/end pairs
    assertThrows(IndexOutOfBoundsException.class, () -> range.subSequence(-1, 0));
    assertThrows(IndexOutOfBoundsException.class, () -> range.subSequence(-5, 6));
    assertThrows(IndexOutOfBoundsException.class, () -> range.subSequence(1, -6));
    assertThrows(IndexOutOfBoundsException.class, () -> range.subSequence(1, len+1));
    // test all the valid start/end pairs
    for (int start = 0; start < len; start++) {
      for (int end = start; end < len; end++) {
        CharSequence sub = range.subSequence(start, end);
        int subLen = sub.length();
        assertEquals(end - start, subLen);
        if (subLen > 0) {
          // the result should also be an instance of CharRange if possible (i.e. when result has a nonzero length)
          assertTrue(sub instanceof CharRange);
        }
        assertEquals(range.toString().substring(start, end), sub.toString());
      }
    }
  }

  public void testEqualsAndHashCode() throws Exception {
    assertEqualsAndHashCode(new CharRange('a', 'a'), new CharRange('a', 'a'));
    assertEqualsAndHashCode(new CharRange('A', 'B'), new CharRange('A', 'B'));
    assertEqualsAndHashCode(new CharRange('0', '9'), new CharRange('0', '9'));
    assertNotEqual(new CharRange('A', 'B'), new CharRange('A', 'C'));
  }

  public void testIndexOf() throws Exception {
    CharRange ad = new CharRange('a', 'd');
    assertEquals(-1, ad.indexOf('A'));
    assertEquals(0, ad.indexOf('a'));
    assertEquals(1, ad.indexOf('b'));
    assertEquals(2, ad.indexOf('c'));
    assertEquals(3, ad.indexOf('d'));
    assertEquals(-1, ad.indexOf('e'));
  }

  public void testContains() throws Exception {
    CharRange ad = new CharRange('a', 'd');
    assertFalse(ad.contains('A'));
    assertTrue(ad.contains('a'));
    assertTrue(ad.contains('b'));
    assertTrue(ad.contains('c'));
    assertTrue(ad.contains('d'));
    assertFalse(ad.contains('e'));
  }
}