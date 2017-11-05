/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.server.stats;

import solutions.trsoftware.commons.server.cache.FixedTimeCache;
import solutions.trsoftware.commons.server.util.Clock;
import solutions.trsoftware.commons.shared.util.MapUtils;
import solutions.trsoftware.commons.shared.util.callables.Function0;
import solutions.trsoftware.commons.shared.util.mutable.MutableInteger;

/**
 * A counter that keeps its entries for a limited amount of time.
 * Can be used to implement a counter for events occurring in a given
 * moving time window (e.g. past hour).
 *
 * The granularity can be increased to use less memory
 * (by default a separate count is stored for every millisecond).
 *
 * Uses O(T/g) memory, where T is the time window and g is the granularity.
 * Updates are O(m), where m is the number of updates performed in the prior (expiring) window.
 * Reads are O(n), where n is the number of updates performed in the recent time window.
 * 
 *
 * @author Alex
 */
public class TimeWindowCounter extends Counter {
  private final FixedTimeCache<Long,MutableInteger> map;
  private final long maxAgeMillis;
  private final long granularityMillis;

  private final Function0<MutableInteger> newValueFactory = new Function0<MutableInteger>() {
    public MutableInteger call() {
      return new MutableInteger(0);
    }
  };

  /**
   * @param maxAgeMillis The value of the counter, retrieved using the get method
   * will be representative of the calls to increment and add over the most recent
   * time window of this size.
   */
  public TimeWindowCounter(long maxAgeMillis) {
    this(null, maxAgeMillis);
  }

  /**
   * @param name Just to facilitate pretty printing, external reporting, and debugging.
   * @param maxAgeMillis The value of the counter, retrieved using the get method
   * will be representative of the calls to increment and add over the most recent
   * time window of this size.
   */
  public TimeWindowCounter(String name, long maxAgeMillis) {
    this(name, maxAgeMillis, 1);
  }

  /**
   * @param name Just to facilitate pretty printing, external reporting, and debugging.
   * @param maxAgeMillis The value of the counter, retrieved using the get method
   * will be representative of the calls to increment and add over the most recent
   * time window of this size.
   * @param granularityMillis The time window will be measure in blocks of this size.
   * This value should be smaller than maxAgeMillis and maxAgeMillis should be
   * divisible by this value.  The tradeoff is: higer granularity => less memory
   * consumption; lower granularity => better time window accuracy.
   */
  public TimeWindowCounter(String name, long maxAgeMillis, long granularityMillis) {
    super(name);
    this.maxAgeMillis = maxAgeMillis;
    if (granularityMillis < 1 || granularityMillis >= maxAgeMillis || maxAgeMillis % granularityMillis != 0) {
      throw new IllegalArgumentException("granularityMillis must be in the range [1, maxAgeMillis) and must evenly divide maxAgeMillis.");
    }
    this.granularityMillis = granularityMillis;
    map = new FixedTimeCache<Long, MutableInteger>(maxAgeMillis);
  }

  /** Adds the given value to the counter */
  public void add(int delta) {
    Long key = Clock.currentTimeMillis() / granularityMillis;
    MutableInteger value;
    synchronized (map) {
      value = MapUtils.getOrInsert(map, key, newValueFactory);
    }
    synchronized (value) {
      value.getAndAdd(delta);
    }
  }

  /** @return The value of the counter. */
  public int getCount() {
    return sumOfAllEntries();
  }

  private int sumOfAllEntries() {
    synchronized (map) {
      int sum = 0;
      for (Long time : map.keySet()) {
        sum += map.get(time).get();
      }
      return sum;
    }
  }

  /**
   * Exposed for unit testing.
   * @return number of underlying entries in use.
   */
  int size() {
    return map.size();
  }

  public long getMaxAgeMillis() {
    return maxAgeMillis;
  }

  public long getGranularityMillis() {
    return granularityMillis;
  }
}
