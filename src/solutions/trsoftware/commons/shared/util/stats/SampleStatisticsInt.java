/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.commons.shared.util.stats;

/**
 * Primitive {@code int} specialization of {@link SampleStatistics}
 *
 * @see com.google.common.math.Stats
 * @see java.util.IntSummaryStatistics
 * @author Alex, 10/26/2024
 */
public interface SampleStatisticsInt {
  int size();

  int min();

  int max();

  long sum();

  double mean();

  /** The upper median of the dataset (if there are 2 medians) */
  int median();

  /**
   * The <a href="http://en.wikipedia.org/wiki/Standard_deviation#Definition_of_population_values">
   * population standard deviation</a> of the values. The {@link #size()} must be non-zero.
   */
  default double stdev() {
    return Math.sqrt(variance());
  }

  /**
   * Returns the <a href="http://en.wikipedia.org/wiki/Variance#Population_variance">population variance</a> of the values.
   * @return the population variance, or {@code 0} if empty
   * @see #sampleVariance()
   */
  double variance();

  /**
   * Returns the <a href="http://en.wikipedia.org/wiki/Variance#Sample_variance">sample variance</a> of the values.
   * @return the sample variance, or {@code 0} if empty
   * @see #variance()
   */
  double sampleVariance();

  /**
   * Returns the <a href="http://en.wikipedia.org/wiki/Standard_deviation#Corrected_sample_standard_deviation">
   * corrected sample standard deviation</a> of the values. If this dataset is a sample drawn from a
   * population, this is an estimator of the population standard deviation of the population which
   * is less biased than {@linkplain #stdev() population standard deviation} (the unbiased estimator depends on
   * the distribution). The {@linkplain #size() count} must be greater than one.
   *
   * @return the sample standard deviation or {@code 0} if {@linkplain #size() count} is not greater than one.
   * @see #stdev()
   */
  default double sampleStdev() {
    return Math.sqrt(sampleVariance());
  }

  ImmutableStats<Integer> summarize();

  /**
   * Computes mean + N&sigma; (where &sigma; denotes "standard deviation")
   * <p>
   * For example, given a normal distribution, approximately 95.5% of the data is expected to be within 2&sigma; of
   * the mean.
   *
   * @param nStdev the number of standard deviations to add to the mean
   * @return the mean plus the given number of standard deviations
   * @see <a href="https://en.wikipedia.org/wiki/Standard_deviation#Rules_for_normally_distributed_data">
   *     Rules for normally distributed data</a>
   */
  default double meanPlusStdev(double nStdev) {
    return mean() + stdev() * nStdev;
  }

}
