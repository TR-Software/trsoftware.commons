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

package solutions.trsoftware.commons.client.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;

import java.util.Arrays;

import static solutions.trsoftware.commons.client.util.ArrayUtils.*;

public class ArrayUtilsTest extends TestCase {

  public void testFlexibleArrayAdd() {
    assertTrue(Arrays.equals(
        new String[]{"a", "b", "c", "d"},
        flexibleArrayAdd(new String[]{"a", "b", "c"}, 3, "d")));
    assertTrue(Arrays.equals(
        new String[]{"a", "b", "c", null, "d"},
        flexibleArrayAdd(new String[]{"a", "b", "c"}, 4, "d")));
    assertTrue(Arrays.equals(
        new String[]{null, null, null, "d"},
        flexibleArrayAdd(null, 3, "d")));
    assertTrue(Arrays.equals(
        new String[]{"a", "b", "d"},
        flexibleArrayAdd(new String[]{"a", "b", "c"}, 2, "d")));
  }

  public void testFlexibleArrayPrimitiveFloat() {
    // start with an empty array and test a few base cases, and the rest will follow
    // by induction
    float[] a = new float[0];
    int i = 0;
    a = flexibleArrayAdd(a, i++, 1f);
    assertEquals(2, a.length);  // should have been grown to size max(2, ceil(1.5*length)) => 2
    assertTrue(Arrays.equals(new float[]{1f,0f}, a));

    a = flexibleArrayAdd(a, i++, 2f);
    assertEquals(2, a.length);  // should still be the same size as before
    assertTrue(Arrays.equals(new float[]{1,2}, a));

    a = flexibleArrayAdd(a, i++, 3f);
    assertEquals(3, a.length);  // should have been grown to size max(2, ceil(1.5*length)) => 3
    assertTrue(Arrays.equals(new float[]{1,2,3}, a));

    a = flexibleArrayAdd(a, i++, 4f);
    assertEquals(5, a.length);  // should have been grown to size max(2, ceil(1.5*length)) => 5
    assertTrue(Arrays.equals(new float[]{1,2,3,4,0}, a));

    a = flexibleArrayAdd(a, i++, 5f);
    assertEquals(5, a.length);  // should still be the same size as before
    assertTrue(Arrays.equals(new float[]{1,2,3,4,5}, a));

    a = flexibleArrayAdd(a, i++, 6f);
    assertEquals(8, a.length);  // should have been grown to size max(2, ceil(1.5*length)) => 8
    assertTrue(Arrays.equals(new float[]{1,2,3,4,5,6,0,0}, a));
    // we assume the rest is correct by induction
  }

  public void testFilterIntArray() {
    assertTrue(Arrays.equals(
        new int[]{2, 5, 1},
        filter(
            new int[]{8, 2, 5, 1, 8, 9, 1000},
            new Predicate<Integer>() {
              public boolean apply(Integer item) {
                return item <= 5;
              }
            })
    ));
  }

