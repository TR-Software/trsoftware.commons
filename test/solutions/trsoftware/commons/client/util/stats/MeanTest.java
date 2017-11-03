package solutions.trsoftware.commons.client.util.stats;
/**
 *
 * Date: Nov 26, 2008
 * Time: 7:12:32 PM
 * @author Alex
 */

import junit.framework.TestCase;

public class MeanTest extends TestCase {

  public void testMean() throws Exception {
    Mean<Integer> mean = new Mean<Integer>();
    assertEquals(0, mean.getNumSamples());
    assertEquals(0.0, mean.getMean());

    mean.update(1);
    assertEquals(1, mean.getNumSamples());
    assertEquals(1.0, mean.getMean());

    mean.update(2);
    assertEquals(2, mean.getNumSamples());
    assertEquals((1d + 2d) / 2, mean.getMean());

    mean.update(3);
    assertEquals(3, mean.getNumSamples());
    assertEquals((1d + 2d + 3d) / 3, mean.getMean());
  }

  /** Makes sure that a non-parametrized Mean instance can handle multiple kinds of Number inputs at once */
  public void testMeanWithDifferentDataTypes() throws Exception {
    Mean<Number> mean = new Mean<Number>();
    assertEquals(0, mean.getNumSamples());
    assertEquals(0.0, mean.getMean());

    mean.update(1);
    assertEquals(1, mean.getNumSamples());
    assertEquals(1.0, mean.getMean());

    mean.update(2L);
    assertEquals(2, mean.getNumSamples());
    assertEquals((1d + 2d) / 2, mean.getMean());

    mean.update(3f);
    assertEquals(3, mean.getNumSamples());
    assertEquals((1d + 2d + 3d) / 3, mean.getMean());

    mean.update(4d);
    assertEquals(4, mean.getNumSamples());
    assertEquals((1d + 2d + 3d + 4d) / 4, mean.getMean());
  }
}