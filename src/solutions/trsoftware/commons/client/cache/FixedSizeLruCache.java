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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implements an LRU cache which starts evicting the oldest-put entries
 * after the size limit (in number of entries) is exceeded.
 *
 * This class decorates LinkedHashMap with fixed-size logic.
 *
 * This class must be synchronized externally.  There is no way to avoid
 * locking, because an LRU cache cannot be built using the java.util.concurrent
 * classes. (Trust me, I spent a lot of time trying).
 *
 * @author Alex
 */
public class FixedSizeLruCache<K, V> extends LinkedHashMap<K,V> {
  private int sizeLimit;

  public FixedSizeLruCache(int initialCapacity, float loadFactor, int sizeLimit) {
    super(initialCapacity, loadFactor, true);  // the third arg (true) makes the LinkedHashMap enforce the LRU property
    this.sizeLimit = sizeLimit;
  }

  public FixedSizeLruCache(int sizeLimit) {
    this(16, .75f, sizeLimit);
  }

  /** No-arg constructor to support {@link java.io.Serializable} */
  private FixedSizeLruCache() {
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    return size() > sizeLimit;
  }

  public int getSizeLimit() {
    return sizeLimit;
  }

  public void setSizeLimit(int sizeLimit) {
    this.sizeLimit = sizeLimit;
  }
}