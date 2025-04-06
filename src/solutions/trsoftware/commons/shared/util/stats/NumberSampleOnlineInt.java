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

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * An online version of {@link NumberSample} for {@code int} values, requiring only O(1) space.
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
 * <b>NOTE</b>: although this class implements {@link CollectableStats StatsCollector&lt;Integer, ...&gt},
 * which allows it to be used with  {@link Stream#collect(java.util.stream.Collector) Stream&lt;Integer&gt;.collect()},
 * it is more efficient to use {@link IntStream#collect} where applicable
 * (see {@link #collectIntStream(IntStream)}).
 *
 * @see <a href="https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Welford's_online_algorithm">
 *   Welford's online algorithm for calculating variance</a>
 * @see IntStream#summaryStatistics()
 * @see Collectors#summarizingInt
 * @author Alex
 */
public class NumberSampleOnlineInt implements Serializable, SampleStatisticsInt, UpdatableInt, CollectableStats<Integer, NumberSampleOnlineInt> {

  final MeanAndVariance mv = new MeanAndVariance();
  private long sum;
  private int min = Integer.MAX_VALUE;
  private int max = Integer.MIN_VALUE;

  public NumberSampleOnlineInt() {
  }

  // TODO: create a static factory method that wraps any SampleStatistics with a thread-saffe adapter (like Collections.synchronized*)
  // TODO: (might be able to do this for any interface using reflection)

  public void update(int value) {
    // allow only finite values, otherwise a single bad input can destroy what we have (e.g. make everything NaN)
    mv.update(value);
    sum += value;
    min = Math.min(min, value);
    max = Math.max(max, value);
  }

  @Override
  public void merge(NumberSampleOnlineInt other) {
    mv.merge(other.mv);
    sum += other.sum;
    min = Math.min(min, other.min);
    max = Math.max(max, other.max);
  }

  public int size() {
    return mv.size();
  }

  // TODO: document that if empty, min/max will return Integer.MAX_VALUE/Integer.MIN_VALUE
  public int min() {
    return min;
  }

  public int max() {
    return max;
  }

  @Override
  public long sum() {
    return sum;
  }

  public double mean() {
    return mv.mean();
  }

  /** The upper median of the dataset (if there are 2 medians) */
  public int median() {
    throw new UnsupportedOperationException("This online algorithm doesn't support medians.");
  }

  /**
   * Returns the <a href="http://en.wikipedia.org/wiki/Variance#Population_variance">population variance</a> of the values.
   * @return the population variance, or {@code 0} if empty
   * @see #sampleVariance()
   */
  public double variance() {
    return mv.variance();
  }

  /**
   * Returns the <a href="http://en.wikipedia.org/wiki/Variance#Sample_variance">sample variance</a> of the values.
   * @return the sample variance, or {@code 0} if empty
   * @see #variance()
   */
  @Override
  public double sampleVariance() {
    return mv.sampleVariance();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    NumberSampleOnlineInt that = (NumberSampleOnlineInt)o;

    if (sum != that.sum)
      return false;
    if (min != that.min)
      return false;
    if (max != that.max)
      return false;
    return mv.equals(that.mv);
  }

  @Override
  public int hashCode() {
    int result = mv.hashCode();
    result = 31 * result + (int)(sum ^ (sum >>> 32));
    result = 31 * result + min;
    result = 31 * result + max;
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("size", size())
        .add("min", min())
        .add("max", max())
        .add("sum", sum())
        .add("mean", mean())
        .add("variance", variance())
        .add("stdev", stdev())
        .toString();
  }

  /**
   * Returns a summary of the sample, which contains all the statistics without
   * actually storing all the numbers in the sample.  This is convenient
   * when you're done collecting data and wish to release the memory used
   * up by all the numbers.
   */
  @Override
  public ImmutableStats<Integer> summarize() {
    return new ImmutableStats<>(size(), min(), max(), null, sum(), mean(), variance());
  }


  @Override
  public java.util.stream.Collector<Integer, ?, NumberSampleOnlineInt> getCollector() {
    return Collector.getInstance();
  }

  /**
   * Collects a {@link IntStream} into an instance of this class.
   */
  public static NumberSampleOnlineInt collectIntStream(IntStream intStream) {
    return intStream.collect(NumberSampleOnlineInt::new,
        NumberSampleOnlineInt::update,
        NumberSampleOnlineInt::merge);
  }
  
  /**
   * Provides a cached collector descriptor that can be passed to {@link Stream#collect}
   * to collect the stream elements into an instance of {@link NumberSampleOnlineInt}.
   *
   * @see #getInstance()
   */
  public static class Collector extends CollectableStats.Collector<Integer, NumberSampleOnlineInt> {

    /**
     * NOTE: static fields are automatically lazy-init for singletons and safer to use than double-checked locking.
     * @see <a href="https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html">Why double-checked locking is broken</a>
     * @see <a href="https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">Initialization-on-demand holder idiom</a>
     */
    private static final Collector INSTANCE = new Collector();

    /**
     * <strong>NOTE:</strong> it is more efficient to use {@link IntStream#collect} where applicable.
     *
     * @see #collectIntegerStream(IntStream)
     * 
     * @return the cached instance of this {@link Collector}
     */
    @SuppressWarnings("unchecked")
    public static Collector getInstance() {
      return INSTANCE;
    }

    @Override
    public Supplier<NumberSampleOnlineInt> supplier() {
      return NumberSampleOnlineInt::new;
    }
  }
  
}