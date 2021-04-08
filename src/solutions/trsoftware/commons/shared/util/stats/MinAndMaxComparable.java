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
import java.util.stream.Stream;

/**
 * Combines {@link MinComparable} and {@link MaxComparable} under one data structure.
 *
 * @see Collectors#summarizingDouble
 * @see Collectors#summarizingInt
 * @see Collectors#summarizingLong
 *
 * @author Alex, Oct 11, 2012
 */
public class MinAndMaxComparable<T extends Comparable<T>> implements Serializable, CollectableStats<T, MinAndMaxComparable<T>> {
  private MinComparable<T> min;
  private MaxComparable<T> max;

  public MinAndMaxComparable() {
    min = new MinComparable<T>();
    max = new MaxComparable<T>();
  }

  public MinAndMaxComparable(Iterable<T> candidates) {
    min = new MinComparable<T>(candidates);
    max = new MaxComparable<T>(candidates);
  }

  /** Updates the current min and max value with a new sample */
  public void update(T val) {
    min.update(val);
    max.update(val);
  }

  public T getMin() {
    return min.get();
  }

  public T getMax() {
    return max.get();
  }

  @Override
  public void merge(MinAndMaxComparable<T> other) {
    update(other.getMin());
    update(other.getMax());
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MinAndMaxComparable minAndMax = (MinAndMaxComparable)o;

    if (max != null ? !max.equals(minAndMax.max) : minAndMax.max != null)
      return false;
    if (min != null ? !min.equals(minAndMax.min) : minAndMax.min != null)
      return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (min != null ? min.hashCode() : 0);
    result = 31 * result + (max != null ? max.hashCode() : 0);
    return result;
  }


  @Override
  public java.util.stream.Collector<T, ?, MinAndMaxComparable<T>> getCollector() {
    return Collector.getInstance();
  }

  /**
   * Provides a cached collector descriptor that can be passed to {@link Stream#collect}
   * to collect the stream elements into an instance of {@link MinAndMaxComparable}.
   *
   * @param <T> the input element type
   * @see #getInstance()
   */
  public static class Collector<T extends Comparable<T>> extends CollectableStats.Collector<T, MinAndMaxComparable<T>> {

    /**
     * NOTE: static fields are automatically lazy-init for singletons and safer to use than double-checked locking.
     * @see <a href="https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html">Why double-checked locking is broken</a>
     * @see <a href="https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">Initialization-on-demand holder idiom</a>
     */
    private static final Collector INSTANCE = new Collector();

    /**
     * @param <T> the input element type
     * @return the cached instance of this {@link Collector}
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> Collector<T> getInstance() {
      return INSTANCE;
    }

    @Override
    public Supplier<MinAndMaxComparable<T>> supplier() {
      return MinAndMaxComparable::new;
    }
  }
}
