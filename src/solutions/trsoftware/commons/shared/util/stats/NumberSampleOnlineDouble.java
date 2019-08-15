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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * An online version of NumberSample for values of type double that uses O(1) space.
 * Uses Welford's online algorithm to computes the mean and variance (see {@link MeanAndVariance}).
 * <p>
 * Since the individual numbers are not stored, it's impossible to compute an exact median nor select a percentile,
 * therefore those operations are not supported.
 * <p>
 * In the future, it might be possible to modify this class to use an approximate median selection algorithm like
 * the one described in Cantone and Hofri's paper <a href="ftp://ftp.cs.wpi.edu/pub/techreports/pdf/06-17.pdf">
 * "Analysis of An Approximate Median Selection Algorithm"</a>
 * (NOTE: there are also more general approximation algorithms for computing vaarious quantiles, e.g. percentiles).
 * <p>
 * <b>NOTE</b>: although this class implements {@link CollectableStats StatsCollector&lt;Double, ...&gt},
 * which allows it to be used with  {@link Stream#collect(java.util.stream.Collector) Stream&lt;Double&gt;.collect()},
 * it is more efficient to use {@link DoubleStream#collect} where applicable
 * (see {@link #collectDoubleStream(DoubleStream)}).
 *
 * @see <a href="https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Welford's_online_algorithm">
 *   Welford's online algorithm for calculating variance</a>
 * @see DoubleStream#summaryStatistics()
 * @see Collectors#summarizingDouble
 * @author Alex
 */
public class NumberSampleOnlineDouble implements Serializable, SampleStatisticsDouble, CollectableStats<Double, NumberSampleOnlineDouble> {

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
    throw new UnsupportedOperationException("This online algorithm doesn't support medians.");
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
    return new ImmutableStats<Double>(size(), min(), max(), null, sum(), mean(), variance());
  }


  @Override
  public java.util.stream.Collector<Double, ?, NumberSampleOnlineDouble> getCollector() {
    return Collector.getInstance();
  }

  /**
   * Collects a {@link DoubleStream} into an instance of this class.
   */
  public static NumberSampleOnlineDouble collectDoubleStream(DoubleStream doubleStream) {
    return doubleStream.collect(NumberSampleOnlineDouble::new,
        (numberSample, value) -> numberSample.update(value),
        NumberSampleOnlineDouble::merge);
  }
  
  /**
   * Provides a cached collector descriptor that can be passed to {@link Stream#collect}
   * to collect the stream elements into an instance of {@link NumberSampleOnlineDouble}.
   *
   * @see #getInstance()
   */
  public static class Collector extends CollectableStats.Collector<Double, NumberSampleOnlineDouble> {

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
    public Supplier<NumberSampleOnlineDouble> supplier() {
      return NumberSampleOnlineDouble::new;
    }

  }
  
}