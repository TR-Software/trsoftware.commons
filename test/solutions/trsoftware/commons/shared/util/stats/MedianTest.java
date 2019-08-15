package solutions.trsoftware.commons.shared.util.stats;

import junit.framework.TestCase;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertEqualsAndHashCode;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertNotEqual;
import static solutions.trsoftware.commons.shared.util.MathUtils.EPSILON;

/**
 * @author Alex
 * @since 8/13/2019
 */
public class MedianTest extends TestCase {

  public void testGetLower() throws Exception {
    for (int i = -10; i < 10; i++) {
      assertEquals((Integer)i, new Median<>(i).getLower());
      assertEquals((Integer)i, new Median<>(i, i).getLower());
      assertEquals((Integer)i, new Median<>(i, i + 1).getLower());
    }
  }

  public void testGetUpper() throws Exception {
    for (int i = -10; i < 10; i++) {
      assertEquals((Integer)i, new Median<>(i).getUpper());
      assertEquals((Integer)i, new Median<>(i, i).getUpper());
      assertEquals((Integer)(i+1), new Median<>(i, i + 1).getUpper());
    }
  }

  public void testGetValue() throws Exception {
    for (int i = -10; i < 10; i++) {
      assertEquals((Integer)i, new Median<>(i).getValue());
      assertEquals((Integer)i, new Median<>(i, i).getValue());
      assertEquals((Integer)(i+1), new Median<>(i, i + 1).getValue());
    }
  }

  public void testIsUnique() throws Exception {
    for (int i = -10; i < 10; i++) {
      assertTrue(new Median<>(i).isUnique());
      assertTrue(new Median<>(i, i).isUnique());
      assertFalse(new Median<>(i, i+1).isUnique());
    }
  }

  public void testInterpolate() throws Exception {
    assertEquals(1, new Median<>(1).interpolate(), EPSILON);
    assertEquals(1, new Median<>(1, 1).interpolate(), EPSILON);
    assertEquals(1, new Median<>(0, 2).interpolate(), EPSILON);
    assertEquals(-1, new Median<>(-2, 0).interpolate(), EPSILON);
    assertEquals(1.5, new Median<>(1, 2).interpolate(), EPSILON);
    assertEquals(-1.5, new Median<>(-1, -2).interpolate(), EPSILON);
    assertEquals(0, new Median<>(-5, 5).interpolate(), EPSILON);
    assertEquals(2.5, new Median<>(-10, 15).interpolate(), EPSILON);
  }

  public void testEqualsAndHashCode() {
    Median<Integer> median00 = new Median<>(0, 0);
    Median<Integer> median12 = new Median<>(1, 2);
    assertEqualsAndHashCode(median00, new Median<>(0, 0));
    assertNotEqual(median00, median12);
    assertEqualsAndHashCode(median12, new Median<>(1, 2));

    Median<Integer> last = null;
    for (int i = -10; i < 10; i++) {
      for (int j = -10; j < 10; j++) {
        Median<Integer> m = new Median<>(i, j);
        Median<Integer> mCopy = new Median<>(i, j);
        assertEqualsAndHashCode(m, mCopy);
        if (i == j)
          assertEqualsAndHashCode(m, new Median<>(i));
        if (last != null)
          assertNotEqual(last, m);
        last = m;
      }
    }
  }
}