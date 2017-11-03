package solutions.trsoftware.commons.client.util.stats;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * Oct 11, 2012
 *
 * @author Alex
 */
public class MinAndMaxComparableTest extends TestCase {

  public void testMinAndMax() throws Exception {
    MinAndMaxComparable<Integer> m = new MinAndMaxComparable<Integer>();
    assertNull(m.getMin());
    assertNull(m.getMax());

    m.update(2);
    assertEquals(2, (int)m.getMin());
    assertEquals(2, (int)m.getMax());

    m.update(1);
    assertEquals(1, (int)m.getMin());
    assertEquals(2, (int)m.getMax());

    m.update(3);
    m.update(2);
    assertEquals(1, (int)m.getMin());
    assertEquals(3, (int)m.getMax());
  }

  public void testUpdateFromCollection() throws Exception {
    MinAndMaxComparable<Integer> m = new MinAndMaxComparable<Integer>();
    m.update(intList(1, 3, 2));
    assertEquals(1, (int)m.getMin());
    assertEquals(3, (int)m.getMax());
  }

  public void testUpdateFromConstructor() throws Exception {
    MinAndMaxComparable<Integer> m = new MinAndMaxComparable<Integer>(intList(1, 3, 2));
    assertEquals(1, (int)m.getMin());
    assertEquals(3, (int)m.getMax());
  }

  private List<Integer> intList(Integer... values) {
    return Arrays.<Integer>asList(values);
  }

  public void testEquals() throws Exception {
    MinAndMaxComparable<Integer> m1 = new MinAndMaxComparable<Integer>(intList(1, 3, 2));
    // these two lists have the same min and max
    MinAndMaxComparable<Integer> m2 = new MinAndMaxComparable<Integer>(intList(1, 3, 2, 1, 2, 3, 1));
    assertTrue(m1.equals(m2));
    assertEquals(m1.hashCode(), m2.hashCode());
    // the next two do not
    MinAndMaxComparable<Integer> m3 = new MinAndMaxComparable<Integer>(intList(1, 3, 2, 1, 4));
    assertFalse(m1.equals(m3));
    assertFalse(m1.hashCode() == m3.hashCode());
    // the next object is not an instance of MinAndMaxComparable
    assertFalse(m1.equals(new MinComparable<Integer>(intList(1, 3, 2))));
  }
}