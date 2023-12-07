/*
 * Copyright 2022 TR Software Inc.
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
 */

package solutions.trsoftware.commons.shared.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.function.IntBiFunction;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertArraysEqual;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;
import static solutions.trsoftware.commons.shared.util.ArrayUtils.*;

@SuppressWarnings("SimplifiableJUnitAssertion")
public class ArrayUtilsTest extends TestCase {

  public void testFilterIntArray() {
    assertTrue(Arrays.equals(
        new int[]{2, 5, 1},
        ArrayUtils.filter(
            new int[]{8, 2, 5, 1, 8, 9, 1000},
            item -> item <= 5)
    ));
  }

  public void testFilterStringArray() {
    assertTrue(Arrays.equals(
        new String[]{"foo", "bar", "baz"},
        filter(
            new String[]{"foo", "a", "bar", "cigar", "baz"},
            item -> item.length() == 3).toArray()
    ));
  }

  public void testIntSwap() throws Exception {
    assertTrue(Arrays.equals(new int[]{1, 2, 3},
        swap(new int[]{2, 1, 3}, 0, 1)));
  }

  public void testObjectSwap() throws Exception {
    assertTrue(Arrays.equals(new String[]{"foo", "bar", "baz"},
        swap(new String[]{"baz", "bar", "foo"}, 0, 2)));
  }

  public void testConcat() throws Exception {
    assertArraysEqual(new String[]{"a", "b", "c", "d"}, concat(
        new String[]{"a"},
        new String[]{"b", "c"},
        new String[]{},
        new String[]{"d"},
        new String[]{}
    ));
    // test some edge cases
    assertArraysEqual(new String[]{"a", "b", "c", "d"},
        concat(new String[]{"a", "b", "c", "d"}));  // only 1 input
    assertArraysEqual(new String[]{},
        concat(new String[]{}));  // empty array
    assertArraysEqual(new String[]{},
        concat(new String[]{}, new String[]{}));  // empty arrays

    //noinspection Convert2MethodRef
    assertThrows(IllegalArgumentException.class, () -> concat());
  }

  public void testAppend() throws Exception {
    assertArraysEqual(new String[]{"a", "b", "c"},
        append(new String[]{"a", "b"}, "c"));
    assertArraysEqual(new String[]{"a", "b", "c", "d"},
        append(new String[]{"a", "b"}, "c", "d"));
    // test some edge cases
    assertArraysEqual(new String[]{"a"},
        append(new String[]{}, "a"));
    assertArraysEqual(new String[]{},
        append(new String[]{}));
  }

  public void testUnbox() throws Exception {
    double[] result = unbox(new Double[]{1d, 2d, 3d});
    assertEquals(3, result.length);
    assertEquals(1d, result[0]);
    assertEquals(2d, result[1]);
    assertEquals(3d, result[2]);
  }

  public void testIndexOfChar() throws Exception {
    assertEquals(-1, indexOf(new char[]{'a', 'b', 'c'}, 'd'));
    assertEquals(-1, indexOf(new char[]{'a'}, 'd'));
    assertEquals(-1, indexOf(new char[0], 'd'));
    assertEquals(0, indexOf(new char[]{'a', 'b', 'c'}, 'a'));
    assertEquals(1, indexOf(new char[]{'a', 'b', 'c'}, 'b'));
    assertEquals(2, indexOf(new char[]{'a', 'b', 'c'}, 'c'));
    assertEquals(0, indexOf(new char[]{'a', 'b'}, 'a'));
    assertEquals(1, indexOf(new char[]{'a', 'b'}, 'b'));
    assertEquals(0, indexOf(new char[]{'a'}, 'a'));
  }

  public void testIndexOfInt() throws Exception {
    assertEquals(-1, indexOf(new int[]{1, 2, 3}, 4));
    assertEquals(-1, indexOf(new int[]{1}, 4));
    assertEquals(-1, indexOf(new int[0], 4));
    assertEquals(0, indexOf(new int[]{1, 2, 3}, 1));
    assertEquals(0, indexOf(new int[]{1, 1, 2, 3}, 1));  // should return first match if more than one
    assertEquals(1, indexOf(new int[]{1, 2, 3}, 2));
    assertEquals(2, indexOf(new int[]{1, 2, 3}, 3));
    assertEquals(0, indexOf(new int[]{1, 2}, 1));
    assertEquals(1, indexOf(new int[]{1, 2}, 2));
    assertEquals(0, indexOf(new int[]{1}, 1));
  }

