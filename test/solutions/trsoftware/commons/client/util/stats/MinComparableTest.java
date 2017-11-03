package solutions.trsoftware.commons.client.util.stats;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * May 12, 2009
 *
 * @author Alex
 */
public class MinComparableTest extends TestCase {

  public void testMin() throws Exception {
    MinComparable<Integer> m = new MinComparable<Integer>();
    assertNull(m.get());
    assertEquals(2, (int)m.update(2));
    assertEquals(1, (int)m.update(1));
    assertEquals(1, (int)m.update(3));
    assertEquals(1, (int)m.get());
    assertEquals(-1, (int)m.update(-1));
    assertEquals(-1, (int)m.get());
  }
  
  public void testUpdateFromCollection() throws Exception {
    MinComparable<Integer> m = new MinComparable<Integer>();
    assertEquals(1, (int)m.updateAll(intList(1, 3, 2)));
    assertEquals(1, (int)m.get());
  }

  public void testEval() throws Exception {
    assertEquals((Integer)1, MinComparable.eval(1, 3, 2));
  }

  public void testUpdateFromConstructor() throws Exception {
    MinComparable<Integer> m = new MinComparable<Integer>(intList(1, 3, 2));
    assertEquals(1, (int)m.get());
    assertEquals(0, (int)m.update(0));
    assertEquals(0, (int)m.update(4));
  }

  private List<Integer> intList(Integer... values) {
    return Arrays.<Integer>asList(values);
  }

  public void testEquals() throws Exception {
    MinComparable<Integer> m1 = new MinComparable<Integer>(intList(1, 3, 2));
    // these two lists have the same min
    MinComparable<Integer> m2 = new MinComparable<Integer>(intList(1, 3, 2, 1, 4));
    assertTrue(m1.equals(m2));
    assertEquals(m1.hashCode(), m2.hashCode());
    // the next two do not
    assertFalse(m1.equals(new MinComparable<Integer>(intList(1, 3, 2, 1, 0))));
    // the next object is not an instance of MinComparable
    assertFalse(m1.equals(new MaxComparable<Integer>(intList(1, 3, 2))));
  }

}