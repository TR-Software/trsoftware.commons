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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import solutions.trsoftware.commons.shared.util.ImmutablePair;
import solutions.trsoftware.commons.shared.util.JsonBuilder;
import solutions.trsoftware.commons.shared.util.mutable.MutableInteger;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Uses a hash map to count the number of occurrences of each key (a key
 * can be any hashable object, most commonly a string).
 * <p>
 * NOTE: this implementation is fully {@code synchronized}.
 * <span style="color: #0073BF; font-weight: bold;">
 *   TODO: might want to also provide an unsynchronized version, for faster performance in single-threaded contexts.
 * </span>
 *
 * @param <K> type of the objects being counted
 * @author Alex
 */
public class HashCounter<K> implements CollectableStats<K, HashCounter<K>> {
  /*
   NOTE: don't change the value type to AtomicInteger because it doesn't implement equals (so would have to rewrite
   our equals method to manually compare the values of each entry, because Map.equals would no longer work as expected.
   */
  private final Map<K, MutableInteger> map;
  private int totalSum = 0;

  /**
   * Will use a new instance of {@link LinkedHashMap} as the internal map.
   * @see #HashCounter(int)
   */
  public HashCounter() {
    this(new LinkedHashMap<>());
  }

  /**
   * Will store the counts in a {@link LinkedHashMap} constructed with the given initial capacity.
   *
   * @param initialCapacity argument for {@link LinkedHashMap#LinkedHashMap(int)}
   * @see #HashCounter(Map)
   */
  public HashCounter(int initialCapacity) {
    this(new LinkedHashMap<>(initialCapacity));
  }

  /**
   * Can use this constructor to provide a custom implementation for the internal map.
   *
   * @param mapImpl will be used as the internal map.
   * NOTE: it is recommended that the caller doesn't retain a reference to this arg
   * after constructing the {@link HashCounter}
   *
   * @see #HashCounter(Supplier)
   */
  public HashCounter(Map<K, MutableInteger> mapImpl) {
    map = mapImpl;
  }

  /**
   * Can use this constructor to specify a custom implementation for the internal map.
   * Rather than directly passing a map instance to {@link #HashCounter(Map)}, this constructor can be used to
   * ensure that the internal map doesn't leak from this class.
   *
   * @param mapSupplier factory for a new map instance to be used as the internal map
   * @see #HashCounter(Map)
   */
  public HashCounter(Supplier<? extends Map<K, MutableInteger>> mapSupplier) {
    this(mapSupplier.get());
  }

  /** Increments the counter for the given key, and returns the previous count */
  public int increment(K key) {
    return add(key, 1);
  }

  /** Increments the counter for the given key, and returns the previous count */
  public synchronized int add(K key, int delta) {
    totalSum += delta;
    return map.computeIfAbsent(key, k -> new MutableInteger()).getAndAdd(delta);
  }

  /** Returns the count for the given key */
  public synchronized int get(K key) {
    MutableInteger value = map.get(key);
    if (value == null)
      return 0;
    return value.get();
  }

  /**
   * @return immutable set of the unique keys
   */
  public synchronized Set<K> keySet() {
    return ImmutableSet.copyOf(map.keySet());
  }

  /**
   * @return a copy of this counter as an immutable map containing key-count pairs in the same order they were inserted.
   *
   * @see #entriesSortedByKeyAscending()
   * @see #entriesSortedByKeyDescending()
   * @see #entriesSortedByValueAscending()
   * @see #entriesSortedByValueDescending()
   */
  public synchronized Map<K, Integer> asMap() {
    ImmutableMap.Builder<K, Integer> mapBuilder = ImmutableMap.builderWithExpectedSize(map.size());
    for (Map.Entry<K, MutableInteger> entry : map.entrySet()) {
      mapBuilder.put(entry.getKey(), entry.getValue().get());
    }
    return mapBuilder.build();
  }

  public SortedSet<Map.Entry<K, Integer>> entriesSortedByKeyAscending() {
    return toSortedSet(keyComparator());
  }

  public SortedSet<Map.Entry<K, Integer>> entriesSortedByKeyDescending() {
    return toSortedSet(Collections.reverseOrder(keyComparator()));
  }

  /** Returns a list of all the counts, sorted by key (not by count) */
  public SortedSet<Map.Entry<K, Integer>> entriesSortedByValueAscending() {
    return toSortedSet(valueComparator());
  }

  /** Returns a list of all the counts, sorted by key (not by count) */
  public SortedSet<Map.Entry<K, Integer>> entriesSortedByValueDescending() {
    return toSortedSet(Collections.reverseOrder(valueComparator()));
  }

