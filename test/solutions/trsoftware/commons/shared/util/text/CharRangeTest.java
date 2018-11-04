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
import solutions.trsoftware.commons.shared.testutil.AssertUtils;

import java.util.Arrays;
import java.util.Collections;

import static solutions.trsoftware.commons.shared.util.iterators.IteratorTestCase.assertIteratedElements;

/**
 * @author Alex
 * @since 12/24/2017
 */
public class CharRangeTest extends TestCase {

  public void testConstructor() throws Exception {
    // should throw an exception if max is less than min
    AssertUtils.assertThrows(new IllegalArgumentException("z > a"), new Runnable() {
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
      assertEquals(expected.charAt(i), charRange.charAt(i));
    }
  }

  public void testIterator() throws Exception {
    assertIteratedElements(Collections.singletonList('a'), new CharRange('a', 'a').iterator());
    assertIteratedElements(Arrays.asList('A', 'B'), new CharRange('A', 'B').iterator());
    assertIteratedElements(Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'), new CharRange('0', '9').iterator());
  }
}