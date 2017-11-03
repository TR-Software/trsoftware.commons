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
