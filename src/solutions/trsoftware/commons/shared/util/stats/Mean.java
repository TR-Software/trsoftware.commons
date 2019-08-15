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

import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * An arithmetic mean that can be updated with new samples.
 * <p>
 * <b>NOTE</b>: although this class implements {@link CollectableStats StatsCollector&lt;Double, ...&gt},
 * which allows it to be used with  {@link Stream#collect(java.util.stream.Collector) Stream&lt;Double&gt;.collect()},
 * it is more efficient to use the primitive stream methods like {@link DoubleStream#average()} where applicable.
 *
 * @see java.util.stream.Collectors#summarizingDouble
 * @author Alex
 */
public class Mean<N extends Number> implements CollectableStats<N, Mean<N>> {
  /** The current mean value of all the samples that have been given */
  private double mean = 0;
  /** Number of samples processed to compute the mean */
  private int n = 0;

  /** Updates the mean with a new sample, returning the new mean */
  public void update(N sample) {
    update(sample.doubleValue());
  }

  /** Updates the mean with a new sample, returning the new mean */
  public double update(double sample) {
    return mean = ((mean * n) + sample) / ++n;
  }

  public double getMean() {
    return mean;
  }

  public int getNumSamples() {
    return n;
  }

  @Override
  public void merge(Mean<N> other) {
    mean = (mean * n + other.mean * other.n) / (n += other.n);
  }

  @Override
  public java.util.stream.Collector<N, ?, Mean<N>> getCollector() {
    return Collector.getInstance();
  }

  /**
   * Provides a cached collector descriptor that can be passed to {@link Stream#collect}
   * to collect the stream elements into an instance of {@link Mean}.
   *
   * @param <N> the input element type
   * @see #getInstance()
   */
  public static class Collector<N extends Number> extends CollectableStats.Collector<N, Mean<N>> {

    /**
     * NOTE: static fields are automatically lazy-init for singletons and safer to use than double-checked locking.
     * @see <a href="https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html">Why double-checked locking is broken</a>
     * @see <a href="https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">Initialization-on-demand holder idiom</a>
     */
    private static final Collector INSTANCE = new Collector();

    /**
     * <strong>NOTE:</strong> it is more efficient to use the primitive stream methods like
     * {@link DoubleStream#average()} where applicable.
     *
     * @param <N> the input element type
     * @return the cached instance of this {@link Collector}
     *
     * @see DoubleStream#average()
     * @see IntStream#average()
     * @see LongStream#average()
     */
    @SuppressWarnings("unchecked")
    public static <N extends Number> Collector<N> getInstance() {
      return INSTANCE;
    }

    @Override
    public Supplier<Mean<N>> supplier() {
      return Mean::new;
    }
  }

}
