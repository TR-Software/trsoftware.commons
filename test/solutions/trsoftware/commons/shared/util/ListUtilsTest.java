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

package solutions.trsoftware.commons.shared.util;

import com.google.common.collect.ImmutableList;
import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.testutil.ComparableInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.lenientFormat;
import static java.util.Arrays.asList;
import static solutions.trsoftware.commons.shared.util.ListUtils.*;

public class ListUtilsTest extends TestCase {

  /**
   * Tests {@link ListUtils#copyOfRange(List, int, int)}
   */
  public void testCopyOfRange() {
    List<Integer> list = asList(1, 2, 3, 4, 5, 6);

    assertEquals(asList(1, 2, 3), copyOfRange(list, 0, 3));
    assertEquals(asList(2, 3, 4), copyOfRange(list, 1, 4));
    assertEquals(list, copyOfRange(list, 0, 6));
    AssertUtils.assertThrows(IndexOutOfBoundsException.class, () -> copyOfRange(list, 0, 7));

    // test that it returns a copy of the range rather than a view of the original list:
    List<Integer> newList = copyOfRange(list, 0, 3);
    assertEquals(1, (int)list.get(0));
    assertEquals(1, (int)newList.get(0));
    newList.set(0, 10);
    assertEquals(10, (int)newList.get(0));
    // the above operation shouldn't have modified the original list
    assertEquals(1, (int)list.get(0));
  }

  public void testSafeSubList() throws Exception {
    List<Integer> list = asList(1,2,3,4,5);
    // test some sub-lists that are equivalent to the original:
    assertEquals(list, safeSubList(list, 0, 5));
    assertEquals(list, safeSubList(list, 0, 50));
    assertEquals(list, safeSubList(list, -50, 50));
    // test some sub-lists that are empty:
    assertEquals(Collections.emptyList(), safeSubList(list, 0, 0));
    assertEquals(Collections.emptyList(), safeSubList(list, -50, 0));
    assertEquals(Collections.emptyList(), safeSubList(list, -50, -1));
    assertEquals(Collections.emptyList(), safeSubList(list, -5, -10));
    assertEquals(Collections.emptyList(), safeSubList(list, 5, 5));
    assertEquals(Collections.emptyList(), safeSubList(list, 5, 50));
    assertEquals(Collections.emptyList(), safeSubList(list, 5, 4));
    assertEquals(Collections.emptyList(), safeSubList(list, 3, 3));
    // test some typical usages:
    assertEquals(asList(3,4,5), safeSubList(list, 2, 50));
    // now test all the valid sub-lists
    for (int i = 0; i <= list.size(); i++) {
      for (int n = i; n <=  list.size(); n++) {
        assertEquals(list.subList(i, n), safeSubList(list, i, n));
      }
    }
  }

  public void testInsertInOrder() throws Exception {
    // 1) insertInOrder(java.util.List<T>, T)
    assertEquals(
        arrayList("a"),
        insertInOrder(arrayList(), "a"));
    assertEquals(
        arrayList("a", "b"),
        insertInOrder(arrayList("a"), "b"));
    assertEquals(
        arrayList("a", "b", "d"),
        insertInOrder(arrayList("a", "b"), "d"));
    assertEquals(
        arrayList("a", "b", "c", "d"),
        insertInOrder(arrayList("a", "b", "d"), "c"));

    // 2) insertInOrder(java.util.List<T>, T, int): with maxSize arg
    ComparableInt[][] i = ComparableInt.createTestData(5, 3);
    ArrayList<ComparableInt> list = arrayList(i[1][1], i[2][1], i[2][0]);

    assertTrue(insertInOrder(list, i[0][1], 5));
    assertEquals(asList(i[0][1], i[1][1], i[2][1], i[2][0]), list);

    assertTrue(insertInOrder(list, i[3][0], 5));
    assertEquals(asList(i[0][1], i[1][1], i[2][1], i[2][0], i[3][0]), list);

    // list should not be modified if it's full and new element is greater than its last element
    assertFalse(insertInOrder(list, i[3][0], 5));
    assertEquals(asList(i[0][1], i[1][1], i[2][1], i[2][0], i[3][0]), list);  // not modified
    // last element should be evicted if new elem inserted and list overflows maxSize as a result
    assertTrue(insertInOrder(list, i[1][0], 5));
    assertEquals(asList(i[0][1], i[1][1], i[1][0], i[2][1], i[2][0]), list);  // former last element evicted

    // given list size > maxSize + 1: should be trimmed to maxSize regardless, but only if new element was inserted
    assertFalse(insertInOrder(list, i[2][2], 3));  // should neither insert elem nor trim the list down to 3
    assertEquals(asList(i[0][1], i[1][1], i[1][0], i[2][1], i[2][0]), list);  // not modified

    assertTrue(insertInOrder(list, i[0][0], 3));  // should insert elem and trim size from 5 to 3
    assertEquals(asList(i[0][1], i[0][0], i[1][1]), list);
  }

