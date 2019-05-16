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

package solutions.trsoftware.commons.shared.util;

import java.util.Map;

/**
 * Wraps a {@link Map} to allow adding multiple new entries in one expression (via method chaining).
 * <p>
 * This is a type-safe alternative to the factory methods in {@link MapUtils}, e.g. {@link MapUtils#hashMap(Object...)}
 * @see MapUtils#linkedHashMapBuilder()
 * @see com.google.common.collect.ImmutableMap#builder()
 * @author Alex
 * @since 11/11/2017
 */
public class MapDecorator<K, V> {

  private Map<K, V> map;

  public MapDecorator(Map<K, V> delegate) {
    this.map = delegate;
  }

  public MapDecorator<K, V> put(K key, V value) {
    map.put(key, value);
    return this;
  }

  public Map<K, V> getMap() {
    return map;
  }

}
