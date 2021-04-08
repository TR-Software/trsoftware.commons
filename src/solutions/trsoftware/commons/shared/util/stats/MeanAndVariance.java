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
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * Uses Welford's online algorithm to compute the mean and variance of a stream of numbers
 * without storing all the individual data points.
 * <p>
 * <b>NOTE</b>: although this class implements {@link CollectableStats StatsCollector&lt;Double, ...&gt},
 * which allows it to be used with  {@link Stream#collect(java.util.stream.Collector) Stream&lt;Double&gt;.collect()},
 * it is more efficient to use {@link DoubleStream#collect} where applicable
 * (see {@link #collectDoubleStream(DoubleStream)}).
 *
 * @see NumberSampleOnlineDouble
 * @see <a href="https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Welford's_online_algorithm">
 *   Welford's online algorithm for calculating variance</a>
 * @author Alex
 */
public class MeanAndVariance implements Serializable, SampleStatisticsDouble, CollectableStats<Double, MeanAndVariance> {

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
   */
  // NOTE: this algorithm is also implemented in variance.py

  private int n;
  private double mean;
  private double m2;

  public MeanAndVariance() {
    // public constructor needed to support Serializable
  }

  /**
   * Add a new number to the sample.
   */
  public synchronized void update(double x) {
    if (Double.isFinite(x)) {
      // allow only finite values, otherwise a single bad input can destroy what we have (e.g. make everything NaN)
      n++;
      double delta = x - mean;
      mean = mean + (delta/n);
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
  public synchronized void merge(MeanAndVariance other) {
    int newN = n + other.n;
    double newMean = (mean * n + other.mean * other.n) / newN;
    // using the "combined variance" formula given by http://www.emathzone.com/tutorials/basic-statistics/combined-variance.html
    m2 = (m2 + other.m2 + n*Math.pow(mean-newMean,2) + other.n*Math.pow(other.mean-newMean,2));
    mean = newMean;
    n = newN;
  }

  /**
   * @return the number of inputs processed
   */
  public synchronized int size() {
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

  public synchronized double mean() {
    return mean;
  }

  @Override
  public double median() {
    throw new UnsupportedOperationException();
  }

  public synchronized double sum() {
    return mean * n;
  }

  public synchronized double variance() {
    // variance_n = M2/n      # Population variance
    // variance = M2/(n - 1)  # Sample variance
    // NOTE: NumberSample uses the Population variance, so we use the same here
    return m2/n;
  }

  @Override
  public synchronized ImmutableStats<Double> summarize() {
    return new ImmutableStats<>(size(), null, null, null, sum(), mean(), variance());
  }

  public synchronized boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MeanAndVariance meanAndVariance = (MeanAndVariance)o;

    if (Double.compare(meanAndVariance.m2, m2) != 0) return false;
    if (Double.compare(meanAndVariance.mean, mean) != 0) return false;
    if (meanAndVariance.n != n) return false;

    return true;
  }

  public synchronized int hashCode() {
    // IntelliJ's default hash code block uses Double.doubleToLongBits which
    // isn't supported by GWT, so we use custom hashCode logic
    return new HashCodeBuilder().update(n, mean, m2).hashCode();
  }

  @Override
  public synchronized String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("MeanAndVariance");
    sb.append("(n=").append(n);
    sb.append(", mean=").append(mean);
    sb.append(", m2=").append(m2);
    sb.append(')');
    return sb.toString();
  }

  @Override
  public void update(Double x) {
    update(x.doubleValue());
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
        (meanAndVariance, value) -> meanAndVariance.update(value),
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
    @SuppressWarnings("unchecked")
    public static Collector getInstance() {
      return INSTANCE;
    }

    @Override
    public Supplier<MeanAndVariance> supplier() {
      return MeanAndVariance::new;
    }

    /**
     * Since all the methods in {@link MeanAndVariance} are synchronized, we can include
     * {@link java.util.stream.Collector.Characteristics#CONCURRENT CONCURRENT} characteristic.
     * @see #CH_CONCURRENT_ID
     */
    @Override
    public Set<Characteristics> characteristics() {
      return CH_CONCURRENT_ID;
    }
  }

}