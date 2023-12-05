package solutions.trsoftware.commons.shared.util.compare;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.MathUtils;

import static solutions.trsoftware.commons.shared.util.RandomUtils.rnd;
import static solutions.trsoftware.commons.shared.util.compare.SortOrder.ASC;
import static solutions.trsoftware.commons.shared.util.compare.SortOrder.DESC;

/**
 * @author Alex
 * @since 8/9/2018
 */
public class SortOrderTest extends TestCase {

  public void testCompare() throws Exception {
    for (int i = 0; i < 100; i++) {
      Integer a = rnd().nextInt();
      Integer b = rnd().nextInt();
      int cmp = a.compareTo(b);
      assertEquals(cmp, ASC.compare(a, b));  // should be the same as the natural ordering
      assertEquals(-cmp, DESC.compare(a, b)); // should be the opposite of the natural ordering
    }
  }

  public void testCompareNull() throws Exception {

    // 1) test some non-Comparable objects
    assertEquals(0, ASC.compareNull(null, null));
    assertEquals(-1, ASC.compareNull(null, new Object()));
    assertEquals(1, ASC.compareNull(new Object(), null));
    assertEquals(0, ASC.compareNull(new Object(), new Object()));

    assertEquals(0, DESC.compareNull(null, null));
    assertEquals(1, DESC.compareNull(null, new Object()));
    assertEquals(-1, DESC.compareNull(new Object(), null));
    assertEquals(0, DESC.compareNull(new Object(), new Object()));

    // 2) test some Comparable objects
    assertEquals(0, ASC.compareNull("a", "a"));
    assertEquals(-1, ASC.compareNull("a", "b"));
    assertEquals(1, ASC.compareNull("b", "a"));
    assertEquals(0, ASC.compareNull(1, 1));
    assertEquals(-1, ASC.compareNull(1, 2));
    assertEquals(1, ASC.compareNull(2, 1));
    
    assertEquals(0, DESC.compareNull("a", "a"));
    assertEquals(-1, DESC.compareNull("b", "a"));
    assertEquals(1, DESC.compareNull("a", "b"));
    assertEquals(0, DESC.compareNull(1, 1));
    assertEquals(-1, DESC.compareNull(2, 1));
    assertEquals(1, DESC.compareNull(1, 2));
  }

  private static int sgn(int x) {
    return MathUtils.signum(x);
  }
}