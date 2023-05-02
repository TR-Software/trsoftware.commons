/*
 * Copyright 2023 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util.collections;

import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.testutil.ComparableInt;
import solutions.trsoftware.commons.shared.util.ListUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.*;

/**
 * @author Alex
 * @since 4/13/2023
 */
public class SortedListTest extends BaseTestCase {

  private SortedList<ComparableInt> list;
  private ComparableInt[][] ints;

  public void setUp() throws Exception {
    super.setUp();
    ints = ComparableInt.createTestData(5, 3);
  }

  @Override
  public void tearDown() throws Exception {
    list = null;
    ints = null;
    super.tearDown();
  }

  public void testConstructor() throws Exception {
    // test the custom constructors that take an array or Collection (the default constructor already tested elsewhere)
    list = new SortedList<>(ints[2][1], ints[2][0], ints[1][1], ints[1][2], ints[4][1], ints[0][0]);
    // the sort performed by constructor should be stable (equal elements should not be reordered)
    assertArraysEqual(new ComparableInt[]{ints[0][0], ints[1][1], ints[1][2], ints[2][1], ints[2][0], ints[4][1]},
        list.toArray());
  }

  public void testAdd() throws Exception {

    list = new SortedList<>();
    assertTrue(add(ints[2][1]));;
    expect(ints[2][1]);
    assertTrue(add(ints[1][1]));;  // 1b should be inserted before 2b
    expect(ints[1][1], ints[2][1]);
    assertTrue(add(ints[2][0]));;  // 2a should be inserted at the end of the equivalent sublist (i.e. after 2b, which we already inserted)
    expect(ints[1][1], ints[2][1], ints[2][0]);

    // now test insertions at specific index add(int, Object)
    // a) should throw if would violate the sort order
    Runnable[] illegalArgs = new Runnable[] {
        // 3a can't be inserted anywhere but the end of list
        () -> add(0, ints[3][0]),
        () -> add(1, ints[3][0]),
        () -> add(2, ints[3][0]),
        // 0a can't be inserted anywhere but the beginning of list
        () -> add(1, ints[0][0]),
        () -> add(2, ints[0][0]),
        () -> add(3, ints[0][0]),
    };

    for (Runnable runnable : illegalArgs) {
      assertThrows(IllegalArgumentException.class, runnable);
      expect(ints[1][1], ints[2][1], ints[2][0]);  // not modified
    }


    add(0, ints[1][0]);
    expect(ints[1][0], ints[1][1], ints[2][1], ints[2][0]);
    add(0, ints[0][0]);
    expect(ints[0][0], ints[1][0], ints[1][1], ints[2][1], ints[2][0]);
    add(2, ints[1][1]);
    expect(ints[0][0], ints[1][0], ints[1][1], ints[1][1], ints[2][1], ints[2][0]);
    add(list.size()-1, ints[2][2]);  // just before the end of list
    expect(ints[0][0], ints[1][0], ints[1][1], ints[1][1], ints[2][1], ints[2][2], ints[2][0]);
    // at the end of list
    add(list.size(), ints[2][1]);
    expect(ints[0][0], ints[1][0], ints[1][1], ints[1][1], ints[2][1], ints[2][2], ints[2][0], ints[2][1]);
    add(list.size(), ints[3][0]);
    expect(ints[0][0], ints[1][0], ints[1][1], ints[1][1], ints[2][1], ints[2][2], ints[2][0], ints[2][1], ints[3][0]);

  }

