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

package solutions.trsoftware.commons.shared.util.collections;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import solutions.trsoftware.commons.shared.util.Assert;

import java.util.*;

/**
 * An implementation of {@link Map} that allows lookup of keys via the {@link #findKeys(Enum, Object)} by
 * using additional secondary indexes computed on the values by the {@link Indexer} instance specified at creation time.
 * Therefore this class defines a {@link Map} that behaves like a database table.
 *
 * The set of possible indexes is specified by the {@code enum} {@link I}.
 *
 * It's important to note that the index entries for each key-value pair are computed only once when the value
 * is inserted with {@link #put(Object, Object)}, and deleted when the mapping is removed.  Therefore if the value
 * changes externally, you have to re-insert it by calling {@link #put(Object, Object)} again to update the indexes.
 * TODO: document this behavior
 *
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * Access must be synchronized externally the same way as any other {@link Map}, like {@link HashMap}.
 * If you wrap it with {@link Collections#synchronizedMap(Map)}, you have to still externally synchronize
 * the iterators and the additional methods provided by this class that are not present in {@link Map}
 * (e.g. {@link #findKeys(Enum, Object)}).
 *
 * @author Alex, 2/24/2016
 */
public class IndexingMap<K, V, I extends Enum<I>> extends AbstractMap<K, V> {

  public interface Indexer<K, V, I extends Enum<I>> {
    /** @return a new instance of the {@link Multimap} implementation to be used for storing the given index */
    Multimap<Object, K> createIndex(I indexSpec);

    /**
     * @return the value for the index entry associated with the given key-value pair.  This value will be used
     * as the key for the {@link Multimap} returned for this index by the {@link #createIndex(Enum)} method.
     */
    Object computeIndexValue(I indexSpec, K key, V value);
  }

  public static abstract class HashIndexer<K, V, I extends Enum<I>> implements Indexer<K, V, I> {
    @Override
    public Multimap<Object, K> createIndex(I indexSpec) {
      return HashMultimap.create();
    }
  }

  /*
   Alternatives to this design that were considered:
   1) primaryIndex = LinkedHashMap<K, V>
      secondaryIndexes = Multimap<IndexEntry, K> where IndexEntry is a (I, Object) pair
      reverseIndex = Multimap<K, IndexEntry>
   1b)
      primaryIndex = LinkedHashMap<K, ValueWrapper> where ValueWrapper is a (V, List<IndexEntry>) pair
   However, putting the entire secondary index into a single map, while making the code cleaner, wouldn't allow us
   to use different structures for each index - for example the user might want to have some indexes sorted
   (implemented as a TreeMap) and some hashed (implemented as a HashMap).

   Future enhancements:
     - support returning an iterator from a particular index (e.g. SELECT * FROM X ORDER BY y)
     - support ORM-like features that derive index values by reflection and update the indexes dynamically by
       using a dynamic proxy (see ObjectToFileMapping, Proxy, and InvocationHandler)
  */

  private final Class<I> indexEnumClass;
  private final Indexer<K, V, I> indexer;
  private Map<K, V> primaryIndex = new LinkedHashMap<K, V>();
  private EnumMap<I, Multimap<Object, K>> secondaryIndexes;
  /** Tells us which secondary index entries need to be removed when an entry is removed from the primary index */
  private DefaultMap<K, EnumMap<I, Object>> reverseIndex;
  private Set<Entry<K, V>> entrySet;


  protected IndexingMap(final Class<I> indexEnumClass, Indexer<K, V, I> indexer) {
    this.indexEnumClass = indexEnumClass;
    this.indexer = indexer;
    secondaryIndexes = new EnumMap<I, Multimap<Object, K>>(indexEnumClass);
    reverseIndex = new DefaultMap<K, EnumMap<I, Object>>() {
      @Override
      public EnumMap<I, Object> computeDefault(K key) {
        return new EnumMap<I, Object>(indexEnumClass);
      }
    };
  }

  /** @return All the keys in this map associated with the given value in the given index */
  public Collection<K> findKeys(I indexSpec, Object indexValue) {
    Multimap<Object, K> index = secondaryIndexes.get(indexSpec);
    if (index == null)
      return Collections.emptySet();
    return index.get(indexValue);
  }

