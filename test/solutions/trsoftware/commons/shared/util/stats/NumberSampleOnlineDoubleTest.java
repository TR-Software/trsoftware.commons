package solutions.trsoftware.commons.shared.util.stats;

import static solutions.trsoftware.commons.shared.util.MathUtils.EPSILON;

/**
 * @author Alex
 * @since 8/13/2019
 */
public class NumberSampleOnlineDoubleTest extends CollectableStatsTestCase {
  
  private static final double[] inputs = new double[]{2,3,65,123,435,123,69,34,23,42,123,12,3,-123,34,-34};

  /** Since we've already unit tested controlSample, all we need to do is to compare variance with controlSample */
  private NumberSample<Double> controlSample;

  public void setUp() throws Exception {
    super.setUp();
    controlSample = new NumberSample<>();
  }

  @Override
  public void tearDown() throws Exception {
    controlSample = null;
    super.tearDown();
  }

  public void testUpdate() throws Exception {
    NumberSampleOnlineDouble numberSampleOnlineDouble = new NumberSampleOnlineDouble();
    for (double x : inputs) {
      numberSampleOnlineDouble.update(x);
      controlSample.update(x);
      // compare all the stats at each step
      assertEquals(controlSample, numberSampleOnlineDouble);
    }
  }

  public static void assertEquals(SampleStatistics<Double> controlSample, SampleStatisticsDouble actual) {
    assertEquals(controlSample.size(), actual.size());
    assertEquals(controlSample.min(), actual.min());
    assertEquals(controlSample.max(), actual.max());
    assertEquals(controlSample.mean(), actual.mean(), .01);
    assertEquals(controlSample.variance(), actual.variance(), .01);
    assertEquals(controlSample.stdev(), actual.stdev(), .01);
  }

  public void testMerge() throws Exception {
    // test a sample with a bunch of Integers; we assume it will work
    // for other numeric types if it works for Integers
    NumberSampleOnlineDouble ns = new NumberSampleOnlineDouble();
    NumberSampleOnlineDouble ns2 = new NumberSampleOnlineDouble();
    for (int i = 0; i < inputs.length; i++) {
      double x = inputs[i];
      controlSample.update(x);
      if (i < 4)  // add the first 4 values to the first instance and the rest to the second instance
        ns.update(x);
      else
        ns2.update(x);
    }
    // now merge the two instances and compare against the control
    ns.merge(ns2);
  }

  public void testInvalidInputs() throws Exception {
    NumberSampleOnlineDouble ns = new NumberSampleOnlineDouble();
    assertEquals(0d, ns.mean());
    assertEquals(0, ns.size());
    double[] badValues = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
    for (double badValue : badValues) {
      ns.update(badValue);
      // should have ignored this input
      assertEquals(0d, ns.mean());
      assertEquals(0, ns.size());
      // TODO: also check min and max!
    }
  }

  public static void assertEquals(NumberSampleOnlineDouble expected, NumberSampleOnlineDouble actual) {
    // compare each field separately within a delta, to ignore floating-point precision errors
    assertEquals(expected.size(), actual.size());
    assertEquals(expected.min(), actual.min());
    assertEquals(expected.max(), actual.max());
    assertEquals(expected.mean(), actual.mean(), EPSILON);
    assertEquals(expected.variance(), actual.variance(), EPSILON);
  }

  @Override
  public void testAsCollector() throws Exception {
    doTestAsDoubleStreamCollector(new NumberSampleOnlineDouble(), NumberSampleOnlineDouble::collectDoubleStream, NumberSampleOnlineDoubleTest::assertEquals, inputs);
  }

}