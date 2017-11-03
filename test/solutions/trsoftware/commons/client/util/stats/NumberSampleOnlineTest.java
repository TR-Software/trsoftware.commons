package solutions.trsoftware.commons.client.util.stats;

import junit.framework.TestCase;

/**
 * Sep 30, 2012
 *
 * @author Alex
 */
public class NumberSampleOnlineTest extends TestCase {
  // since we've already unit tested NumberSample, all we need to do to test
  // this class is to compare its output to that of NumberSample

  public void testOnlineNumberSample() throws Exception {
    // test a sample with a bunch of Integers; we assume it will work
    // for other numeric types if it works for Integers
    NumberSampleOnline<Integer> ons = new NumberSampleOnline<Integer>();
    NumberSample<Integer> ns = new NumberSample<Integer>();
    int[] values = new int[]{2,3,65,123,435,123,69,34,23,42,123,12,3,-123,34,-34};
    for (int v : values) {
      ons.update(v);
      ns.update(v);
      // compare all the stats at each step
      assertEquals(ns.size(), ons.size());
      assertEquals(ns.min(), ons.min());
      assertEquals(ns.max(), ons.max());
      assertEquals(ns.mean(), ons.mean(), .01);
      assertEquals(ns.variance(), ons.variance(), .01);
      assertEquals(ns.stdev(), ons.stdev(), .01);
    }
  }

}