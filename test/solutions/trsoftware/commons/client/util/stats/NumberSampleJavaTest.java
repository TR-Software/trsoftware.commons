/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.util.stats;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This a Java-only (non-GWT) test because it uses the Java-only Collections.shuffle method.
 *  
 * @author Alex
 */
public class NumberSampleJavaTest extends TestCase {
  private NumberSample<Integer> numberSample;

  public void setUp() {
    numberSample = new NumberSample<Integer>();

    // add the numbers 0..9 to the sample in random order

    // 1) randomize the inputs (must not add them in sorted order, to truly test the code)
    List<Integer> inputs = new ArrayList<Integer>();
    for (int i = 0; i < 10; i++) {
      inputs.add(i);
    }
    Collections.shuffle(inputs);
    // 2) add them to the sample
    for (Integer input : inputs) {
      numberSample.update(input);
    }
  }

  public void testCount() throws Exception {
    assertEquals(10, numberSample.size());
  }

  public void testMedian() throws Exception {
    // there is an even number of samples in the given data set, make sure that the upper median is used
    assertEquals(5, (int)numberSample.median());
    // add one more value to get rid of the two medians ambiguity
    numberSample.update(10);
    assertEquals(5, (int)numberSample.median());  // now an even number of samples, so the mean is obvious

    // add some numbers out-of-order, to make sure the list will get re-sorted
    numberSample.update(-1);
    numberSample.update(-5);
    assertEquals(4, (int)numberSample.median());
  }

  public void testMean() throws Exception {
    assertEquals((9d * (9 + 1) / 2) / 10, numberSample.mean());  // Gauss's formula for sum 1,2,3,..n: n(n+1)/2
    assertEquals(4.5, numberSample.mean());  // just making sure :)
  }

  public void testStdDev() throws Exception {
    // variance is the sum of the squared diffs from the mean divided by number of samples
    double sumSquaredDiffs = 0;
    double mean = numberSample.mean();
    for (int i = 0; i < 10; i++) {
      sumSquaredDiffs += Math.pow(mean - i, 2);
    }
    double variance = sumSquaredDiffs / 10;
    // variance is standard deviation squared
    assertEquals(Math.sqrt(variance), numberSample.stdev());
  }

  public void testSummarize() throws Exception {
    ImmutableStats<Integer> summary = numberSample.summarize();
    assertEquals(numberSample.size(), summary.size());
    assertEquals(numberSample.min(), summary.min());
    assertEquals(numberSample.max(), summary.max());
    assertEquals(numberSample.mean(), summary.mean());
    assertEquals(numberSample.median(), summary.median());
    assertEquals(numberSample.stdev(), summary.stdev());
    assertEquals(numberSample.variance(), summary.variance());
  }

  public void testMin() throws Exception {
    assertEquals(0, (int)numberSample.min());
    numberSample.update(-1);
    assertEquals(-1, (int)numberSample.min());
    numberSample.update(25);
    assertEquals(-1, (int)numberSample.min());
  }

  public void testMax() throws Exception {
    assertEquals(9, (int)numberSample.max());
    numberSample.update(-1);
    assertEquals(9, (int)numberSample.max());
    numberSample.update(25);
    assertEquals(25, (int)numberSample.max());
  }
}