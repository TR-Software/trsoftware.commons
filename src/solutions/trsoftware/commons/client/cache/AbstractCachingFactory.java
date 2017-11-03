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

package solutions.trsoftware.commons.client.cache;

import solutions.trsoftware.commons.bridge.BridgeTypeFactory;
import solutions.trsoftware.commons.client.bridge.util.Duration;
import solutions.trsoftware.commons.client.util.stats.Mean;

/**
 * Caches the most recent results of a computation performed on two strings.
 * The computation is implemented by inheriting classes.
 *
 * TODO: it would probably make sense to replace this class with {@link com.google.common.cache.LoadingCache}
 *
 * Apr 21, 2011
 *
 * @author Alex
 */
public abstract class AbstractCachingFactory<K, V> {

  /** A cache of past results */
  protected FixedSizeLruCache<K, V> cache;
  // fields for stats gathering
  private int hits;
  private int misses;
  /** Mean time taken to compute the result */
  private Mean<Double> meanDuration = new Mean<Double>();

  protected AbstractCachingFactory() {
    cache = new FixedSizeLruCache<K, V>(16);
  }

  public V compute(K key) {
    if (cache.containsKey(key)) {
      hits++;
      return cache.get(key);
    }
    else {
      misses++;
      Duration duration = BridgeTypeFactory.newDuration();
      V result = _compute(key);
      meanDuration.update(duration.elapsedMillis());
      cache.put(key, result);
      return result;
    }
  }

  protected abstract V _compute(K key);

  public String printStats() {
    int n = getAccessCount();
    return " access count: " + n + "\n"
        + " hit rate: " + ((double)hits/n) + "\n"
        + " mean computation duration: " + meanDuration.getMean() + " ms";
  }

  public int getAccessCount() {
    return hits + misses;
  }

  public int getHits() {
    return hits;
  }

  public int getMisses() {
    return misses;
  }

  public int getCacheSize() {
    return cache.size();
  }

  public int getSizeLimit() {
    return cache.getSizeLimit();
  }
}