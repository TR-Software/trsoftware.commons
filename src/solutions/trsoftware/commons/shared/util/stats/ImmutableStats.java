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
 * Encapsulates the statistics for a sample of numbers without the overhead
 * of storing each number in the sample (in contrast to NumberSample)
 *
 * @author Alex
 */
public class ImmutableStats<N extends Number> implements SampleStatistics<N>, Serializable {

  private final int size;
  private final N min;
  private final N max;
  private final N median;
  private final double sum;
  private final double mean;
  private final double stdev;
  private final double variance;

  /**
   * Default constructor to satisfy the contract of {@link Serializable}
   */
  public ImmutableStats() {
    this(0, null, null, null, 0, 0, 0, 0);
  }

  public ImmutableStats(int size, N min, N max, N median, double sum, double mean, double stdev, double variance) {
    this.size = size;
    this.min = min;
    this.max = max;
    this.median = median;
    this.sum = sum;
    this.mean = mean;
    this.stdev = stdev;
    this.variance = variance;
  }

  @Override
  public void update(N number) {
    throw new UnsupportedOperationException(getClass().getName() + " doesn't support the update method.");
  }

  public int size() {
    return size;
  }

  public N min() {
    return min;
  }

  public N max() {
    return max;
  }

  @Override
  public double sum() {
    return sum;
  }

  public double mean() {
    return mean;
  }

  public N median() {
    return median;
  }

  public double stdev() {
    return stdev;
  }

  public double variance() {
    return variance;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("ImmutableStats");
    sb.append("(size=").append(size);
    sb.append(", mean=").append(mean);
    sb.append(", min=").append(min);
    sb.append(", median=").append(median);
    sb.append(", max=").append(max);
    sb.append(", stdev=").append(stdev);
    sb.append(", variance=").append(variance);
    sb.append(')');
    return sb.toString();
  }

  @Override
  public ImmutableStats<N> summarize() {
    return this; // already summarized and immutable
  }
}
