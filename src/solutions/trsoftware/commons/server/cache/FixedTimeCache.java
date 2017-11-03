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

package solutions.trsoftware.commons.server.cache;

import solutions.trsoftware.commons.server.util.Clock;

import java.util.*;

/**
 * Implements a cache which evicts entries that were added more than the threshold
 * time interval ago.  Getting or updating keys does not change their expiration
 * time, that is, this cache is not LRU and doesn't care about any of its
 * entries being touched before expiring them.
 *
 * The eviction is ammortized - performed on every get and put operation.
 *
 * This class decorates LinkedHashMap with fixed-time logic.
 *
 * This class must be synchronized externally.  There is no way to avoid
 * locking, because a cache cannot be built using the java.util.concurrent
 * classes. (Trust me, I spent a lot of time trying).
 *
 * @author Alex
 */
public class FixedTimeCache<K,V> implements Map<K,V> {

  private final long maxAge;

  private LinkedHashMap<K, FixedTimeCacheValue<V>> delegate;

  /**
   * This value will be maintained to speed up all amortized checks to remove
   * expired values (these checks are costly because they require construction
   * of an iterator over the map).
   */
  private long earliestExpirationTime = Long.MAX_VALUE;

  /**
   *
   * @param initialCapacity The starting capacity of the underlying LHM (affects performace).
   * @param loadFactor The load factor of the underlying LHM (affects performace).
   * @param maxAge (millis) Entries will be evicted after this amount of time has
   * elapsed since they were put.
   */
  public FixedTimeCache(int initialCapacity, float loadFactor, long maxAge, final int maxCapacity) {
    // the third arg (false) makes the LinkedHashMap maintaing the original insertion order
    delegate = new LinkedHashMap<K, FixedTimeCacheValue<V>>(initialCapacity, loadFactor, false) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<K, FixedTimeCacheValue<V>> eldest) {
        return size() > maxCapacity || isExpired(eldest.getValue());
      }
    };
    this.maxAge = maxAge;
  }

  public FixedTimeCache(long maxAge, int maxCapacity) {
    this(16, .75f, maxAge, maxCapacity);
  }

  public FixedTimeCache(long maxAge) {
    this(maxAge, Integer.MAX_VALUE);
  }

  private boolean isExpired(FixedTimeCacheValue<V> value) {
    return value.getExpirationTime() < Clock.currentTimeMillis();
  }

  /** Removes all entries that were added more than maxAge ago */
  private void removeExpiredEntries() {
    if (Clock.currentTimeMillis() < earliestExpirationTime)
      return;  // the fast path
    Iterator<Entry<K, FixedTimeCacheValue<V>>> entryIterator = delegate.entrySet().iterator();
    while (entryIterator.hasNext()) {
      Entry<K, FixedTimeCacheValue<V>> entry = entryIterator.next();
      if (isExpired(entry.getValue())) {
//        System.out.printf("Entry (%s,%s) has expired at time %d.%n", entry.getKey(), entry.getValue().getValue(), Clock.currentTimeMillis());
        entryIterator.remove();
      }
      else {
        // this is the first entry that's not expired yet
        earliestExpirationTime = entry.getValue().getExpirationTime();
        return;
      }
    }
    // if we got this far, this means there are no unexpired entries left
    earliestExpirationTime = Long.MAX_VALUE;
  }

  public long getMaxAge() {
    return maxAge;
  }

  public void clear() {
    delegate.clear();
  }

  public boolean containsKey(Object key) {
    if (!delegate.containsKey(key)) {
      // the fast path: if the delegate map doesn't contain the key, removing expired entries is unnecessary
      return false;
    }
    else {
      // the slow path: the key could be expired, so we clean up the map and do the check again
      removeExpiredEntries();
      return delegate.containsKey(key);
    }
  }

  private FixedTimeCacheValue<V> wrap(V value) {
    long expiryTime = Clock.currentTimeMillis() + maxAge;
    if (expiryTime < earliestExpirationTime)
      earliestExpirationTime = expiryTime;
    return new FixedTimeCacheValue<V>(value, expiryTime);
  }
  
  private V unwrap(FixedTimeCacheValue<V> value) {
    if (value == null)
      return null;
    return value.getValue();
  }

  public V get(Object key) {
    return unwrap(getWithExpirationTime(key));
  }

  /**
   * @return an object that contains both the value and the time when the key key will expire,
   * or null if the key wasn't found.
   */
  public FixedTimeCacheValue<V> getWithExpirationTime(Object key) {
    if (!delegate.containsKey(key)) {
      // the fast path: if the delegate map doesn't contain the key, removing expired entries is unnecessary
      return null;
    }
    else {
      // the slow path: the key could be expired, so we clean up the map and do the fetch again
      removeExpiredEntries();
      return delegate.get(key);
    }
  }

  /**
   * If already contains a mapping for this key, the old mapping's
   * expiration time will be used for the new value.  (This is the only
   * way to preserve the invariants, because the underlying LHM is not
   * access-ordered).
   */
  public V put(K key, V value) {
    // the keySet iterator doesn't handle null keys properly
    if (key == null)
      throw new NullPointerException("FixedTimeCache doesn't support null keys");
    removeExpiredEntries();
    FixedTimeCacheValue<V> oldCacheValue = delegate.put(key, wrap(value));
    if (oldCacheValue != null) {
      // we must use the old value's expiration time to preserve the invariant
      // otherwise the delegate LHM will contain an entry with a later expiration
      // than a subsequent entry
      delegate.put(key, new FixedTimeCacheValue<V>(value, oldCacheValue.getExpirationTime()));
    }
    return unwrap(oldCacheValue);
  }

  public V remove(Object key) {
    return unwrap(delegate.remove(key));
  }

  public int size() {
    removeExpiredEntries();
    return delegate.size();
  }

  /**
   * This method is useful for unit testing.
   * @return Whether the underlying LHM contains any entries whose expirationTime
   * exceeds the curret clock time.
   */
  boolean anyExpiredEntries() {
    for (Entry<K, FixedTimeCacheValue<V>> entry : delegate.entrySet()) {
      if (isExpired(entry.getValue()))
        return true;
    }
    return false;
  }

  // the rest of these methods exist to support the Map interface; they are optional

  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.cache.FixedTimeCache.containsValue is not supported.");
  }

  public boolean isEmpty() {
    return size() <= 0;
  }

  public Set<K> keySet() {
    // because entries in the underlying map can "expire" (i.e. be removed at any time)
    // an iterator on the underlying collection can throw a CME, so we make a
    // copy of the keys and then iterate over them manually, checking for expiration each time
    return new AbstractSet<K>() {
      public Iterator<K> iterator() {
        return new Iterator<K>() {
          Iterator<K> copyOfKeySetIterator = new HashSet<K>(delegate.keySet()).iterator();
          K next = null;

          public boolean hasNext() {
            return findNextUnexpired();  // make sure the key not expired yet
          }

          private boolean findNextUnexpired() {
            while (copyOfKeySetIterator.hasNext()) {
              if (next == null)
                next = copyOfKeySetIterator.next();
              if (FixedTimeCache.this.containsKey(next))
                break;
              else
                next = null;  // this element is no longer contained
            }
            return next != null;
          }

          public K next() {
            if (!findNextUnexpired())
              throw new NoSuchElementException();
            K temp = next;
            next = null;
            return temp;
          }

          public void remove() {
            throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.cache.FixedTimeCache.entrySet().iterator().remove() is not supported.");

          }
        };
      }

      public int size() {
        return FixedTimeCache.this.size();
      }
    };
  }

  /** This method just calls put(key, value) in a loop. */
  public void putAll(Map<? extends K, ? extends V> m) {
    for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  // we chose not to support these two methods to avoid the hassle of unwrapping values

  public Set<Entry<K, V>> entrySet() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.cache.FixedTimeCache.entrySet is not supported.");
  }

  public Collection<V> values() {
    throw new UnsupportedOperationException("Method solutions.trsoftware.commons.server.cache.FixedTimeCache.values is not supported.");
  }
}