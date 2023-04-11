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

package solutions.trsoftware.commons.shared.util.collections;

import com.google.common.collect.ForwardingMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Decorates a {@link Map} such that {@link #get(Object)} inserts the result of {@link #computeDefault(Object)}
 * for any key that's not already contained by the encapsulated map.
 * <p>
 * NOTE: Java 8 provides a new method that serves a similar purpose: {@link Map#computeIfAbsent}
 *
 * @see #fromSupplier(Supplier)
 * @author Alex, 2/24/2016
 */
public abstract class DefaultMap<K, V> extends ForwardingMap<K, V> {

  private final Map<K,V> delegate;

  protected DefaultMap() {
    this(new LinkedHashMap<>());
  }

  protected DefaultMap(Map<K, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  protected Map<K, V> delegate() {
    return delegate;
  }

  /** Compute the default value for the given key */
  public abstract V computeDefault(K key);

  @Override
  @SuppressWarnings("unchecked")
  public V get(Object key) {
    return getOrInsert((K)key);
  }

  private V getOrInsert(K key) {
    if (!containsKey(key)) {
      put(key, computeDefault(key));
    }
    return delegate.get(key);
  }

  public static <K, V> DefaultMap<K, V> fromSupplier(Supplier<V> defaultValueSupplier) {
    return fromSupplier(new LinkedHashMap<>(), defaultValueSupplier);
  }

  public static <K, V> DefaultMap<K, V> fromSupplier(Map<K, V> delegate, Supplier<V> defaultValueSupplier) {
    return new DefaultMap<K, V>(delegate) {
      @Override
      public V computeDefault(K key) {
        return defaultValueSupplier.get();
      }
    };
  }

}
