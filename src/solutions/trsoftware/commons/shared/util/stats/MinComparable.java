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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Keeps track of the minimum in a sequence of Comparable objects.
 *
 * @see Stream#min(Comparator)
 * @see Collections#min(Collection)
 * @author Alex
 */
public class MinComparable<T extends Comparable<T>> extends AbstractMinMaxComparable<T, MinComparable<T>> {

  public MinComparable() {}

  public MinComparable(Iterable<T> candidates) {
    super(candidates);
  }

  @Override
  protected int getMultiplier() {
    return -1;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MinComparable min = (MinComparable)o;

    if (get() != null ? !get().equals(min.get()) : min.get() != null) return false;

    return true;
  }

  public int hashCode() {
    return (get() != null ? get().hashCode() : 0);
  }

  /**
   * @return The min of the given comparable objects.
   */
  @SafeVarargs
  public static <T extends Comparable<T>> T eval(T... candidates) {
    MinComparable<T> instance = new MinComparable<>();
    instance.updateAll(candidates);
    return instance.get();
  }

  @Override
  public java.util.stream.Collector<T, ?, MinComparable<T>> getCollector() {
    return Collector.getInstance();
  }

  /**
   * Provides a cached collector descriptor that can be passed to {@link Stream#collect}
   * to collect the stream elements into an instance of {@link MinComparable}.
   *
   * @param <T> the input element type
   * @see #getInstance()
   */
  public static class Collector<T extends Comparable<T>> extends CollectableStats.Collector<T, MinComparable<T>> {

    /**
     * NOTE: static fields are automatically lazy-init for singletons and safer to use than double-checked locking.
     * @see <a href="https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html">Why double-checked locking is broken</a>
     * @see <a href="https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">Initialization-on-demand holder idiom</a>
     */
    private static final Collector INSTANCE = new Collector();

    /**
     * @param <T> the input element type
     * @return the cached instance of this {@link Collector}
     * @see Stream#min(Comparator)
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> Collector<T> getInstance() {
      return INSTANCE;
    }

    @Override
    public Supplier<MinComparable<T>> supplier() {
      return MinComparable::new;
    }
  }
}