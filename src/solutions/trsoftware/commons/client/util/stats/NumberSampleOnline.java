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

import java.io.Serializable;

/**
 * An online version of NumberSample for Comparable values that uses O(1) space. Computes mean and variance
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
 *
 * @author Alex
 */
public class NumberSampleOnline<N extends Number & Comparable<N>> implements SampleStatistics<N>, Serializable {

  final MeanAndVariance meanAndVariance = new MeanAndVariance();
  final MinAndMaxComparable<N> minAndMax = new MinAndMaxComparable<N>();

  @Override
  public synchronized void update(N number) {
    meanAndVariance.update(number.doubleValue());
    minAndMax.update(number);
  }

  public synchronized int size() {
    return meanAndVariance.size();
  }

  public synchronized N min() {
    return minAndMax.getMin();
  }

  public synchronized N max() {
    return minAndMax.getMax();
  }

  @Override
  public double sum() {
    return meanAndVariance.sum();
  }

  public synchronized double mean() {
    return meanAndVariance.mean();
  }

  /** The upper median of the dataset (if there are 2 medians) */
  public synchronized N median() {
    throw new UnsupportedOperationException("NumberSampleOnlineComparable doesn't support medians.");
  }

  public synchronized double stdev() {
    return Math.sqrt(variance());
  }

  public synchronized double variance() {
    // variance_n = M2/n      # Population variance
    // variance = M2/(n - 1)  # Sample variance
    // NOTE: NumberSample uses the Population variance, so we use the same here
    return meanAndVariance.variance();
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NumberSampleOnline that = (NumberSampleOnline)o;

    if (meanAndVariance != null ? !meanAndVariance.equals(that.meanAndVariance) : that.meanAndVariance != null)
      return false;
    if (minAndMax != null ? !minAndMax.equals(that.minAndMax) : that.minAndMax != null)
      return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (meanAndVariance != null ? meanAndVariance.hashCode() : 0);
    result = 31 * result + (minAndMax != null ? minAndMax.hashCode() : 0);
    return result;
  }

  /**
   * Returns a summary of the sample, which contains all the statistics without
   * actually storing all the numbers in the sample.  This is convenient
   * when you're done collecting data and wish to release the memory used
   * up by all the numbers.
   */
  @Override
  public ImmutableStats<N> summarize() {
    return new ImmutableStats<N>(size(), min(), max(), null, sum(), mean(), stdev(), variance());
  }
}