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

import javax.annotation.Nullable;

/**
 * Mar 26, 2009
 *
 * @author Alex
 * @see com.google.common.math.Stats
 */
public interface SampleStatistics<N extends Number> extends Updatable<N> {
  /**
   * @return the number of values in the sample
   */
  int size();

  /**
   * @return the smallest number in the sample, or {@code null} if the sample is empty.
   */
  @Nullable
  N min();

  /**
   * @return the largest number in the sample, or {@code null} if the sample is empty.
   */
  @Nullable
  N max();

  double sum();

  double mean();

  /**
   * @return the upper median of the dataset (if there are 2 medians), or {@code null} if the sample is empty.
   * @see NumberSample#getMedian()
   */
  @Nullable
  N median();

  default double stdev() {
    return Math.sqrt(variance());
  }

  double variance();

  ImmutableStats<N> summarize();

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
