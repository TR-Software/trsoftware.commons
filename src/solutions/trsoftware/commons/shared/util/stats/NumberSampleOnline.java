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
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An online version of {@link NumberSample} for {@link Comparable} values that uses O(1) space.
 * Computes mean and variance of the sample without storing all the individual data points using
 * the algorithm described in http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm.
 * <p>
 * Since the individual numbers are not stored, it's impossible to compute an exact median nor select a percentile,
 * therefore those operations are not supported.
 * <p>
 * In the future, it's possible to modify this class to use an approximate online median selection algorithm like the
 * one described in Cantone and Hofri, "Analysis of An Approximate Median Selection Algorithm,"
 * ftp.cs.wpi.edu/pub/techreports/pdf/06-17.pdf
 *
 * <p style="color: #6495ed; font-weight: bold;">
 *   TODO: implement {@link java.util.stream.Collector} to allow using this class with a stream.
 * </p>
 *
 * @author Alex
 * @see NumberSampleOnlineDouble
 * @see NumberSample
 */
public class NumberSampleOnline<N extends Number & Comparable<N>> implements SampleStatistics<N>, CollectableStats<N, NumberSampleOnline<N>>, Serializable {

  private final MeanAndVariance meanAndVariance = new MeanAndVariance();
  private final MinAndMaxComparable<N> minAndMax = new MinAndMaxComparable<N>();

  @Override
  public synchronized void update(N number) {
    meanAndVariance.update(number.doubleValue());
    minAndMax.update(number);
  }

  @Override
  public synchronized void merge(NumberSampleOnline<N> other) {
    meanAndVariance.merge(other.meanAndVariance);
    minAndMax.merge(other.minAndMax);
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
  public synchronized double sum() {
    return meanAndVariance.sum();
  }

  public synchronized double mean() {
    return meanAndVariance.mean();
  }

  /** The upper median of the dataset (if there are 2 medians) */
  public synchronized N median() {
    throw new UnsupportedOperationException("This online algorithm doesn't support medians.");
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

    if (!meanAndVariance.equals(that.meanAndVariance))
      return false;
    if (!minAndMax.equals(that.minAndMax))
      return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = meanAndVariance.hashCode();
    result = 31 * result + minAndMax.hashCode();
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
    return new ImmutableStats<N>(size(), min(), max(), null, sum(), mean(), variance());
  }

  @Override
  public java.util.stream.Collector<N, ?, NumberSampleOnline<N>> getCollector() {
    return Collector.getInstance();
  }

  /**
   * Provides a cached collector descriptor that can be passed to {@link Stream#collect}
   * to collect the stream elements into an instance of {@link NumberSampleOnline}.
   *
   * @param <N> the input element type
   * @see #getInstance()
   */
  public static class Collector<N extends Number & Comparable<N>> extends CollectableStats.Collector<N, NumberSampleOnline<N>> {

    /**
     * NOTE: static fields are automatically lazy-init for singletons and safer to use than double-checked locking.
     *
     * @see <a href="https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html">Why double-checked locking
     *     is broken</a>
     * @see <a href="https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">Initialization-on-demand
     *     holder idiom</a>
     */
    private static final Collector INSTANCE = new Collector();

    /**
     * @param <N> the input element type
     * @return the cached instance of this {@link Collector}
     */
    @SuppressWarnings("unchecked")
    public static <N extends Number & Comparable<N>> Collector<N> getInstance() {
      return INSTANCE;
    }

    @Override
    public Supplier<NumberSampleOnline<N>> supplier() {
      return NumberSampleOnline::new;
    }

    /**
     * Since all the methods in {@link NumberSampleOnline} are synchronized, we can include
     * {@link java.util.stream.Collector.Characteristics#CONCURRENT CONCURRENT} characteristic.
     * @see #CH_CONCURRENT_ID
     */
    @Override
    public Set<Characteristics> characteristics() {
      return CH_CONCURRENT_ID;
    }
  }
}