  public void testIndexOfObject() throws Exception {
    assertEquals(-1, indexOf(new String[]{"a", "b", "c"}, "d"));
    assertEquals(-1, indexOf(new String[]{"a"}, "d"));
    assertEquals(-1, indexOf(new String[0], "d"));
    assertEquals(0, indexOf(new String[]{"a", "b", "c"}, "a"));
    assertEquals(0, indexOf(new String[]{"a", "a", "b", "c"}, "a"));  // should return first match if more than one
    assertEquals(1, indexOf(new String[]{"a", "b", "c"}, "b"));
    assertEquals(2, indexOf(new String[]{"a", "b", "c"}, "c"));
    assertEquals(0, indexOf(new String[]{"a", "b"}, "a"));
    assertEquals(1, indexOf(new String[]{"a", "b"}, "b"));
    assertEquals(0, indexOf(new String[]{"a"}, "a"));
    // also test null values
    assertEquals(-1, indexOf(new String[]{null}, "a"));
    assertEquals(0, indexOf(new String[]{null}, null));
    assertEquals(1, indexOf(new String[]{"a", null}, null));
    assertEquals(-1, indexOf(new String[]{"a", "b"}, null));
  }

  public void testContains() throws Exception {
    assertFalse(contains(new String[]{"a", "b", "c"}, "d"));
    assertFalse(contains(new String[]{"a"}, "d"));
    assertFalse(contains(new String[0], "d"));
    assertTrue(contains(new String[]{"a", "b", "c"}, "a"));
    assertTrue(contains(new String[]{"a", "b", "c"}, "b"));
    assertTrue(contains(new String[]{"a", "b", "c"}, "c"));
    assertTrue(contains(new String[]{"a", "b"}, "a"));
    assertTrue(contains(new String[]{"a", "b"}, "b"));
    assertTrue(contains(new String[]{"a"}, "a"));
    // also test null values
    assertFalse(contains(new String[]{null}, "a"));
    assertFalse(contains(new String[]{"a", "b"}, null));
    assertTrue(contains(new String[]{null}, null));
    assertTrue(contains(new String[]{"a", null}, null));
  }

  public void testContainsInt() throws Exception {
    assertFalse(contains(new int[]{1, 2, 3}, 4));
    assertFalse(contains(new int[]{1}, 4));
    assertFalse(contains(new int[0], 4));
    assertTrue(contains(new int[]{1, 1, 2, 3}, 1));
    assertTrue(contains(new int[]{1, 2, 3}, 2));
    assertTrue(contains(new int[]{1, 2, 3}, 3));
    assertTrue(contains(new int[]{1, 2}, 1));
    assertTrue(contains(new int[]{1, 2}, 2));
    assertTrue(contains(new int[]{1}, 1));
  }

  public void testGetLast() throws Exception {
    assertThrows(NullPointerException.class, new Runnable() {
      public void run() {
        getLast(null);
      }
    });
    assertThrows(ArrayIndexOutOfBoundsException.class, new Runnable() {
      public void run() {
        getLast(new Object[0]);
      }
    });
    assertEquals("a", getLast(new String[]{"a"}));
    assertEquals("b", getLast(new String[]{"a", "b"}));
    assertEquals("c", getLast(new String[]{"a", "b", "c"}));
  }

  public void testCheckBounds() throws Exception {
    // 1) check some cases that shouldn't throw an exception
    for (int arrayLength = 1; arrayLength < 10; arrayLength++) {
      for (int i = 0; i < arrayLength; i++) {
        checkBounds(arrayLength, i);
      }
    }
    // 2) check some cases that should throw an exception
    assertThrows(new ArrayIndexOutOfBoundsException(0), new Runnable() {
      @Override
      public void run() {
        checkBounds(0, 0);
      }
    });
    assertThrows(new ArrayIndexOutOfBoundsException(1), new Runnable() {
      @Override
      public void run() {
        checkBounds(1, 1);
      }
    });
    assertThrows(new ArrayIndexOutOfBoundsException(-1), new Runnable() {
      @Override
      public void run() {
        checkBounds(2, -1);
      }
    });
  }