  public void testFindInsertionPoint() throws Exception {
    int nValues = 5;
    int nDuplicates = 3;  // the number of different ComparableInt to create for each unique int value
    ComparableInt[][] ints = ComparableInt.createTestData(nValues, nDuplicates);

    {
      List<ComparableInt> list = new ArrayList<>();
      List<ComparableInt> expectedList = new ArrayList<>();
      int expectedInsPoint = 0;
      int valueToSkip = 2;
      for (int n = 0; n < ints.length; n++) {
        if (n == valueToSkip) continue;
        ComparableInt[] instances = ints[n];  // different instances of ComparableInt(n)
        for (ComparableInt value : instances) {
          // the next instance of of ComparableInt(n) should be inserted at the end of all such instances already inserted
          expectedList.add(value);
          verifyInsertionPoint(list, value, expectedInsPoint++, expectedList);
        }
      }
      // at this point we've inserted all instances of 0,1,3, and 4; now try inserting all the inst
      // now test some insertion points in the middle of the list
      expectedInsPoint = valueToSkip * nDuplicates;
      for (ComparableInt skippedValue : ints[valueToSkip]) {
        expectedList.add(expectedInsPoint, skippedValue);
        verifyInsertionPoint(list, skippedValue, expectedInsPoint++, expectedList);
      }
      // sanity check: at this point, the list should contain all of our data in its natural order
      assertEquals(
          Arrays.stream(ints).flatMap(Arrays::stream).collect(Collectors.toList()),
          list);

      // now try inserting a duplicate value somewhere in the middle
      ComparableInt toInsert = ints[0][0];  // 0a
      expectedInsPoint = ints[0].length;  // should go after 0a,0b,0c
      expectedList.add(expectedInsPoint, toInsert);
      verifyInsertionPoint(list, toInsert, expectedInsPoint, expectedList);
      // sanity check: the list should now start with 0a, 0b, 0c, 0a, 1a
      assertEquals(asList(ArrayUtils.append(ints[0], toInsert, ints[1][0])), list.subList(0, expectedInsPoint+2));

      // should throw NPE if the key is null
      AssertUtils.assertThrows(NullPointerException.class, () -> findInsertionPoint(list, null));
    }
  }

  /**
   * Helper for {@link #testFindInsertionPoint()}.  Invokes {@link ListUtils#findInsertionPoint(List, Comparable)}
   * on the given list and key and adds the key to the list at the returned insertion point.
   *
   * @param expectedInsertionPoint the expected return value of {@link ListUtils#findInsertionPoint(List, Comparable)}
   * @param expectedElements the expected state of the list after inserting the key
   * @return the result of {@code findInsertionPoint(list, key)}
   */
  private int verifyInsertionPoint(List<ComparableInt> list, ComparableInt key, int expectedInsertionPoint, List<ComparableInt> expectedElements) {
    int i = findInsertionPoint(list, key);
    list.add(i, key);
    System.out.println(lenientFormat("%s inserted at index %s: %s", key, i, list));
    assertEquals(expectedInsertionPoint, i);
    assertEquals(expectedElements, list);
    return i;
  }

  public void testIsSorted() throws Exception {
    assertTrue(isSorted(new ArrayList<Integer>()));
    assertTrue(isSorted(arrayList(5)));
    assertTrue(isSorted(arrayList(5, 15)));
    assertTrue(isSorted(arrayList(0, 1, 2, 3, 4)));

    assertFalse(isSorted(arrayList(0, 1, 5, 3, 4)));
    assertFalse(isSorted(arrayList(0, 1, 2, 3, 1)));
    assertFalse(isSorted(reversedCopy(arrayList(0, 1, 2, 3, 4))));

    /*
        TODO: test lists with duplicate values (see solutions.trsoftware.commons.shared.util.CollectionUtilsTest.testIsSorted)
        - or maybe get rid of this test altogether, since the implementation delegates to CollectionUtils
     */

  }

  public void testArrayList() throws Exception {
    // the casts are redundant but necessary for testing purposes
    assertEquals(asList(), (ArrayList)arrayList());
    assertEquals(asList("a"), (ArrayList<String>)arrayList("a"));
    assertEquals(asList("a", "b"), (ArrayList<String>)arrayList("a", "b"));
    assertEquals(asList("a", "b", "c"), (ArrayList<String>)arrayList("a", "b", "c"));
  }

  public void testGetLast() throws Exception {
    assertEquals("a", last(asList("a")));
    assertEquals("b", last(asList("a", "b")));
    assertEquals("c", last(asList("a", "b", "c")));
    AssertUtils.assertThrows(IndexOutOfBoundsException.class, new Runnable() {
      public void run() {
        last(new ArrayList<Object>());  // an empty list has no last element
      }
    });
  }

  public void testReversedCopy() throws Exception {
    assertEquals(asList(), reversedCopy(asList()));
    assertEquals(asList("a"), reversedCopy(asList("a")));
    assertEquals(asList("b", "a"), reversedCopy(asList("a", "b")));
    assertEquals(asList("c", "b", "a"), reversedCopy(asList("a", "b", "c")));
    List<String> lst = asList("a", "b", "c");
    assertNotSame(lst, reversedCopy(lst));  // make sure that a copy of the list was made
  }

  public void testTrimTail() throws Exception {
    List<Integer> list = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      list.add(i);
    }
    List<Integer> originalList = ImmutableList.copyOf(list);

    for (int i = list.size(); i < list.size() + 3; i++) {
      // shouldn't modify the list if maxSize >= actual size
      assertFalse(trimTail(list, i));
      assertEquals(originalList, list);
      assertEquals(19, (int)last(list));
    }
    // the list should still contain 0..19
    assertEquals(20, list.size());
    assertEquals(19, (int)last(list));

    // now actually trim the list
    assertTrue(trimTail(list, 15));
    assertEquals(15, list.size());
    assertEquals(14, (int)last(list));

    assertTrue(trimTail(list, 1));
    assertEquals(1, list.size());
    assertEquals(0, (int)last(list));

    assertTrue(trimTail(list, 0));
    assertTrue(list.isEmpty());
  }
}