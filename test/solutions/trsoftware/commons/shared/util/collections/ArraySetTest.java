package solutions.trsoftware.commons.shared.util.collections;

import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.*;
import static solutions.trsoftware.commons.shared.util.SetUtils.newSet;

/**
 * @author Alex
 * @since 4/24/2025
 */
public class ArraySetTest extends BaseTestCase {

  public void testAdd() throws Exception {
    // start with an empty set
    ArraySet<Integer> set = new ArraySet<>();
    int size = 0;
    assertTrue(set.isEmpty());
    assertEquals(size, set.size());
    assertEquals(Collections.emptySet(), set);

    // add some elements
    Set<Integer> expected = new LinkedHashSet<>();
    for (int i = 0; i < 5; i++) {
      assertFalse(set.contains(i));
      assertTrue(set.add(i));
      assertEquals(++size, set.size());
      assertTrue(set.contains(i));
      expected.add(i);
      assertEquals(expected, set);
      // adding same value again should return false and set should be unchanged
      assertFalse(set.add(i));
      assertEquals(size, set.size());  // size unchanged
      assertEquals(expected, set);
    }
    assertArraysEqual(new Object[]{0, 1, 2, 3, 4}, set.toArray());
    // should be equal to a new ArraySet created from a collection of the same elements, as well as same hashCode
    assertEqualsAndHashCode(new ArraySet<>(expected), set);

    // test null elements
    assertFalse(set.contains(null));
    assertTrue(set.add(null));
    assertTrue(set.contains(null));
    assertFalse(set.add(null));
    assertArraysEqual(new Object[]{0, 1, 2, 3, 4, null}, set.toArray());

    // TODO: maybe test the internal array management (e.g. grow on add, shift on remove, etc.)
  }

  public void testRemove() throws Exception {
    Set<Integer> expected = IntStream.range(0, 20).boxed().collect(Collectors.toSet());
    ArraySet<Integer> set = new ArraySet<>(expected);
    assertEquals(expected, set);

    // remove random elements until empty
    while (!set.isEmpty()) {
      Integer x = RandomUtils.randomElement(set);
      assertTrue(set.contains(x));
      assertTrue(set.remove(x));
      assertFalse(set.contains(x));
      assertFalse(set.remove(x));  // already removed
      expected.remove(x);
      assertEquals(expected, set);
    }
  }

  public void testIterator() throws Exception {
    ArraySet<Integer> set = new ArraySet<>();
    assertFalse(set.iterator().hasNext());  // empty iterator
    Set<Integer> toAdd = newSet(0, 1, 2, 3);
    set.addAll(toAdd);
    assertSameSequence(toAdd.iterator(), set.iterator());

    Iterator<Integer> it = set.iterator();
    // test removal of elements via the iterator
    assertThrows(IllegalStateException.class, (Runnable)it::remove);  // next hasn't been called yet
    assertEquals(0, (int)it.next());
    it.remove();  // should modify the set
    assertEquals(newSet(1, 2, 3), set);
    assertEquals(1, (int)it.next());  // should return the next element in the original sequence
    // check for co-modification
    assertFalse(set.remove(0));  // already removed, no concurrent modification
    assertEquals(2, (int)it.next());
    assertTrue(set.remove(1));  // concurrent modification
    assertThrows(ConcurrentModificationException.class, it::next);
    assertThrows(ConcurrentModificationException.class, (Runnable)it::remove);
  }

}