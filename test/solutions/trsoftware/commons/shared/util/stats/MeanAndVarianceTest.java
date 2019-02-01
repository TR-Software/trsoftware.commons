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

import junit.framework.TestCase;

/**
 * Oct 2, 2012
 *
 * @author Alex
 */
public class MeanAndVarianceTest extends TestCase {

  private static final int[] inputs = new int[]{2,3,65,123,435,123,69,34,23,42,123,12,3,-123,34,-34};
  
  /** Since we've already unit tested controlSample, all we need to do is to compare variance with controlSample */
  private NumberSample<Integer> controlSample;

  public void setUp() throws Exception {
    controlSample = new NumberSample<Integer>();
  }

  public void testMeanAndVariance() throws Exception {
    // test a sample with a bunch of Integers; we assume it will work
    // for other numeric types if it works for Integers
    MeanAndVariance meanAndVariance = new MeanAndVariance();
    for (int x : inputs) {
      meanAndVariance.update(x);
      controlSample.update(x);
      // compare all the stats at each step
      assertEquals(controlSample.size(), meanAndVariance.size());
      assertEquals(controlSample.mean(), meanAndVariance.mean(), .01);
      assertEquals(controlSample.variance(), meanAndVariance.variance(), .01);
      assertEquals(controlSample.stdev(), meanAndVariance.stdev(), .01);
    }
  }

  public void testMerge() throws Exception {
    // test a sample with a bunch of Integers; we assume it will work
    // for other numeric types if it works for Integers
    MeanAndVariance meanAndVariance = new MeanAndVariance();
    MeanAndVariance meanAndVariance2 = new MeanAndVariance();
    for (int i = 0; i < inputs.length; i++) {
      int x = inputs[i];
      controlSample.update(x);
      if (i < 4)  // add the first 4 values to the first instance and the rest to the second instance
        meanAndVariance.update(x);
      else
        meanAndVariance2.update(x);
    }
    // now merge the two instances and compare against the control
    meanAndVariance.merge(meanAndVariance2);
    assertEquals(controlSample.size(), meanAndVariance.size());
    assertEquals(controlSample.mean(), meanAndVariance.mean(), .01);
    assertEquals(controlSample.variance(), meanAndVariance.variance(), .01);
    assertEquals(controlSample.stdev(), meanAndVariance.stdev(), .01);
  }

  public void testInvalidInputs() throws Exception {
    MeanAndVariance meanAndVariance = new MeanAndVariance();
    assertEquals(0d, meanAndVariance.mean());
    assertEquals(0, meanAndVariance.size());
    double[] badValues = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
    for (double badValue : badValues) {
      meanAndVariance.update(badValue);
      // should have ignored this input
      assertEquals(0d, meanAndVariance.mean());
      assertEquals(0, meanAndVariance.size());

    }
  }
}