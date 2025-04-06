package solutions.trsoftware.commons.shared.util.collections;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.ListUtils;

import java.util.*;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;
import static solutions.trsoftware.commons.shared.util.ListUtils.arrayList;

/**
 * @author Alex
 * @since 12/27/2022
 */
public class FluentListTest extends TestCase {

  /** The wrapped list */
  private ArrayList<Integer> delegate;
  /** The list wrapper */
  private FluentList<Integer> fluent;

  public void setUp() throws Exception {
    super.setUp();
    delegate = arrayList(0, 1, 2, 3, 4);
    fluent = FluentList.from(delegate);
  }

  public void testExamples() throws Exception {
    // tests the example code given in the FluentList class javadoc
    ArrayList<Integer> original = ListUtils.arrayList(0, 1, 2, 3, 4);
    FluentList<Integer> fluent = FluentList.from(original);
    assertEquals((Integer)4, fluent.get(-1));
    fluent.set(-2, 33);
    assertEquals(Arrays.asList(0, 1, 2, 33, 4), original);
    FluentList<Integer> subList = fluent.subList(-3);
    assertEquals(Arrays.asList(2, 33, 4), subList);
    assertEquals(original.subList(2, original.size()), subList);
    subList.add(-1, 333);
    assertEquals(Arrays.asList(0, 1, 2, 33, 333, 4), original);
  }

  public void testGet() throws Exception {
    // for non-negative indices should be the same as List.get
    for (int i = 0; i < fluent.size(); i++) {
      assertEquals((Integer)i, fluent.get(i));;
    }
    // otherwise should use offset from end of list
    assertEquals((Integer)4, fluent.get(-1));
    assertEquals((Integer)1, fluent.get(-4));
    assertEquals((Integer)0, fluent.get(-5));
    assertThrows(IndexOutOfBoundsException.class, () -> fluent.get(-6));
  }

  public void testSet() throws Exception {
    // for non-negative indices should be the same as List.get
    int size = fluent.size();
    for (int i = 0; i < size; i++) {
      fluent.set(i, i+1);
    }
    List<Integer> expected;
    verifyChanges(Arrays.asList(1, 2, 3, 4, 5));

    // otherwise should use offset from end of list
    for (int i = 1; i <= size + 2; i++) {
      int idx = -i;
      if (i <= size) {
        fluent.set(idx, idx);
      } else {
        assertThrows(IndexOutOfBoundsException.class, () -> fluent.set(idx, idx));
      }
    }
    verifyChanges(Arrays.asList(-5, -4, -3, -2, -1));
  }

  public void testAddAll() throws Exception {
    int size = fluent.size();
    assertThrows(IndexOutOfBoundsException.class, () -> fluent.addAll(size, Arrays.asList(5, 6)));
    assertThrows(IndexOutOfBoundsException.class, () -> fluent.addAll(-(size + 1), Arrays.asList(5, 6)));
    fluent.addAll(1, Arrays.asList(5, 6));
    verifyChanges(Arrays.asList(0, 5, 6, 1, 2, 3, 4));
    fluent.addAll(-2, Arrays.asList(7, 8));
    verifyChanges(Arrays.asList(0, 5, 6, 1, 2, 7, 8, 3, 4));
  }

  public void testAdd() throws Exception {
    int size = fluent.size();
    assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> fluent.add(size, 5));
    assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> fluent.add(-(size + 1), 5));
    fluent.add(1, 5);
    verifyChanges(Arrays.asList(0, 5, 1, 2, 3, 4));
    fluent.add(-2, 6);
    verifyChanges(Arrays.asList(0, 5, 1, 2, 6, 3, 4));
  }

  public void testRemove() throws Exception {
    int size = fluent.size();
    assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> fluent.remove(size));
    assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> fluent.remove(-(size + 1)));
    fluent.remove(1);
    verifyChanges(Arrays.asList(0, 2, 3, 4));
    fluent.remove(-2);
    verifyChanges(Arrays.asList(0, 2, 4));
  }

  public void testListIterator() throws Exception {
    ListIterator<Integer> it = fluent.listIterator(-4);  // corresponds to index 1
    assertEquals(0, it.previousIndex());
    assertEquals(1, it.nextIndex());
    assertEquals((Integer)0, it.previous());
    assertEquals(-1, it.previousIndex());
    assertEquals(0, it.nextIndex());
    assertThrows(NoSuchElementException.class, it::previous);
    assertEquals((Integer)0, it.next());
    assertEquals(0, it.previousIndex());
    assertEquals(1, it.nextIndex());
    verifyChanges(Arrays.asList(0, 1, 2, 3, 4));  // no changes made
    // now test modifying the list using the iterator
    it.set(5);
    verifyChanges(Arrays.asList(5, 1, 2, 3, 4));
    assertEquals(0, it.previousIndex());
    assertEquals(1, it.nextIndex());
    assertEquals((Integer)1, it.next());
    assertEquals(1, it.previousIndex());
    assertEquals(2, it.nextIndex());
    it.add(6);
    verifyChanges(Arrays.asList(5, 1, 6, 2, 3, 4));
    assertEquals(2, it.previousIndex());
    assertEquals(3, it.nextIndex());
    assertEquals((Integer)6, it.previous());
    it.remove();
    verifyChanges(Arrays.asList(5, 1, 2, 3, 4));
  }

  /**
   * Tests {@link FluentList#subList(int, int)}
   */
  public void testSubList() throws Exception {
    FluentList<Integer> subList = fluent.subList(-4, -2);
    assertEquals(Arrays.asList(1, 2), subList);
    subList.add(5);
    subList.clear();
    verifyChanges(Arrays.asList(0, 3, 4));
  }

  /**
   * Tests {@link FluentList#subList(int)}
   */
  public void testSubList1() throws Exception {
    assertEquals(Arrays.asList(0, 1, 2, 3, 4), fluent.subList(0));
    assertEquals(Arrays.asList(0, 1, 2, 3, 4), fluent.subList(-5));
    assertEquals(Arrays.asList(3, 4), fluent.subList(3));
    assertEquals(Arrays.asList(3, 4), fluent.subList(-2));
    assertEquals(Collections.emptyList(), fluent.subList(5));
  }

  private void verifyChanges(List<Integer> expected) {
    assertEquals(expected, fluent);
    // modifications should be reflected in the wrapped list
    assertEquals(expected, delegate);
  }
}