  @SuppressWarnings("unchecked")
  private Comparator<Map.Entry<K, Integer>> keyComparator() {
    return (e1, e2) -> {
      K k1 = e1.getKey();
      K k2 = e2.getKey();
      if (k1 instanceof Comparable && k2 instanceof Comparable)
        return ((Comparable)k1).compareTo(k2);
      else
        // TODO(10/4/2019): this is too arbitrary; instead have callers explicitly pass the Comparator<K>
        return k1.toString().compareTo(k2.toString());  // compare the string representations if the classes aren't comparable
    };
  }

  private Comparator<Map.Entry<K, Integer>> valueComparator() {
    Comparator<Map.Entry<K,Integer>> keyComparator = keyComparator();
    return (e1, e2) -> {
      int result = e1.getValue().compareTo(e2.getValue());
      // since the underlying implementation uses a SortedSet, we have to make
      // sure that equivalent values don't get lost: use the key comparator to resolve ties
      // TODO(10/4/2019): to be sure values don't get lost, don't use SortedSet (instead return a List sorted with Collections.sort)
      if (result == 0)
        return keyComparator.compare(e1, e2);
      return result;
    };
  }

  private synchronized SortedSet<Map.Entry<K, Integer>> toSortedSet(Comparator<Map.Entry<K, Integer>> comparator) {
    /*
     TODO(10/4/2019): it might make more sense to return a List<Map.Entry> instead of SortedSet<Map.Entry> to be
     sure that none of the entries get lost because the comparator considers them equal
     (can just use Collections.sort() instead of TreeSet)
     */
    SortedSet<Map.Entry<K, Integer>> sortedEntries = new TreeSet<Map.Entry<K, Integer>>(comparator);
    for (K key : map.keySet()) {
      sortedEntries.add(new ImmutablePair<K, Integer>(key, map.get(key).get()));
    }
    return sortedEntries;
  }

  public synchronized int size() {
    return map.size();
  }

  @Override
  public synchronized String toString() {
    return JsonBuilder.mapToJson(map);
  }

  public synchronized int sumOfAllEntries() {
    return totalSum;
  }

  @Override
  public synchronized void merge(HashCounter<K> other) {
    for (K k : other.keySet()) {
      add(k, other.get(k));
    }
  }

  /**
   * Computes the probability of the given key being selected from a statistical data sample represented by this counter:
   * <pre>
   *   P(k) = count(k) / count(*)
   * </pre>
   *
   * @return the probability of the given key being selected from a statistical data sample represented by this counter
   */
  public synchronized double probabilityOf(K key) {
    return ((double)get(key)) / sumOfAllEntries();
  }

  @Override
  public void update(K x) {
    increment(x);
  }

  /*
  TODO: reconsider whether to implement equals/hashCode, since this is a mutable object
  - see https://stackoverflow.com/questions/1636399/correctly-synchronizing-equals-in-java
  - ideas:
    - perhaps use the immutable version returned by asMap();
      that still doesn't solve the unpredictability of using this class in a HashMap, but maybe that's okay
  */

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    HashCounter<?> that = (HashCounter<?>)o;

    if (totalSum != that.totalSum)
      return false;
    return map.equals(that.map);
  }

  @Override
  public int hashCode() {
    int result = map.hashCode();
    result = 31 * result + totalSum;
    return result;
  }

  @Override
  public java.util.stream.Collector<K, ?, HashCounter<K>> getCollector() {
    return Collector.getInstance();
  }

  /**
   * Provides a cached collector descriptor that can be passed to {@link Stream#collect}
   * to collect the stream elements into an instance of {@link HashCounter}.
   *
   * @param <K> the input element type
   * @see #getInstance()
   */
  public static class Collector<K> extends CollectableStats.Collector<K, HashCounter<K>> {

    /**
     * NOTE: static fields are automatically lazy-init for singletons and safer to use than double-checked locking.
     * @see <a href="https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html">Why double-checked locking is broken</a>
     * @see <a href="https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">Initialization-on-demand holder idiom</a>
     */
    private static final Collector INSTANCE = new Collector();

    /**
     * @param <K> the input element type
     * @return the cached instance of this {@link Collector}
     */
    @SuppressWarnings("unchecked")
    public static <K> Collector<K> getInstance() {
      return INSTANCE;
    }

    @Override
    public Supplier<HashCounter<K>> supplier() {
      return HashCounter::new;
    }

    /**
     * Since all the methods in {@link HashCounter} are synchronized, we can include
     * {@link java.util.stream.Collector.Characteristics#CONCURRENT CONCURRENT} characteristic.
     * @see #CH_CONCURRENT_ID
     */
    @Override
    public Set<Characteristics> characteristics() {
      return CH_CONCURRENT_ID;
    }
  }
}