  @Override
  public V put(K key, V value) {
    Assert.assertNotNull(key);
    removeSecondaryIndexEntries(key); // remove the old index entries (if any) first
    V ret = primaryIndex.put(key, value);
    // update the secondaryIndexes
    for (I indexSpec : indexEnumClass.getEnumConstants()) {
      Object indexValue = indexer.computeIndexValue(indexSpec, key, value);
      if (indexValue != null) {
        getOrCreateIndex(indexSpec).put(indexValue, key);
        reverseIndex.get(key).put(indexSpec, indexValue);
      }
    }
    return ret;
  }

  private Multimap<Object, K> getOrCreateIndex(I indexSpec) {
    Multimap<Object, K> index = secondaryIndexes.get(indexSpec);
    if (index == null)
      secondaryIndexes.put(indexSpec, index = indexer.createIndex(indexSpec));
    return index;
  }

  @Override
  public V remove(Object key) {
    V value = primaryIndex.remove(key);
    if (value != null) {
      removeSecondaryIndexEntries(key);
    }
    return value;
  }

  private void removeSecondaryIndexEntries(Object key) {
    // remove the corresponding entry from every index
    EnumMap<I, Object> indexEntries = reverseIndex.remove(key);
    if (indexEntries != null) {
      for (Entry<I, Object> entry : indexEntries.entrySet()) {
        secondaryIndexes.get(entry.getKey()).remove(entry.getValue(), key);
      }
    }
  }

  // We override the methods provided by AbstractMap that can be made more efficient by delegating directly to primaryIndex

  @Override
  public V get(Object key) {
    return primaryIndex.get(key);
  }

  @Override
  public boolean containsKey(Object key) {
    return primaryIndex.containsKey(key);
  }

  @Override
  public int size() {
    return primaryIndex.size();
  }

  @Override
  public boolean containsValue(Object value) {
    return primaryIndex.containsValue(value);
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return entrySet != null ? entrySet : (entrySet = new EntrySet());
  }


  private final class EntrySet extends AbstractSet<Entry<K, V>> {
    private Set<Entry<K, V>> primaryIndexEntrySet = primaryIndex.entrySet();

    public Iterator<Entry<K, V>> iterator() {
      return new Iterator<Entry<K, V>>() {
        private Iterator<Entry<K, V>> it = primaryIndexEntrySet.iterator();
        private Entry<K, V> next;

        @Override
        public boolean hasNext() {
          return it.hasNext();
        }

        @Override
        public Entry<K, V> next() {
          return next = it.next();
        }

        @Override
        public void remove() {
          // hook the primaryIndex iterator's remove operation to update our secondary indexes
          it.remove();
          removeSecondaryIndexEntries(next.getKey());
        }
      };
    }

    @Override
    public int size() {
      return primaryIndexEntrySet.size();
    }
  }

  /** Method exposed for unit testing */
  boolean sanityCheck() {
    if (isEmpty()) {
      // the primary index should be empty and all the secondary indexes should be empty (or null)
      if (!primaryIndex.isEmpty() || !reverseIndex.isEmpty())
        return false;
      for (I indexSpec : indexEnumClass.getEnumConstants()) {
        Multimap<Object, K> secondaryIndex = secondaryIndexes.get(indexSpec);
        if (secondaryIndex != null && !secondaryIndex.isEmpty())
          return false;
      }
    }
    else {
      for (K key : primaryIndex.keySet()) {
        EnumMap<I, Object> reverseIndexEntries = reverseIndex.get(key);
        // the secondary indexes should contain all the entries from the reverseIndex for this key
        for (Entry<I, Object> reverseIndexEntry : reverseIndexEntries.entrySet()) {
          if (!secondaryIndexes.get(reverseIndexEntry.getKey()).containsEntry(reverseIndexEntry.getValue(), key))
            return false;
        }
        // conversely, the secondary indexes should not contain any entries that are not in the reverseIndex for this key
        for (I indexSpec : EnumSet.complementOf(EnumSet.copyOf(reverseIndexEntries.keySet()))) {
          Multimap<Object, K> secondaryIndex = secondaryIndexes.get(indexSpec);
          if (secondaryIndex != null && secondaryIndex.containsValue(key))
            return false;
        }
      }
    }
    return true;
  }


  // TODO: implement an ObjectDatabase class on top of this
  // TODO: use this to implement Model.rooms with a secondary index for owner UID

}