  /**
   * Tests {@link SortedList#addAll(Collection)}
   */
  public void testAddAll() throws Exception {
    // test addAll(Collection); addAll(int, Collection) not supported
    int nValues = 1000;
    int nInstances = 10;
    LinkedHashMap<ComparableInt, Integer> data = createTestData(nValues, nInstances);
    list = new SortedList<>();
    assertTrue(list.addAll(data.keySet()));
    assertEquals(data.size(), list.size());
    // verify that the list is sorted and that each equivalent sublist is arranged in insertion order
    ListIterator<ComparableInt> it = list.listIterator();
    ComparableInt prev = null;
    for (int i = 0; it.hasNext(); i++) {
      assertEquals(i, it.nextIndex());
      ComparableInt next = it.next();
      assertEquals(next, list.get(i));  // might as well test the List.get(int) method here
      assertNotNull(next);
      if (prev != null) {
        if (next.isEqualTo(prev)) {
          // should be in insertion order
          assertThat(data.get(next)).isGreaterThan(data.get(prev));
        } else {
          assertThat(next).isGreaterThan(prev);
        }
      }
      prev = next;
    }
    
    // test with a non-empty list
    {
      List<ComparableInt> moreInts = arrayList(ComparableInt.createInstances(nValues + 1, 2));
      ArrayList<ComparableInt> expected = ListUtils.concat(list, moreInts);
      assertTrue(list.addAll(moreInts));
      assertEquals(expected, list);
    }
  }

  /**
   * Tests {@link SortedList#addAll(int, Collection)}
   */
  public void testAddAll2() throws Exception {
    // test addAll at specific index
    list = new SortedList<>(ints[2][1], ints[2][0]);
    expect(ints[2][1], ints[2][0]);
    // should throw if it would violate sort order
    assertThrows(IllegalArgumentException.class, () -> list.addAll(0, arrayList(ints[3][1], ints[3][0])));
    expect(ints[2][1], ints[2][0]);  // not modified
    assertThrows(IllegalArgumentException.class, () -> list.addAll(0, arrayList(ints[3][1], ints[3][0])));
    expect(ints[2][1], ints[2][0]);  // not modified
    // make sure the list isn't modified even if the first element(s) of the given collection aren't out-of-order
    assertThrows(IllegalArgumentException.class, () -> list.addAll(0, arrayList(ints[2][2], ints[3][0])));
    expect(ints[2][1], ints[2][0]);  // not modified
    // should be allowed if doesn't violate sort order
    list.addAll(0, arrayList(ints[0][1], ints[0][0], ints[2][0]));
    expect(ints[0][1], ints[0][0], ints[2][0], ints[2][1], ints[2][0]);
    list.addAll(list.size(), arrayList(ints[4][1]));
    expect(ints[0][1], ints[0][0], ints[2][0], ints[2][1], ints[2][0], ints[4][1]);
    list.addAll(list.size()-1, arrayList(ints[2][2], ints[3][0], ints[4][2]));
    expect(ints[0][1], ints[0][0], ints[2][0], ints[2][1], ints[2][0], ints[2][2], ints[3][0], ints[4][2], ints[4][1]);
  }

  public void testGet() throws Exception {
    // already tested in testAddAll and other tests
  }

  public void testIndexOf() throws Exception {
    verifyIndexOf(SortedList::indexOf, ListUtils::first);
  }

  public void testLastIndexOf() throws Exception {
    verifyIndexOf(SortedList::lastIndexOf, ListUtils::last);
  }

  /**
   * Helper for {@link #testIndexOf()} and {@link #testLastIndexOf()}
   *
   * @param method either {@link SortedList#indexOf(Object)} or {@link SortedList#lastIndexOf(Object)}
   * @param elementGetter should return the expected element from the appropriate
   *   {@linkplain #getEquivalenceClasses(SortedList) equivalence class}
   */
  private void verifyIndexOf(BiFunction<SortedList, ComparableInt, Integer> method,
                             Function<List<ListElement<ComparableInt>>, ListElement<ComparableInt>> elementGetter) {
    int nValues = 10;
    int nInstances = 5;
    LinkedHashMap<ComparableInt, Integer> data = createTestData(nValues, nInstances);
    list = new SortedList<>();
    list.addAll(data.keySet());
    Map<Integer, List<ListElement<ComparableInt>>> equivalenceClasses = getEquivalenceClasses(list);
    assertEquals(nValues, equivalenceClasses.size());
    equivalenceClasses.values().forEach(subList -> assertEquals(nInstances, subList.size()));

    for (ComparableInt item : data.keySet()) {
      List<ListElement<ComparableInt>> equivClass = equivalenceClasses.get(item.getValue());
      ComparableInt expected = elementGetter.apply(equivClass).getElement();
      int i = method.apply(list, item);
      assertTrue(i >= 0);  // found
      // should be the first occurrence in its equivalence class
      assertSame(expected, list.get(i));
    }
  }

