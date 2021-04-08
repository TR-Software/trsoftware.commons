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

import java.io.Serializable;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * Keeps track of the min of a sequence of double values.
 * <p>
 * <b>NOTE</b>: although this class implements {@link CollectableStats StatsCollector&lt;Double, ...&gt},
 * which allows it to be used with  {@link Stream#collect(java.util.stream.Collector) Stream&lt;Double&gt;.collect()},
 * it is more efficient to use {@link DoubleStream#min()} where applicable.
 *
 * @see DoubleStream#min()
 * @see DoubleStream#summaryStatistics()
 * @see Collectors#summarizingDouble
 * @author Alex
 */
public class MinDouble extends MinMaxDoubleBase<MinDouble> implements Serializable {

  @Override
  protected double absoluteWorst() {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  protected double bestOf(double a, double b) {
    return Math.min(a, b);
  }

  public MinDouble() {
  }

  public MinDouble(double initialValue) {
    super(initialValue);
  }

  public MinDouble(Iterable<Double> candidates) {
    super(candidates);
  }

  public MinDouble(double... candidates) {
    super(candidates);
  }

  @Override
  public void merge(MinDouble other) {
    update(other.get());
  }

  @Override
  public java.util.stream.Collector<Double, ?, MinDouble> getCollector() {
    return Collector.getInstance();
  }

  /**
   * Provides a cached collector descriptor that can be passed to {@link Stream#collect}
   * to collect the stream elements into an instance of {@link MinDouble}.
   *
   * @see #getInstance()
   */
  public static class Collector extends CollectableStats.Collector<Double, MinDouble> {

    /**
     * NOTE: static fields are automatically lazy-init for singletons and safer to use than double-checked locking.
     * @see <a href="https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html">Why double-checked locking is broken</a>
     * @see <a href="https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">Initialization-on-demand holder idiom</a>
     */
    private static final Collector INSTANCE = new Collector();

    /**
     * <strong>NOTE:</strong> it is more efficient to use {@link DoubleStream#min()} where applicable.
     *
     * @return the cached instance of this {@link Collector}
     * @see DoubleStream#min()
     * @see DoubleStream#summaryStatistics()
     * @see Collectors#summarizingDouble
     */
    @SuppressWarnings("unchecked")
    public static Collector getInstance() {
      return INSTANCE;
    }

    @Override
    public Supplier<MinDouble> supplier() {
      return MinDouble::new;
    }
  }
  
}