  public void testFill() throws Exception {
    Integer[] expected = {0, 1, 2, 3, 4};
    Integer[] array = new Integer[expected.length];
    Integer[] result = fill(array, new Supplier<Integer>() {
      private int next;
      @Override
      public Integer get() {
        return next++;
      }
    });
    assertSame(array, result);
    assertArraysEqual(expected, result);
  }

  public void testMerge() throws Exception {
    assertArraysEqual(new String[]{"a", "b", "c", "d"}, merge(new String[]{"a", "b"}, new String[]{"c", "d"}));
    assertArraysEqual(new String[]{"a", "b", "c", "d"}, merge(new String[]{"a", "b", "c", "d"}, new String[]{}));
    assertArraysEqual(new String[]{"a", "b", "c", "d"}, merge(new String[]{}, new String[]{"a", "b", "c", "d"}));
    assertArraysEqual(new String[]{}, merge(new String[]{}, new String[]{}));
  }

  @SuppressWarnings("unchecked")
  public void testComputeIfAbsent() throws Exception {
    class Producer {
      int count;  // used to ensure that function invoked only once
    }

    // 1) test computeIfAbsent(T[], int, IntFunction<T>)
    {
      class ListProducer extends Producer implements IntFunction<List<Integer>> {
        @Override
        public List<Integer> apply(int value) {
          count++;
          return ListUtils.arrayList(value);
        }
      }
      ListProducer producer = new ListProducer();
      List<Integer>[] arr = new List[4];
      int i = 1;
      List<Integer> computedValue1 = computeIfAbsent(arr, i, producer);
      assertEquals(singletonList(i), computedValue1);
      // computed value should be inserted into arr[i]
      assertArraysEqual(new List[]{null, computedValue1, null, null}, arr);
      assertEquals(1, producer.count);
      // subsequent invocations should return the same instance without invoking the producer function again
      assertSame(computedValue1, computeIfAbsent(arr, i, producer));
      assertEquals(1, producer.count);
      assertArraysEqual(new List[]{null, computedValue1, null, null}, arr);
      // invoke again with a different index
      i = 2;
      List<Integer> computedValue2 = computeIfAbsent(arr, i, producer);
      assertEquals(singletonList(i), computedValue2);
      assertArraysEqual(new List[]{null, computedValue1, computedValue2, null}, arr);
      assertEquals(2, producer.count);
    }

    // 1) test computeIfAbsent(T[][], int, int, IntBiFunction<T>): the 2D array version
    {
      class Area2dProducer extends Producer implements IntBiFunction<Area2d> {
        @Override
        public Area2d apply(int w, int h) {
          count++;
          return new Area2d(w, h);
        }
      }
      Area2dProducer producer = new Area2dProducer();
      Area2d[][] arr = new Area2d[2][2];
      Area2d computedValue1 = computeIfAbsent(arr, 1, 1, producer);
      assertEquals(new Area2d(1, 1), computedValue1);
      // computed value should be inserted into arr[1][1]
      Area2d[][] expectedArr = new Area2d[][]{
          new Area2d[]{null, null},
          new Area2d[]{null, computedValue1},
      };
      assertArraysEqual(expectedArr, arr);
      assertEquals(1, producer.count);
      // subsequent invocations should return the same instance without invoking the producer function again
      assertSame(computedValue1, computeIfAbsent(arr, 1, 1, producer));
      assertEquals(1, producer.count);
      assertArraysEqual(expectedArr, arr);
      // invoke again with different indices
      Area2d computedValue2 = computeIfAbsent(arr, 1, 0, producer);
      assertEquals(new Area2d(1, 0), computedValue2);
      // computed value should be inserted into arr[1][0]
      expectedArr = new Area2d[][]{
          new Area2d[]{null, null},
          new Area2d[]{computedValue2, computedValue1},
      };
      assertArraysEqual(expectedArr, arr);
      assertEquals(2, producer.count);
    }


  }


}