package solutions.trsoftware.commons.client.util.stats;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * May 12, 2009
 *
 * @author Alex
 */
public class MaxComparableTest extends TestCase {

  public void testMax() throws Exception {
    MaxComparable<Integer> m = new MaxComparable<Integer>();
    assertNull(m.get());
    assertEquals(2, (int)m.update(2));
    assertEquals(2, (int)m.update(1));
    assertEquals(3, (int)m.update(3));
    assertEquals(3, (int)m.get());
    assertEquals(3, (int)m.update(2));
    assertEquals(3, (int)m.get());
  }

  public void testUpdateFromCollection() throws Exception {
    MaxComparable<Integer> m = new MaxComparable<Integer>();
    assertEquals(3, (int)m.updateAll(intList(1, 3, 2)));
    assertEquals(3, (int)m.get());
  }

  public void testEval() throws Exception {
    assertEquals((Integer)3, MaxComparable.eval(1, 3, 2));
  }

  public void testUpdateFromConstructor() throws Exception {
    MaxComparable<Integer> m = new MaxComparable<Integer>(intList(1, 3, 2));
    assertEquals(3, (int)m.get());
    assertEquals(3, (int)m.update(0));
    assertEquals(4, (int)m.update(4));
  }

  private List<Integer> intList(Integer... values) {
    return Arrays.<Integer>asList(values);
  }

  public void testEquals() throws Exception {
    MaxComparable<Integer> m1 = new MaxComparable<Integer>(intList(1, 3, 2));
    // these two lists have the same max
    MaxComparable<Integer> m2 = new MaxComparable<Integer>(intList(1, 3, 2, 1, 0));
    assertTrue(m1.equals(m2));
    assertEquals(m1.hashCode(), m2.hashCode());
    // the next two do not
    assertFalse(m1.equals(new MaxComparable<Integer>(intList(1, 3, 2, 1, 4))));
    // the next object is not an instance of MaxComparable
    assertFalse(m1.equals(new MinComparable<Integer>(intList(1, 3, 2))));
  }
}