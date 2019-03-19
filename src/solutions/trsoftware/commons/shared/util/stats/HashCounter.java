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
import solutions.trsoftware.commons.shared.util.ImmutablePair;
import solutions.trsoftware.commons.shared.util.JsonBuilder;
import solutions.trsoftware.commons.shared.util.mutable.MutableInteger;

import java.util.*;

/**
 * Uses a hash map to count the number of occurrences of each key (a key
 * can be any hashable object, most commonly a string).
 *
 * @param <K> type of the objects being counted
 *
 * @author Alex
 */
public class HashCounter<K> implements Mergeable<HashCounter<K>> {
  private final LinkedHashMap<K, MutableInteger> map;
  private int totalSum = 0;

  public HashCounter() {
    this(new LinkedHashMap<>());
  }

  public HashCounter(int estimatedSize) {
    this(new LinkedHashMap<>(estimatedSize));
  }

  /** Subclasses can use this constructor to provide their own map implementation */
  protected HashCounter(LinkedHashMap<K, MutableInteger> mapImpl) {
    map = mapImpl;
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

  public synchronized Set<K> keySet() {
    return map.keySet();
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

  private Comparator<Map.Entry<K, Integer>> keyComparator() {
    return new Comparator<Map.Entry<K, Integer>>() {
      public int compare(Map.Entry<K, Integer> e1, Map.Entry<K, Integer> e2) {
        K k1 = e1.getKey();
        K k2 = e2.getKey();
        if (k1 instanceof Comparable && k2 instanceof Comparable)
          return ((Comparable)k1).compareTo(k2);
        else
          return k1.toString().compareTo(k2.toString());  // compare the string representations if the classes aren't comparable
      }
    };
  }

  private Comparator<Map.Entry<K, Integer>> valueComparator() {
    return new Comparator<Map.Entry<K, Integer>>() {
      private Comparator<Map.Entry<K,Integer>> keyComparator = keyComparator();
      public int compare(Map.Entry<K, Integer> e1, Map.Entry<K, Integer> e2) {
        int result = e1.getValue().compareTo(e2.getValue());
        // since the underlying implementation uses a SortedSet, we have to make
        // sure that equivalent values don't get lost: use the key comparator to resolve ties
        if (result == 0)
          return keyComparator.compare(e1, e2);
        return result;
      }
    };
  }

  private synchronized SortedSet<Map.Entry<K, Integer>> toSortedSet(Comparator<Map.Entry<K, Integer>> comparator) {
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
}
