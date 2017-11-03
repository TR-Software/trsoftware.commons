package solutions.trsoftware.commons.shared.util.collections;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Decorates a {@link Map} such that {@link #get(Object)} invokes the private method {@link #getOrInsert(Object)},
 * which inserts the result of {@link #computeDefault(Object)} for any key that's not already contained by the
 * encapsulated map.
 *
 * @author Alex, 2/24/2016
 */
public abstract class DefaultMap<K, V> implements Map<K, V> {

  private final Map<K,V> delegate;

  public DefaultMap() {
    this(new LinkedHashMap<K, V>());
  }

  protected DefaultMap(Map<K, V> delegate) {
    this.delegate = delegate;
  }

  /** Compute the default value for the given key */
  public abstract V computeDefault(K key);

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return delegate.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return delegate.containsValue(value);
  }

  @Override
  public V get(Object key) {
    return getOrInsert((K)key);
  }

  private V getOrInsert(K key) {
    if (!containsKey(key))
      put(key, computeDefault(key));
    return delegate.get(key);
  }

  public V put(K key, V value) {
    return delegate.put(key, value);
  }

  @Override
  public V remove(Object key) {
    return delegate.remove(key);
  }

  public void putAll(Map<? extends K, ? extends V> m) {
    delegate.putAll(m);
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  @Override
  public Set<K> keySet() {
    return delegate.keySet();
  }

  @Override
  public Collection<V> values() {
    return delegate.values();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return delegate.entrySet();
  }

  @Override
  public boolean equals(Object o) {
    return delegate.equals(o);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }
}
