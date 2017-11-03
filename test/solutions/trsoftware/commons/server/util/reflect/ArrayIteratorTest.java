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

package solutions.trsoftware.commons.server.util.reflect;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;
import solutions.trsoftware.commons.client.util.CollectionUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class ArrayIteratorTest extends TestCase {

  public void testArrayIterator() throws Exception {
    Object[][] arrays = new Object[][] {
        new Object[]{ new int[]{}, new Integer[]{}},
        new Object[]{ new int[]{1}, new Integer[]{1}},
        new Object[]{ new int[]{1, 2}, new Integer[]{1, 2}},
        new Object[]{ new int[]{1, 2, 3}, new Integer[]{1, 2, 3}}
    };
    for (Object[] array : arrays) {
      List<Integer> expectedList = Arrays.asList(((Integer[])array[1]));
      checkArrayIterator(array[0], int.class, expectedList);
      checkArrayIterator(array[1], Integer.class, expectedList);
    }
  }

  private static <A, T> void checkArrayIterator(final A arr, Class<T> componentType, List<T> expected) {
    // type-check the args
    assertTrue(arr.getClass().isArray());
    assertNotNull(componentType);
    assertEquals(componentType, arr.getClass().getComponentType());
    int length = expected.size();
    assertEquals(length, Array.getLength(arr));
    // 1) check the default constructor
    assertIteratorsEqual(expected.listIterator(), new ArrayIterator<T>(arr));
    // 2) check the constructor that starts at a particular element
    for (int i = 0; i < length; i++) {
      ArrayIterator<T> it = new ArrayIterator<T>(arr, i);
      assertEquals(i, it.nextIndex());
      assertIteratorsEqual(expected.listIterator(i), it);
    }
    // 3) check with starting index out of bounds
    for (final int badIndex : new int[]{-1, -2, length, length + 1}) {
      AssertUtils.assertThrows(ArrayIndexOutOfBoundsException.class, new Runnable() {
        @Override
        public void run() {
          ArrayIterator<T> it = new ArrayIterator<T>(arr, badIndex);
          assertFalse(it.hasNext());

        }
      });
    }
    // 4) sanity check:
    assertEquals(expected, CollectionUtils.asList(new ArrayIterator<T>(arr)));
  }

  public static <T> void assertIteratorsEqual(ListIterator<T> expected, final ArrayIterator<T> actual) {
    while (expected.hasNext() && actual.hasNext()) {
      assertEquals("Iterators differ at index " + expected.nextIndex(), expected.next(), actual.next());
    }
    assertFalse("Iterator <" + expected + "> has more elements than <" + actual + ">", expected.hasNext());
    assertFalse("Iterator <" + expected + "> has fewer elements than <" + actual + ">", actual.hasNext());
    AssertUtils.assertThrows(NoSuchElementException.class, new Runnable() {
      @Override
      public void run() {
        actual.next();
      }
    });
  }

}