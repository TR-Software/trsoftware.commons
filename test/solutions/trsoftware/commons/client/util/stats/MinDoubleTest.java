package solutions.trsoftware.commons.client.util.stats;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;

/**
 * @author Alex, 1/8/14
 */
public class MinDoubleTest extends TestCase {

  public void testCRUD() throws Exception {
    assertEquals(Double.POSITIVE_INFINITY, new MinDouble().get());
    assertEquals(0d, new MinDouble(0d).get());
    assertEquals(-1d, new MinDouble(-1d).get());
    assertEquals(1d, new MinDouble(1d).get());
    assertEquals(1d, new MinDouble(1d, 2d).get());
    assertEquals(-2d, new MinDouble(1d, 2d, -2d).get());
  }

  public void testEqualsAndHashCode() throws Exception {
    AssertUtils.assertEqualsAndHashCode(new MinDouble(), new MinDouble());
    AssertUtils.assertEqualsAndHashCode(new MinDouble(1d), new MinDouble(1d));
    AssertUtils.assertEqualsAndHashCode(new MinDouble(1d, 2d, -2d), new MinDouble(-2d));

    AssertUtils.assertNotEqualsAndHashCode(new MinDouble(), new MaxDouble());
    AssertUtils.assertNotEqualsAndHashCode(new MinDouble(1d), new MaxDouble(1d));
    AssertUtils.assertNotEqualsAndHashCode(new MinDouble(1d), new MinDouble(-1d));
    AssertUtils.assertNotEqualsAndHashCode(new MinDouble(1d, 2d, -2d), new MinDouble(1d, 2d, 2d));
  }

  public void testMerge() throws Exception {
    {
      MinDouble minDouble = new MinDouble(0d);
      minDouble.merge(new MinDouble(-1d));
      assertEquals(new MinDouble(0d, -1d), minDouble);
    }
    {
      MinDouble minDouble = new MinDouble();
      minDouble.merge(new MinDouble(1d));
      minDouble.merge(new MinDouble(2d, 3d));
      minDouble.merge(new MinDouble());
      assertEquals(new MinDouble(1d, 2d, 3d), minDouble);
    }
  }

}
