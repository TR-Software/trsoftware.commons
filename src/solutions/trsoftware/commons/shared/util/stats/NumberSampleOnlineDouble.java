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

import java.io.Serializable;

/**
 * An online version of NumberSample for values of type double that uses O(1) space. Computes mean and variance
 * of the sample without storing all the individual data points using the algorithm described in
 * http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm:
 *
 * Since the individual numbers are not stored, it's impossible to compute an exact median nor select a percentile,
 * therefore those operations are not supported.
 *
 * In the future, it's possible to modify this class to use an approximate online median selection algorithm like the
 * one described in Cantone and Hofri, "Analysis of An Approximate Median Selection Algorithm,"
 * ftp.cs.wpi.edu/pub/techreports/pdf/06-17.pdf
 *
 *  @author Alex
 */
public class NumberSampleOnlineDouble implements Serializable, SampleStatisticsDouble, Mergeable<NumberSampleOnlineDouble> {

  final MeanAndVariance mv = new MeanAndVariance();
  final MinDouble min = new MinDouble();
  final MaxDouble max = new MaxDouble();

  public NumberSampleOnlineDouble() {
  }

  // TODO: create a static factory method that wraps any SampleStatistics with an adapter (like Collections.synchronized*)
  // TODO: (might be able to do this for any interface using reflection)

  @Override
  public void update(Double x) {
   update(x.doubleValue());
  }

  public void update(double x) {
    if (Double.isFinite(x)) {
      // allow only finite values, otherwise a single bad input can destroy what we have (e.g. make everything NaN)
      mv.update(x);
      min.update(x);
      max.update(x);
    }
    else {
      System.err.println("WARNING: " + getClass().getSimpleName() + " ignoring bad input: " + x);
    }
  }

  @Override
  public void merge(NumberSampleOnlineDouble other) {
    mv.merge(other.mv);
    min.merge(other.min);
    max.merge(other.max);
  }

  public int size() {
    return mv.size();
  }

  public double min() {
    return min.get();
  }

  public double max() {
    return max.get();
  }

  @Override
  public double sum() {
    return mv.sum();
  }

  public double mean() {
    return mv.mean();
  }

  /** The upper median of the dataset (if there are 2 medians) */
  public double median() {
    throw new UnsupportedOperationException("NumberSampleOnlineDouble doesn't support medians.");
  }

  public double stdev() {
    return Math.sqrt(variance());
  }

  public double variance() {
    // variance_n = M2/n      # Population variance
    // variance = M2/(n - 1)  # Sample variance
    // NOTE: NumberSample uses the Population variance, so we use the same here
    return mv.variance();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NumberSampleOnlineDouble that = (NumberSampleOnlineDouble)o;
    return max.equals(that.max) && min.equals(that.min) && mv.equals(that.mv);
  }

  @Override
  public int hashCode() {
    int result = mv.hashCode();
    result = 31 * result + min.hashCode();
    result = 31 * result + max.hashCode();
    return result;
  }

  /**
   * Returns a summary of the sample, which contains all the statistics without
   * actually storing all the numbers in the sample.  This is convenient
   * when you're done collecting data and wish to release the memory used
   * up by all the numbers.
   */
  @Override
  public ImmutableStats<Double> summarize() {
    return new ImmutableStats<Double>(size(), min(), max(), null, sum(), mean(), stdev(), variance());
  }


}