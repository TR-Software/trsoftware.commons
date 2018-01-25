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

package solutions.trsoftware.commons.shared.util;

import java.util.Map;

/**
 * Provides a type-safe alternative to the factory methods in {@link MapUtils}, e.g. {@link MapUtils#hashMap(Object...)}
 *
 * @author Alex
 * @since 11/11/2017
 */
public class MapBuilder<K, V> {

  private Map<K, V> map;

  public MapBuilder(Map<K, V> delegate) {
    this.map = delegate;
  }

  public MapBuilder<K, V> put(K key, V value) {
    map.put(key, value);
    return this;
  }

  public Map<K, V> getMap() {
    return map;
  }

}
