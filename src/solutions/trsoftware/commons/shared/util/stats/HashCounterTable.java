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

import com.google.common.collect.ImmutableMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A 2-dimensional version of {@link HashCounter}, similar to Guava's {@link com.google.common.collect.Table Table} class.
 *
 * @author Alex
 * @since 9/30/2019
 *
 * @see HashCounter
 */
public class HashCounterTable<R, C> implements Mergeable<HashCounterTable<R, C>> {

  private final Map<R, HashCounter<C>> map;
  private final Function<R, ? extends HashCounter<C>> rowFactory;

  public HashCounterTable() {
    this(new LinkedHashMap<>(), r -> new HashCounter<>());
  }

  /**
   * <p style="color: #0073BF; font-weight: bold;">
   *   TODO: make this constructor package-private to ensure that the backing map doesn't leak:
   *   replace it with a constructor that takes a supplier {@code Supplier<Map<R, HashCounter<C>>} for the backing map
   * </p>
   */
  public HashCounterTable(Map<R, HashCounter<C>> backingMap, Function<R, ? extends HashCounter<C>> rowFactory) {
    this.map = backingMap;
    this.rowFactory = rowFactory;
  }

  /**
   * Adds the given delta to the counter indexed by the given row and col keys and returns its previous value.
   * @return the previous count
   */
  public synchronized int add(R rowKey, C colKey, int delta) {
    return map.computeIfAbsent(rowKey, rowFactory).add(colKey, delta);
  }

  /**
   * Increments the counter indexed by the given row and col keys and returns its previous value.
   * @return the previous count
   */
  public synchronized int increment(R rowKey, C colKey) {
    return add(rowKey, colKey, 1);
  }

  /**
   * @return the value of the counter indexed by the given row and col keys
   */
  public synchronized int getValue(R rowKey, C colKey) {
    HashCounter<C> rowCounter = map.get(rowKey);
    if (rowCounter != null) {
      return rowCounter.get(colKey);
    }
    return 0;
  }

  /**
   * @return the sum of all the counts in the given row
   */
  public synchronized int getValue(R rowKey) {
    HashCounter<C> rowCounter = map.get(rowKey);
    if (rowCounter != null) {
      return rowCounter.sumOfAllEntries();
    }
    return 0;
  }

  /**
   * @return the internal map of {@link HashCounter} instances. <strong>Not a defensive copy</strong>.
   */
  public Map<R, HashCounter<C>> getMap() {
    return map;
  }

  public synchronized Map<R, Map<C, Integer>> asMap() {
    ImmutableMap.Builder<R, Map<C, Integer>> mapBuilder = ImmutableMap.builder();
    for (Map.Entry<R, HashCounter<C>> entry : map.entrySet()) {
      mapBuilder.put(entry.getKey(), entry.getValue().asMap());
    }
    return mapBuilder.build();
  }

  @Override
  public synchronized void merge(HashCounterTable<R, C> other) {
    for (Map.Entry<R, HashCounter<C>> entry : other.map.entrySet()) {
      map.computeIfAbsent(entry.getKey(), rowFactory).merge(entry.getValue());
    }
  }
}