  public void testFilterStringArray() {
    assertTrue(Arrays.equals(
        new String[]{"foo", "bar", "baz"},
        filter(
            new String[]{"foo", "a", "bar", "cigar", "baz"},
            new Predicate<String>() {
              public boolean apply(String item) {
                return item.length() == 3;
              }
            }).toArray()
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
    String[] result = concat(
        new String[]{"a"},
        new String[]{"b", "c"},
        new String[]{},
        new String[]{"d"},
        new String[]{}
    );
    assertTrue(Arrays.equals(new String[]{"a", "b", "c", "d"}, result));
  }

  public void testInterleave() throws Exception {
    String[] result = interleave(
        new String[]{"a", "b"},
        new String[]{"c", "d", "e"},
        new String[]{"f"}
    );
    assertTrue(Arrays.equals(new String[]{"a", "c", "f", "b", "d", "e"}, result));
  }

  public void testUnbox() throws Exception {
    double[] result = unbox(new Double[]{1d, 2d, 3d});
    assertEquals(3, result.length);
    assertEquals(1d, result[0]);
    assertEquals(2d, result[1]);
    assertEquals(3d, result[2]);
  }

  public void testLinearSearchChar() throws Exception {
    assertEquals(-1, linearSearch(new char[]{'a', 'b', 'c'}, 'd'));
    assertEquals(-1, linearSearch(new char[]{'a'}, 'd'));
    assertEquals(-1, linearSearch(new char[0], 'd'));
    assertEquals(0, linearSearch(new char[]{'a', 'b', 'c'}, 'a'));
    assertEquals(1, linearSearch(new char[]{'a', 'b', 'c'}, 'b'));
    assertEquals(2, linearSearch(new char[]{'a', 'b', 'c'}, 'c'));
    assertEquals(0, linearSearch(new char[]{'a', 'b'}, 'a'));
    assertEquals(1, linearSearch(new char[]{'a', 'b'}, 'b'));
    assertEquals(0, linearSearch(new char[]{'a'}, 'a'));
  }

  public void testLinearSearchInt() throws Exception {
    assertEquals(-1, linearSearch(new int[]{1, 2, 3}, 4));
    assertEquals(-1, linearSearch(new int[]{1}, 4));
    assertEquals(-1, linearSearch(new int[0], 4));
    assertEquals(0, linearSearch(new int[]{1, 2, 3}, 1));
    assertEquals(1, linearSearch(new int[]{1, 2, 3}, 2));
    assertEquals(2, linearSearch(new int[]{1, 2, 3}, 3));
    assertEquals(0, linearSearch(new int[]{1, 2}, 1));
    assertEquals(1, linearSearch(new int[]{1, 2}, 2));
    assertEquals(0, linearSearch(new int[]{1}, 1));
  }

  public void testLinearSearchObject() throws Exception {
    assertEquals(-1, linearSearch(new String[]{"a", "b", "c"}, "d"));
    assertEquals(-1, linearSearch(new String[]{"a"}, "d"));
    assertEquals(-1, linearSearch(new String[0], "d"));
    assertEquals(0, linearSearch(new String[]{"a", "b", "c"}, "a"));
    assertEquals(1, linearSearch(new String[]{"a", "b", "c"}, "b"));
    assertEquals(2, linearSearch(new String[]{"a", "b", "c"}, "c"));
    assertEquals(0, linearSearch(new String[]{"a", "b"}, "a"));
    assertEquals(1, linearSearch(new String[]{"a", "b"}, "b"));
    assertEquals(0, linearSearch(new String[]{"a"}, "a"));
    // also test null values
    assertEquals(-1, linearSearch(new String[]{null}, "a"));
    assertEquals(0, linearSearch(new String[]{null}, null));
    assertEquals(1, linearSearch(new String[]{"a", null}, null));
    assertEquals(-1, linearSearch(new String[]{"a", "b"}, null));
  }

  public void testSlice() throws Exception {
    assertEquals(Arrays.asList("c", "d", "e"), slice(new String[]{"a", "b", "c", "d", "e", "f"}, 2, 4));
    assertEquals(Arrays.asList("e", "f"), slice(new String[]{"a", "b", "c", "d", "e", "f"}, 4, 5));
    assertEquals(Arrays.asList("a"), slice(new String[]{"a", "b", "c", "d", "e", "f"}, 0, 0));
  }

  public void testGetLast() throws Exception {
    AssertUtils.assertThrows(NullPointerException.class, new Runnable() {
      public void run() {
        getLast(null);
      }
    });
    AssertUtils.assertThrows(ArrayIndexOutOfBoundsException.class, new Runnable() {
      public void run() {
        getLast(new Object[0]);
      }
    });
    assertEquals("a", getLast(new String[]{"a"}));
    assertEquals("b", getLast(new String[]{"a", "b"}));
    assertEquals("c", getLast(new String[]{"a", "b", "c"}));
  }

}