  public void testSet() throws Exception {
    list = new SortedList<>(ints[1][1], ints[2][1], ints[2][0]);
    expect(ints[1][1], ints[2][1], ints[2][0]);
    assertThrows(IllegalArgumentException.class, () -> set(0, ints[3][0]));  // can't replace 1b with 3a
    assertThrows(IllegalArgumentException.class, () -> set(1, ints[3][0]));  // can't replace 2b with 3a
    // should be able to replace any element with any other element from the same equivalence class
    assertSame(ints[2][1], set(1, ints[2][2]));
    expect(ints[1][1], ints[2][2], ints[2][0]);
    // also with any other elements where the replacement wouldn't violate the overall sort order
    assertSame(ints[1][1], set(0, ints[0][0]));
    expect(ints[0][0], ints[2][2], ints[2][0]);
    assertSame(ints[2][0], set(2, ints[3][0]));
    expect(ints[0][0], ints[2][2], ints[3][0]);
    assertSame(ints[2][2], set(1, ints[3][1]));
    expect(ints[0][0], ints[3][1], ints[3][0]);
    assertSame(ints[0][0], set(0, ints[3][2]));
    expect(ints[3][2], ints[3][1], ints[3][0]);
    // trivial case: should be able to replace any element with itself
    for (int i = 0; i < list.size(); i++) {
      ComparableInt el = list.get(i);
      assertSame(el, set(i, el));
    }
    // lastly, test all possibilities that wouldn't violate the sort order
    System.out.println("----- Testing all values -----");
    list = new SortedList<>(ints[0]);
    for (ComparableInt[] values : ints) {
      for (int i = list.size() - 1; i >= 0; i--) {  // must set in reverse order to not violate the constraint
        ComparableInt before = list.get(i);
        // should be able to replace with any other value from the same equivalence class
        for (ComparableInt value : values) {
          assertSame(before, set(i, value));
          before = value;
        }

      }

    }
  }

  /**
   * Assert that {@link #list} contains the specified elements in the specified order.
   */
  private void expect(ComparableInt... expected) {
    assertEquals(arrayList(expected), list);
  }

  /**
   * Invokes {@link SortedList#add(int, Comparable)} with the given arguments, and prints the state before and after
   */
  private void add(int index, ComparableInt element) {
    System.out.println("Invoking " + StringUtils.methodCallToString(list + ".add", index, element));
    list.add(index, element);
    System.out.println("  -> " + list);
  }

  /**
   * Invokes {@link SortedList#add(Comparable)} with the given argument, and prints the state before and after
   * @return the value returned by {@link SortedList#add(Comparable)} with the given argument
   */
  private boolean add(ComparableInt element) {
    System.out.println("Invoking " + StringUtils.methodCallToString(list + ".add", element));
    boolean ret = list.add(element);
    System.out.println("  -> " + ret + "; " + list);
    return ret;
  }

  /**
   * Invokes {@link SortedList#set(int, Comparable)} with the given arguments, and prints the state before and after
   * @return the value returned by {@link SortedList#set(int, Comparable)} with the given arguments
   */
  private ComparableInt set(int index, ComparableInt element) {
    System.out.println("Invoking " + StringUtils.methodCallToString(list + ".set", index, element));
    ComparableInt ret = list.set(index, element);
    System.out.println("  -> " + ret + "; " + list);
    return ret;
  }

  /**
   * Invokes {@link SortedList#remove(int)} with the given argument, and prints the state before and after
   * @return the value returned by {@link SortedList#remove(int)} with the given argument
   */
  private ComparableInt remove(int index) {
    System.out.println("Invoking " + StringUtils.methodCallToString(list + ".remove", index));
    ComparableInt ret = list.remove(index);
    System.out.println("  -> " + ret + "; " + list);
    return ret;
  }

