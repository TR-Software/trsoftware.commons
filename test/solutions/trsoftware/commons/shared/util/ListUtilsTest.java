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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static solutions.trsoftware.commons.shared.util.ListUtils.*;

public class ListUtilsTest extends TestCase {

  public void testSubList() {
    Integer[] arr = new Integer[]{1, 2, 3, 4, 5, 6};

    assertTrue(Arrays.equals(
        new Integer[]{1, 2, 3},
        subList(Arrays.asList(arr), 0, 3).toArray()
    ));
    assertTrue(Arrays.equals(
        new Integer[]{2, 3, 4},
        subList(Arrays.asList(arr), 1, 4).toArray()
    ));
    assertTrue(Arrays.equals(
        arr,
        subList(Arrays.asList(arr), 0, 6).toArray()
    ));
    boolean oobException = false;
    try {
      subList(Arrays.asList(arr), 0, 7);
    }
    catch (ArrayIndexOutOfBoundsException e) {
      oobException = true;
    }
    assertTrue(oobException);
  }

  public void testSafeSubList() throws Exception {
    List<Integer> list = Arrays.asList(1,2,3,4,5);
    // test some sub-lists that are equivalent to the original
    assertEquals(list, safeSubList(list, 0, 5));
    assertEquals(list, safeSubList(list, 0, 50));
    assertEquals(list, safeSubList(list, -50, 50));
    // test some sub-lists that are empty
    assertEquals(Collections.emptyList(), safeSubList(list, 0, 0));
    assertEquals(Collections.emptyList(), safeSubList(list, -50, 0));
    assertEquals(Collections.emptyList(), safeSubList(list, -50, -1));
    assertEquals(Collections.emptyList(), safeSubList(list, -5, -10));
    assertEquals(Collections.emptyList(), safeSubList(list, 5, 5));
    assertEquals(Collections.emptyList(), safeSubList(list, 5, 50));
    assertEquals(Collections.emptyList(), safeSubList(list, 5, 4));
    assertEquals(Collections.emptyList(), safeSubList(list, 3, 3));
    // now test all the valid sub-lists
    for (int i = 0; i <= list.size(); i++) {
      for (int n = i; n <=  list.size(); n++) {
        assertEquals(list.subList(i, n), safeSubList(list, i, n));
      }
    }
  }

  public void testInsertInOrder() throws Exception {
    assertEquals(
        arrayList("a"),
        insertInOrder(new ArrayList<String>(), "a"));
    assertEquals(
        arrayList("a", "b"),
        insertInOrder(arrayList("a"), "b"));
    assertEquals(
        arrayList("a", "b", "d"),
        insertInOrder(arrayList("a", "b"), "d"));
    assertEquals(
        arrayList("a", "b", "c", "d"),
        insertInOrder(arrayList("a", "b", "d"), "c"));
  }

  public void testIsSorted() throws Exception {
    assertTrue(isSorted(new ArrayList<Integer>()));
    assertTrue(isSorted(arrayList(5)));
    assertTrue(isSorted(arrayList(5, 15)));
    assertTrue(isSorted(arrayList(0, 1, 2, 3, 4)));

    assertFalse(isSorted(arrayList(0, 1, 5, 3, 4)));
    assertFalse(isSorted(arrayList(0, 1, 2, 3, 1)));
    assertFalse(isSorted(reversedCopy(arrayList(0, 1, 2, 3, 4))));
  }

  public void testArrayList() throws Exception {
    // the casts are redundant but necessary for testing purposes
    assertEquals(Arrays.asList(), (ArrayList)arrayList());
    assertEquals(Arrays.asList("a"), (ArrayList<String>)arrayList("a"));
    assertEquals(Arrays.asList("a", "b"), (ArrayList<String>)arrayList("a", "b"));
    assertEquals(Arrays.asList("a", "b", "c"), (ArrayList<String>)arrayList("a", "b", "c"));
  }

  public void testGetLast() throws Exception {
    assertEquals("a", last(Arrays.asList("a")));
    assertEquals("b", last(Arrays.asList("a", "b")));
    assertEquals("c", last(Arrays.asList("a", "b", "c")));
    AssertUtils.assertThrows(IndexOutOfBoundsException.class, new Runnable() {
      public void run() {
        last(new ArrayList<Object>());  // an empty list has no last element
      }
    });
  }

  public void testReversedCopy() throws Exception {
    assertEquals(Arrays.asList(), reversedCopy(Arrays.asList()));
    assertEquals(Arrays.asList("a"), reversedCopy(Arrays.asList("a")));
    assertEquals(Arrays.asList("b", "a"), reversedCopy(Arrays.asList("a", "b")));
    assertEquals(Arrays.asList("c", "b", "a"), reversedCopy(Arrays.asList("a", "b", "c")));
    List<String> lst = Arrays.asList("a", "b", "c");
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
      trimTail(list, i);
      assertEquals(originalList, list);
      assertEquals(19, (int)last(list));
    }
    // the list should still contain 0..19
    assertEquals(20, list.size());
    assertEquals(19, (int)last(list));

    // now actually trim the list
    trimTail(list, 15);
    assertEquals(15, list.size());
    assertEquals(14, (int)last(list));

    trimTail(list, 1);
    assertEquals(1, list.size());
    assertEquals(0, (int)last(list));

    trimTail(list, 0);
    assertTrue(list.isEmpty());
  }
}