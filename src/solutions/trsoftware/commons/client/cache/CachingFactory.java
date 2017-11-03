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

import solutions.trsoftware.commons.client.util.callables.Function1;

import java.util.HashMap;

/**
 * A cache of unlimited size.  Basically a thin wrapper around a HashMap, with
 * some convenience operations.
 *
 * TODO: can probably replace this class by the newer {@link solutions.trsoftware.commons.shared.util.collections.DefaultMap}
 * or even {@link com.google.common.cache.LoadingCache}
 *
 * @author Alex
 */
public class CachingFactory<K, V> {
  private final HashMap<K, V> cache = new HashMap<K, V>();

  private Function1<K, V> factoryMethod;

  public CachingFactory(Function1<K, V> factoryMethod) {
    this.factoryMethod = factoryMethod;
  }

  public V getOrInsert(K key) {
    synchronized (cache) {
      if (cache.containsKey(key))
        return cache.get(key);
      V newValue = factoryMethod.call(key);
      cache.put(key, newValue);
      return newValue;
    }
  }
}