  /**
   * Invokes {@link SortedList#remove(Object)} with the given arguments, and prints the state before and after
   * @return the value returned by {@link SortedList#remove(Object)} with the given arguments
   */
  private boolean remove(ComparableInt element) {
    System.out.println("Invoking " + StringUtils.methodCallToString(list + ".remove", element));
    boolean ret = list.remove(element);
    System.out.println("  -> " + ret + "; " + list);
    return ret;
  }

  public void testRemove() throws Exception {
    list = new SortedList<>(ints[1][1], ints[2][1], ints[2][0]);
    System.out.println("list = " + list);
    expect(ints[1][1], ints[2][1], ints[2][0]);
    assertThrows(IndexOutOfBoundsException.class, () -> remove(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> remove(3));
    // 1) test remove(int)
    remove(0);
    expect(ints[2][1], ints[2][0]);
    remove(1);
    expect(ints[2][1]);
    remove(0);
    expect();
    // 2) test remove(Object)
    list = new SortedList<>(ints[1][1], ints[2][1], ints[2][0]);
    System.out.println("list = " + list);
    expect(ints[1][1], ints[2][1], ints[2][0]);
    // should remove the first occurrence as long as compareTo(o) == 0; doesn't have to be equals(o)
    assertTrue(remove(ints[2][2]));
    expect(ints[1][1], ints[2][0]);
    // list should be unchanged if no such element is found
    assertFalse(remove(ints[3][0]));
    expect(ints[1][1], ints[2][0]);
    // should throw if null
    assertThrows(NullPointerException.class, () -> remove(null));
  }

  public void testRemoveAll() throws Exception {
    list = new SortedList<>(ints[1][1], ints[1][2], ints[2][1], ints[2][0], ints[4][1]);
    expect(ints[1][1], ints[1][2], ints[2][1], ints[2][0], ints[4][1]);
    // 1) test removeAll(Collection), which uses == equality to test each element for containment in collection
    assertFalse(list.removeAll(arrayList(ints[2][2], ints[0][0])));
    expect(ints[1][1], ints[1][2], ints[2][1], ints[2][0], ints[4][1]);  // not modified
    assertTrue(list.removeAll(arrayList(ints[1][1], ints[4][1])));
    expect(ints[1][2], ints[2][1], ints[2][0]);

    // 2) test removeAll(E key), which removes all elements in same equiv. class as key (custom SortedList method, not declared in List interface)
    list = new SortedList<>(ints[1][1], ints[1][2], ints[2][1], ints[2][0], ints[4][1]);
    expect(ints[1][1], ints[1][2], ints[2][1], ints[2][0], ints[4][1]);
    assertFalse(list.removeAll(ints[0][0]));  // no equiv. elements in list
    expect(ints[1][1], ints[1][2], ints[2][1], ints[2][0], ints[4][1]);  // not modified
    assertTrue(list.removeAll(ints[2][2]));  // should remove all equivalent to ints[2]{*]
    expect(ints[1][1], ints[1][2], ints[4][1]);
  }

  public void testListIterator() throws Exception {
    list = new SortedList<>(ints[1][1], ints[2][1], ints[2][0]);
    ListIterator<ComparableInt> it = list.listIterator();
    // basic iteration already tested in testAddAll; the following tests set/add/remove using the ListIterator
    assertEquals(0, it.nextIndex());
    assertSame(ints[1][1], it.next());
    assertEquals(1, it.nextIndex());
    // can't replace out of order
    assertThrows(IllegalArgumentException.class, (Runnable)() -> it.set(ints[3][1]));
    expect(ints[1][1], ints[2][1], ints[2][0]);  // not modified
    // can replace if sort order not violated
    it.set(ints[1][2]);
    expect(ints[1][2], ints[2][1], ints[2][0]);
    assertEquals(1, it.nextIndex());
    assertSame(ints[2][1], it.next());
    assertEquals(2, it.nextIndex());
    // can't replace out of order
    assertThrows(IllegalArgumentException.class, (Runnable)() -> it.set(ints[3][1]));
    expect(ints[1][2], ints[2][1], ints[2][0]);  // not modified
    assertEquals(2, it.nextIndex());
    // can't insert out of order
    assertThrows(IllegalArgumentException.class, (Runnable)() -> it.add(ints[3][1]));
    expect(ints[1][2], ints[2][1], ints[2][0]);  // not modified
    assertEquals(2, it.nextIndex());
    // can insert:
    it.add(ints[2][2]);
    expect(ints[1][2], ints[2][1], ints[2][2], ints[2][0]);
    assertEquals(3, it.nextIndex());
    assertThrows(IllegalStateException.class, (Runnable)it::remove);  // must call next() or previous() first
    assertSame(ints[2][0], it.next());
    assertEquals(4, it.nextIndex());  // cursor at end of list
    assertFalse(it.hasNext());
    // can always remove an element (as long as preceded by a call to next/previous)
    it.remove();
    // should have removed the last element
    expect(ints[1][2], ints[2][1], ints[2][2]);
    assertEquals(3, it.nextIndex());  // still at end of list
    assertFalse(it.hasNext());
    // can't call remove() again until we call next() or previous()
    assertThrows(IllegalStateException.class, (Runnable)it::remove);
    assertSame(ints[2][2], it.previous());  // should still return the last element
    expect(ints[1][2], ints[2][1], ints[2][2]);  // not modified

    // now test listIterator(int) and concurrent modification
    ListIterator<ComparableInt> it2 = list.listIterator(1);
    assertSame(ints[2][1], it2.next());
    assertEquals(2, it2.nextIndex());
    expect(ints[1][2], ints[2][1], ints[2][2]);  // not modified yet
    // remove last elem returned by it2.next() using the 2nd iterator
    it2.remove();  // should remove ints[2][1]
    expect(ints[1][2], ints[2][2]);
    assertEquals(1, it2.nextIndex());  // should still be on the last elem of list
    // At this point, the first iterator still expects it.prev == list[1] == ints[2][1], so should throw CME
    assertEquals(1, it.previousIndex());
    assertThrows(ConcurrentModificationException.class, it::previous);
  }

  public void testIterator() throws Exception {
    list = new SortedList<>(ints[1][1], ints[2][1], ints[2][0]);
    // 1) trivial iteration:
    assertSameSequence(arrayList(ints[1][1], ints[2][1], ints[2][0]).iterator(), list.iterator());
    // 2) concurrent modification
    {
      // 2.1) should throw if modified using a different iterator
      Iterator<ComparableInt> it = list.iterator();
      Iterator<ComparableInt> it2 = list.iterator();
      assertSame(ints[1][1], it.next());
      it.remove();
      expect(ints[2][1], ints[2][0]);
      assertThrows(IllegalStateException.class, (Runnable)it::remove);  // can't remove again until it.next() is called again
      // it2 should throw CME because list was modified using the other iterator
      assertThrows(ConcurrentModificationException.class, it2::next);
      // however can still keep going using the iterator that performed the modification
      assertSame(ints[2][1], it.next());
      assertSame(ints[2][0], it.next());
      assertThrows(NoSuchElementException.class, it::next);  // end of list
    }
    {
      // 2.2) should throw if list modified directly
      Iterator<ComparableInt> it = list.iterator();
      expect(ints[2][1], ints[2][0]);
      assertTrue(list.add(ints[3][0]));
      expect(ints[2][1], ints[2][0], ints[3][0]);
      assertThrows(ConcurrentModificationException.class, it::next);
      Iterator<ComparableInt> it2 = list.iterator();
      assertTrue(it2.hasNext());
      list.clear();
      assertThrows(ConcurrentModificationException.class, it2::next);
    }
  }

  public void testSubList() throws Exception {
    // 1) test the custom SortedList.subList(E) method, which returns all elts in the same equivalence class
    {
      list = new SortedList<>(ints[0][0], ints[1][1], ints[1][2], ints[2][1], ints[2][0], ints[4][1]);
      // contained elements
      assertEquals(list.subList(0, 1), list.subList(ints[0][1]));
      assertEquals(list.subList(1, 3), list.subList(ints[1][1]));
      assertEquals(list.subList(3, 5), list.subList(ints[2][1]));
      assertEquals(list.subList(5, 6), list.subList(ints[4][1]));
      // if equiv. class not found, should return an empty sublist that allows inserting at the appropriate position
      List<ComparableInt> sub3 = list.subList(ints[3][1]);
      assertEquals(0, sub3.size());
      sub3.add(ints[3][1]);
      expect(ints[0][0], ints[1][1], ints[1][2], ints[2][1], ints[2][0], ints[3][1], ints[4][1]);
      // TODO: check some IllegalArgumentException conditions here

    }

    // 2) test the standard subList(int, int) method
    {
      list = new SortedList<>(ints[0][0], ints[1][1], ints[1][2], ints[2][1], ints[2][0], ints[4][1]);
      expect(ints[0][0], ints[1][1], ints[1][2], ints[2][1], ints[2][0], ints[4][1]);
      List<ComparableInt> sub1_3 = list.subList(1, 3);
      assertEquals(arrayList(ints[1][1], ints[1][2]), sub1_3);
      // test the sublist ops that should propagate to the underlying list, with same restrictions:

      // should be able to replace any element with any other element from the same equivalence class
      sub1_3.set(0, ints[0][1]);
      assertEquals(arrayList(ints[0][1], ints[1][2]), sub1_3);
      expect(ints[0][0], ints[0][1], ints[1][2], ints[2][1], ints[2][0], ints[4][1]);
      // shouldn't allow any operations that would cause the underlying list to become unsorted
      assertThrows(IllegalArgumentException.class, () -> sub1_3.set(0, ints[3][0]));
      assertThrows(IllegalArgumentException.class, (Runnable)() -> sub1_3.add(0, ints[3][0]));
      assertThrows(IllegalArgumentException.class, (Runnable)() ->
          sub1_3.addAll(0, arrayList(ints[0][2], ints[3][0])));
      assertEquals(arrayList(ints[0][1], ints[1][2]), sub1_3);  // not modified
      expect(ints[0][0], ints[0][1], ints[1][2], ints[2][1], ints[2][0], ints[4][1]);  // not modified
    }

  }

  public void testSize() throws Exception {
    list = new SortedList<>();
    assertEquals(0, list.size());
    List<ComparableInt> elements = Arrays.stream(ints).flatMap(Arrays::stream).collect(Collectors.toList());
    for (int i = 0; i < elements.size(); i++) {
      assertEquals(i, list.size());
      ComparableInt el = elements.get(i);
      list.add(el);
      assertEquals(i+1, list.size());
      assertEquals(elements.subList(0, i+1), list);
    }
    assertEquals(elements.size(), list.size());
  }

  public void testClear() throws Exception {
    list = new SortedList<>(ints[1][1], ints[2][1], ints[2][0]);
    expect(ints[1][1], ints[2][1], ints[2][0]);
    list.clear();
    expect();
    list.add(ints[3][0]);
    expect(ints[3][0]);
  }

  public void testContains() throws Exception {
    list = new SortedList<>(ints[0][1], ints[1][1], ints[1][0]);
    assertTrue(list.contains(ints[1][1]));  // trivially true
    // also true for any other element in same equiv. class, even if not actually in the list
    assertTrue(list.contains(ints[1][2]));
    assertTrue(list.contains(ints[0][2]));
    // false for any other elements that don't compare as equals
    for (int i = 2; i < ints.length; i++) {
      ComparableInt[] others = ints[i];
      for (ComparableInt other : others) {
        assertFalse(list.contains(other));
      }
    }
  }

  public void testSort() throws Exception {
    list = new SortedList<>(ints[0][1], ints[1][1], ints[1][0]);
    // should always throw, regardless of comparator
    assertThrows(UnsupportedOperationException.class, (Runnable)() -> list.sort(Comparator.naturalOrder()));
    assertThrows(UnsupportedOperationException.class, (Runnable)() -> list.sort(Comparator.reverseOrder()));
  }

  public void testToArray() throws Exception {
    ComparableInt[] elements = {ints[0][1], ints[1][1], ints[1][0]};
    list = new SortedList<>(elements);
    // should allocate a new array:
    {
      Object[] ret = list.toArray();
      assertArraysEqual(elements, ret);
      assertNotSame(elements, ret);
    }
    {
      ComparableInt[] arg = new ComparableInt[0];
      ComparableInt[] ret = list.toArray(arg);
      assertArraysEqual(elements, ret);
      assertNotSame(ret, arg);
    }
    // should reuse the same array if the list fits:
    {
      Object[] arg = new Object[3];
      Object[] ret = list.toArray(arg);
      // should reuse the same array if the list fits
      assertArraysEqual(elements, ret);
      assertSame(arg, ret);
    }
    // if given array is longer, should set the arg[list.size()] to null
    {
      ComparableInt[] arg = {ints[1][2], ints[2][1], ints[2][0], ints[3][0], ints[3][1]};
      ComparableInt[] ret = list.toArray(arg);
      assertArraysEqual(new ComparableInt[]{ints[0][1], ints[1][1], ints[1][0], null, ints[3][1]}, ret);
      assertSame(arg, ret);
    }
    //noinspection SuspiciousToArrayCall
    assertThrows(ArrayStoreException.class, () -> list.toArray(new String[0]));
    //noinspection ConstantConditions
    assertThrows(NullPointerException.class, () -> list.toArray(null));
  }

  /**
   * @return {@code Arrays.asList(a)}
   * @see Arrays#asList(Object[])
   */
  @SafeVarargs
  public static <T> List<T> arrayList(T... a) {
    return Arrays.asList(a);
  }

  /**
   * Creates a list of {@code nInstances} of {@link ComparableInt} for each integer in range {@code [0, nValues)},
   * shuffles that list, and returns a mapping of each {@link ComparableInt} instance to its index in the shuffled list.
   * The iteration order of the map entries is the same as that of the shuffled list.  The key set of this map
   * can be used to construct a {@link SortedList} and the mappings can be used to validate the insertion order in that
   * sorted list.  Another way to look at it is that the value mapped by each key is a sequential ID of each element.
   *
   * @see ComparableInt#createInstances(int, int)
   */
  @Nonnull
  public static LinkedHashMap<ComparableInt, Integer> createTestData(int nValues, int nInstances) {
    ComparableInt[][] data = ComparableInt.createTestData(nValues, nInstances);
    List<ComparableInt> items = Arrays.stream(data).flatMap(Arrays::stream).collect(Collectors.toList());
    Collections.shuffle(items);
    LinkedHashMap<ComparableInt, Integer> indexOf = new LinkedHashMap<>();  // the index of each item in the list to be passed to addAll(Collection)
    for (int i = 0; i < items.size(); i++) {
      indexOf.put(items.get(i), i);
    }
    return indexOf;
  }

  private Map<Integer, List<ListElement<ComparableInt>>> getEquivalenceClasses(SortedList<ComparableInt> list) {
    // TODO: maybe provide a similar method in SortedList?
    // TODO: maybe don't need to wrap members with ListElement?
    DefaultMap<Integer, List<ListElement<ComparableInt >>> ret = DefaultMap.fromSupplier(new TreeMap<>(), ArrayList::new);
    for (int i = 0; i < list.size(); i++) {
      ComparableInt el = list.get(i);
      ret.get(el.getValue()).add(new ListElement<>(el, i));
    }
    {
      // TODO: temp
      System.out.println("Equivalence classes:");
      for (Map.Entry<Integer, List<ListElement<ComparableInt>>> entry : ret.entrySet()) {
        System.out.println("  " + entry.getKey() + ": " + entry.getValue());
      }
    }
    return ret;
  }

  private static class ListElement<E> {
    // TODO: this class probably unnecessary
    /** A list element */
    private final E element;
    /** The element's index in the list */
    private final int index;

    public ListElement(E element, int index) {
      this.index = index;
      this.element = element;
    }

    public int getIndex() {
      return index;
    }

    public E getElement() {
      return element;
    }

    @Override
    public String toString() {
      return StringUtils.tupleToString(element, index);
    }
  }
}