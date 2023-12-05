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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Decorates a {@link Map} such that {@link #get(Object)} inserts the result of {@link #computeDefault(Object)}
 * for any key that's not already contained by the encapsulated map.
 * <p>
 * <em>Note</em>: This class is not strictly compatible with the Java Collections Framework because it
 * changes the semantics of {@link Map#get} to behave like {@link Map#computeIfAbsent},
 * making this class more similar to Python's
 * <a href="https://docs.python.org/3/library/collections.html#collections.defaultdict">defaultdict</a>
 * than a typical Java {@link Map}.
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

  /**
   * Returns the value to which the specified key is mapped, or, in the absence of such a mapping, returns the result
   * produced by {@link #computeDefault(Object)}, after entering it into this map as the value for the given key
   * (unless {@code null}).
   *
   * @return the current (existing or computed) value associated with the specified key,
   *         or {@code null} if the computed value is {@code null}
   */
  @Override
  @SuppressWarnings("unchecked")
  public V get(Object key) {
    return getOrInsert((K)key);
  }

  private V getOrInsert(K key) {
    // delegating to computeIfAbsent in order to ensure atomicity when delegate is a ConcurrentMap
    return computeIfAbsent(key, this::computeDefault);
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

  /*
  NOTE: We must explicitly delegate all the optional Map methods whose default implementation (in Map.java) assumes
        a spec-compliant Map.get implementation (which has different semantics in our case).
        The following methods are not automatically forwarded to the delegate by ForwardingMap (our superclass),
        and not doing so could result in bugs, errors (e.g. infinite recursion), and concurrency issues
        (e.g. ConcurrentMap guarantees atomicity for these methods, unlike the default implementations in Map).

        TODO: unit test and verify the semantics of delegating these methods in terms of compatibility with this class
  */

  @Override
  public V getOrDefault(Object key, V defaultValue) {
    return delegate.getOrDefault(key, defaultValue);
  }

  @Override
  public V computeIfAbsent(K key, @Nonnull Function<? super K, ? extends V> mappingFunction) {
    return delegate.computeIfAbsent(key, mappingFunction);
  }

  @Nullable
  @Override
  public V putIfAbsent(K key, V value) {
    return delegate.putIfAbsent(key, value);
  }

  @Override
  public V computeIfPresent(K key, @Nonnull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    return delegate.computeIfPresent(key, remappingFunction);
  }

  @Override
  public V compute(K key, @Nonnull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    return delegate.compute(key, remappingFunction);
  }

  @Override
  public V merge(K key, @Nonnull V value, @Nonnull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
    return delegate.merge(key, value, remappingFunction);
  }

  @Override
  public boolean remove(Object key, Object value) {
    return delegate.remove(key, value);
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    return delegate.replace(key, oldValue, newValue);
  }

  @Nullable
  @Override
  public V replace(K key, V value) {
    return delegate.replace(key, value);
  }
}
