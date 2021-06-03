/*
 * Copyright 2018 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.shared.util.stats;

import static solutions.trsoftware.commons.shared.util.MathUtils.EPSILON;

/**
 * Sep 30, 2012
 *
 * @author Alex
 */
public class NumberSampleOnlineTest extends CollectableStatsTestCase {
  // since we've already unit tested NumberSample, all we need to do to test
  // this class is to compare its output to that of NumberSample

  public void testOnlineNumberSample() throws Exception {
    // test a sample with a bunch of Integers; we assume it will work
    // for other numeric types if it works for Integers
    NumberSampleOnline<Integer> ons = new NumberSampleOnline<Integer>();
    NumberSample<Integer> ns = new NumberSample<Integer>();
    assertEquals(ns, ons);
    int[] values = new int[]{2,3,65,123,435,123,69,34,23,42,123,12,3,-123,34,-34};
    for (int v : values) {
      ons.update(v);
      ns.update(v);
      // compare all the stats at each step
      assertEquals(ns, ons);
    }
  }

  public static <N extends Number> void assertEquals(SampleStatistics<N> expected, SampleStatistics<N> actual) {
    assertEquals(expected.size(), actual.size());
    assertEquals(expected.min(), actual.min());
    assertEquals(expected.max(), actual.max());
    assertEquals(expected.mean(), actual.mean(), EPSILON);
    assertEquals(expected.variance(), actual.variance(), EPSILON);
    assertEquals(expected.stdev(), actual.stdev(), EPSILON);
  }

  @Override
  public void testAsCollector() throws Exception {
    doTestAsCollector(new NumberSampleOnline<>(), NumberSampleOnlineTest::assertEquals,
        2,3,65,123,435,123,69,34,23,42,123,12,3,-123,34,-34);
  }
}