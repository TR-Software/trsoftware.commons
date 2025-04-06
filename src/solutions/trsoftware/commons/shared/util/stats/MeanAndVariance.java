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

import solutions.trsoftware.commons.shared.util.HashCodeBuilder;

import java.io.Serializable;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * Uses Welford's online algorithm to compute the mean and variance of a stream of numbers
 * without storing all the individual data points.
 * <p>
 * This class is not thread-safe (since 10/23/2024).
 * <p>
 * <b>NOTE</b>: although this class implements {@link CollectableStats StatsCollector&lt;Double, ...&gt},
 * which allows it to be used with  {@link Stream#collect(java.util.stream.Collector) Stream&lt;Double&gt;.collect()},
 * it is more efficient to use {@link DoubleStream#collect} where applicable
 * (see {@link #collectDoubleStream(DoubleStream)}).
 *
 * @see NumberSampleOnlineDouble
 * @see <a href="https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Welford's_online_algorithm">
 *   Welford's online algorithm for calculating variance</a>
 * @see com.google.common.math.StatsAccumulator
 * @author Alex
 */
public class MeanAndVariance implements Serializable, SampleStatisticsDouble, UpdatableDouble, CollectableStats<Double, MeanAndVariance> {

  /* -------------------- Algorithm --------------------------------------------
   *  def online_variance(data):
   *    n = 0
   *    mean = 0
   *    M2 = 0
   *
   *    for x in data:
   *        n = n + 1
   *        delta = x - mean
   *        mean = mean + delta/n
   *        M2 = M2 + delta*(x - mean)     # (x-mean) uses the new value of mean
   *
   *    variance_n = M2/n      # Population variance
   *    variance = M2/(n - 1)  # Sample variance
   *    return (variance, variance_n)
   * ---------------------------------------------------------------------------
   * Note: this is the same algorithm as com.google.common.math.StatsAccumulator.add, which Guava attributes to
   * "Art of Computer Programming vol. 2, Knuth, 4.2.2, (15) and (16)"
   */

  private int n;  // TODO(10/23/2024): make long? (see com.google.common.math.StatsAccumulator); add SampleStatistics*.sum method variant that returns long, if available
  private double mean;
  /**
   * Sum of squares of deltas
   * (equivalent to {@link com.google.common.math.StatsAccumulator#sumOfSquaresOfDeltas})
   */
  private double m2;

  /* TODO(10/21/2024):
      - maybe add min/max, to avoid the need for MinDouble/MaxDouble in NumberSampleOnlineDouble
  */

  public MeanAndVariance() {
    // public constructor needed to support Serializable
  }

  /**
   * Add a new number to the sample.
   */
  public void update(double x) {
    if (Double.isFinite(x)) {
      // allow only finite values, otherwise a single bad input can destroy what we have (e.g. make everything NaN)
      n++;
      double delta = x - mean;
      mean += delta / n;
      m2 += delta * (x - mean);
    }
    else {
      System.err.println("WARNING: " + getClass().getSimpleName() + " ignoring bad input: " + x);
    }
  }

  /**
   * Merges another sample into this one.
   */
  @Override
  public void merge(MeanAndVariance other) {
    int newN = n + other.n;
    double newMean = (mean * n + other.mean * other.n) / newN;
    // using the "combined variance" formula given by http://www.emathzone.com/tutorials/basic-statistics/combined-variance.html
    m2 = (m2 + other.m2 + n*Math.pow(mean-newMean,2) + other.n*Math.pow(other.mean-newMean,2));
    mean = newMean;
    n = newN;
    // TODO(10/23/2024): might be able to improve this code (see com.google.common.math.StatsAccumulator.merge)
  }

  /**
   * @return the number of inputs processed
   */
  public int size() {
    return n;
  }

  @Override
  public double min() {
    throw new UnsupportedOperationException();
  }

  @Override
  public double max() {
    throw new UnsupportedOperationException();
  }

  public double mean() {
    return mean;
  }

  @Override
  public double median() {
    throw new UnsupportedOperationException();
  }

  public double sum() {
    return mean * n;
  }

  /**
   * Returns the <a href="http://en.wikipedia.org/wiki/Variance#Population_variance">population variance</a> of the values.
   * @return the population variance, or {@code 0} if empty
   * @see #sampleVariance()
   */
  public double variance() {
    if (n == 0) return 0;  // avoid divide-by-zero
    // TODO: maybe throw ISE when empty (see com.google.common.math.Stats.populationVariance)

    // variance_n = M2/n      # Population variance
    // variance = M2/(n - 1)  # Sample variance
    // NOTE: NumberSample uses the Population variance, so we use the same here
    return m2/n;
    // TODO: maybe ensure that m2 is non-negative? (see see com.google.common.math.Stats.populationVariance)
  }

  /**
   * Returns the <a href="http://en.wikipedia.org/wiki/Variance#Sample_variance">sample variance</a> of the values.
   * @return the sample variance, or {@code 0} if empty
   * @see #variance()
   */
  public double sampleVariance() {
    if (n <= 1) return 0;  // avoid divide-by-zero
    // TODO: maybe throw ISE when n <= 1 (see com.google.common.math.Stats.sampleVariance)

    // variance_n = M2/n      # Population variance
    // variance = M2/(n - 1)  # Sample variance
    return m2/(n-1);
    // TODO: maybe ensure that m2 is non-negative? (see see com.google.common.math.Stats.sampleVariance)
  }

  @Override
  public ImmutableStats<Double> summarize() {
    return new ImmutableStats<>(size(), null, null, null, sum(), mean(), variance());
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MeanAndVariance meanAndVariance = (MeanAndVariance)o;

    if (Double.compare(meanAndVariance.m2, m2) != 0) return false;
    if (Double.compare(meanAndVariance.mean, mean) != 0) return false;
    if (meanAndVariance.n != n) return false;

    return true;
  }

  public int hashCode() {
    // IntelliJ's default hash code block uses Double.doubleToLongBits which
    // isn't supported by GWT, so we use custom hashCode logic
    return new HashCodeBuilder().update(n, mean, m2).hashCode();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("MeanAndVariance");
    sb.append("(n=").append(n);
    sb.append(", mean=").append(mean);
    sb.append(", m2=").append(m2);
    sb.append(')');
    return sb.toString();
  }

  @Override
  public java.util.stream.Collector<Double, ?, MeanAndVariance> getCollector() {
    return Collector.getInstance();
  }

  /**
   * Collects a {@link DoubleStream} into an instance of this class.
   */
  public static MeanAndVariance collectDoubleStream(DoubleStream doubleStream) {
    return doubleStream.collect(MeanAndVariance::new,
        MeanAndVariance::update,
        MeanAndVariance::merge);
  }

  /**
   * Provides a cached collector descriptor that can be passed to {@link Stream#collect}
   * to collect the stream elements into an instance of {@link MeanAndVariance}.
   *
   * @see #getInstance()
   */
  public static class Collector extends CollectableStats.Collector<Double, MeanAndVariance> {

    /**
     * NOTE: static fields are automatically lazy-init for singletons and safer to use than double-checked locking.
     * @see <a href="https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html">Why double-checked locking is broken</a>
     * @see <a href="https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">Initialization-on-demand holder idiom</a>
     */
    private static final Collector INSTANCE = new Collector();

    /**
     * <strong>NOTE:</strong> it is more efficient to use {@link DoubleStream#collect} where applicable.
     *
     * @see #collectDoubleStream(DoubleStream)
     * 
     * @return the cached instance of this {@link Collector}
     */
    public static Collector getInstance() {
      return INSTANCE;
    }

    @Override
    public Supplier<MeanAndVariance> supplier() {
      return MeanAndVariance::new;
    }
  }

}