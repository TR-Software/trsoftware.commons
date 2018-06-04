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

import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;
import solutions.trsoftware.commons.shared.util.callables.Function0;
import solutions.trsoftware.commons.shared.util.callables.Function1;
import solutions.trsoftware.commons.shared.util.callables.Function2;
import solutions.trsoftware.commons.shared.util.stats.Mergeable;

import java.util.*;

/**
 * Date: Jun 6, 2008 Time: 2:15:43 PM
 *
 * @author Alex
 */
public class MapUtils {

  /**
   * Creates a {@link TreeMap} from the given args (treated as {@code key1, value1, key2, value2, ...})
   * @deprecated use {@link MapDecorator} for type-safety
   */
  public static <K,V> SortedMap<K, V> sortedMap(Object... keyValuePairs) {
    return putAll(new TreeMap<K, V>(), keyValuePairs);
  }

  /**
   * Creates a {@link HashMap} from the given args (treated as {@code key1, value1, key2, value2, ...})
   * @deprecated use {@link MapDecorator} for type-safety
   */
  public static <K,V> HashMap<K, V> hashMap(Object... keyValuePairs) {
    return putAll(new HashMap<K, V>(), keyValuePairs);
  }

  /**
   * Creates a {@link LinkedHashMap} from the given args (treated as {@code key1, value1, key2, value2, ...})
   * @deprecated use {@link #linkedHashMapBuilder()} for type-safety
   */
  public static <K,V> LinkedHashMap<K, V> linkedHashMap(Object... keyValuePairs) {
    return putAll(new LinkedHashMap<K, V>(), keyValuePairs);
  }

  /**
   * @return a new {@link MapDecorator} instance for building a {@link LinkedHashMap} using method chaining.
   */
  public static <K, V> MapDecorator<K, V> linkedHashMapBuilder() {
    return new MapDecorator<>(new LinkedHashMap<>());
  }

  /** This a frequently-used special case of {@link #hashMap(Object...)}, for String keys and values */
  public static HashMap<String, String> stringMap(String... keyValuePairs) {
    return hashMap((Object[])keyValuePairs);
  }

  /** This a frequently-used special case of {@link #linkedHashMap(Object...)}, for String keys and values */
  public static LinkedHashMap<String, String> stringLinkedHashMap(String... keyValuePairs) {
    return linkedHashMap((Object[])keyValuePairs);
  }

  /** Creates a sorted map of the given the args in order key1, value2, key2, value2, ... */
  public static <K extends Enum<K>,V> EnumMap<K, V> enumMap(Class<K> enumClass, Object... keyValuePairs) {
    return putAll(new EnumMap<K, V>(enumClass), keyValuePairs);
  }

  /**
   * Adds the given items to the given map instance.
   * @param keyValuePairs should be K key1, V value1, K key2, V value2, ...
   *
   */
  public static <M extends Map<K,V>,K,V> M putAll(M map, Object... keyValuePairs) {
    int n = keyValuePairs.length;
    if (n % 2 == 1)
      throw new IllegalArgumentException("Even number of args required.");
    for (int i = 0; i < n; i+=2)
      map.put((K)keyValuePairs[i], (V)keyValuePairs[i+1]);
    return map;
  }

  /**
   * Creates a map that incorporates all the entries in the given maps.  If a map contains a key that is present
   * in a previous map (according to the order of the arguments to this method), then that key will be overwritten.
   * @return A new map that contains all the entries from the given maps, in the same order.
   */
  public static <M extends Map<K,V>, K, V> LinkedHashMap<K, V> union(M... maps) {
    LinkedHashMap<K, V> ret = new LinkedHashMap<K, V>();
    for (M map : maps)
      ret.putAll(map);
    return ret;
  }

  /**
   * Adds the entries from the second map to the first map, merging values that already exist (if the value type
   * implements {@link Mergeable}).
   * @param map the map to which entries will be added.
   * @param map2 the entries from this map will be added to the first map, if the values implement {@link Mergeable},
   * then instead of overwriting values in the first map, the values from the second map will be merged.
   * @return the map to which entries were added (i.e. the first argument: {@code map}), for method chaining.
   */
  public static <M extends Map<K,V>, K, V extends Mergeable<V>> M unionMerge(M map, M map2) {
    for (K k : map2.keySet()) {
      V map1Value = map.get(k);
      V map2Value = map2.get(k);
      if (map1Value != null && map2Value != null)
        map1Value.merge(map2Value);
      else
        map.put(k, map2Value);
    }
    return map;
  }

  /**
   * Removes all mappings whose values are null
   * @return the same map, post-modification.
   */
  public static <K,V> Map<K,V> removeNullValues(Map<K,V> map) {
    Iterator<K> mapKeyIter = map.keySet().iterator();
    while (mapKeyIter.hasNext()) {
      K k = mapKeyIter.next();
      if (map.get(k) == null)
        mapKeyIter.remove();
    }
    return map;
  }

  /**
   * Removes all the keys from the given map that aren't in the given set.
   * Returns the same map post-modification.
   * See MapUtils.filterMap(Map<K,V>, Set<K>) for the same operation that
   * doesn't modify the given map. 
   */
  public static <K,V> Map<K,V> retainAll(Map<K,V> map, Set<K> keysToRetain) {
    Iterator<K> mapKeyIter = map.keySet().iterator();
    while (mapKeyIter.hasNext()) {
      K k = mapKeyIter.next();
      if (!keysToRetain.contains(k))
        mapKeyIter.remove();
    }
    return map;
  }

  /**
   * Creates a new HashMap with all the keys from the given map that aren't in
   * the given set.
   *
   * Similar to retainAll, except the underlying map is not modified.
   *
   * @return the new map
   */
  public static <K,V> Map<K,V> filterMap(Map<K,V> map, Set<K> keysToRetain) {
    HashMap<K,V> ret = new HashMap<K,V>(keysToRetain.size()*2);
    for (K k : keysToRetain) {
      if (map.containsKey(k))
        ret.put(k, map.get(k));    
    }
    return ret;
  }

  /**
   * Puts the given key, value pair into the given map and returns the map,
   * to allow method chaining.
   *
   * @return the same map that was passed in; post-modification
   */
  public static <K,V> Map<K,V> put(Map<K,V> map, K key, V value) {
    map.put(key, value);
    return map;
  }

  /**
   * The map may be a String->String mapping or a String->String[] mapping.
   * If it's the former, this method is equivalent to Map.get, otherwise
   * it returns the first element of the array returned by Map.get on the
   * given map.
   *
   * This is useful because the Java Servlet API allows for multiple values
   * for all URL parameters and treats them all as arrays.
   *
   * @return the first mapping for the given key, or {@code null} if the mapping doesn't exist.
   */
  public static String extractSingleValue(Map<String, String[]> paramMap, String key) {
    Object value = paramMap.get(key);
    if (value instanceof String)
      return (String)value;
    else if (value instanceof String[]) {
      String[] arr = (String[])value;
      if (!ArrayUtils.isEmpty(arr))
        return arr[0];
      return null;
    }
    return null;  // value neither String nor String[]
  }

  /**
   * Inserts the given element into the map if the map doesn't already contain it.
   * @return The given value if it was inserted, or the previous value if it
   * was already contained by the map. 
   */
  public static <K,V> V getOrInsert(Map<K,V> map, K key, V value) {
    if (map.containsKey(key))
      return map.get(key);
    map.put(key, value);
    return value;
  }

  /**
   * Returns a default value if the given key is not present in the map,
   * but does not insert it.
   * @return The given value if it's not in the map, or the previous value if it
   * was already contained by the map.
   */
  public static <K,V> V getOrDefault(Map<K,V> map, K key, V defaultValue) {
    if (map.containsKey(key))
      return map.get(key);
    return defaultValue;
  }

  // TODO: write a GWT generator that will instantiate a class based on its literal - can get rid of many factories that way:

  /**
   * This version of getOrInsert is for values that are expensive to create,
   * and allows passing in a factory method which will be invoked only if needed. 
   * @return The value created by the factory if it was inserted,
   * or the previous value if it was already contained by the map.
   */
  public static <K,V> V getOrInsert(Map<K,V> map, K key, Function0<V> factory) {
    if (map.containsKey(key))
      return map.get(key);
    V value = factory.call();
    map.put(key, value);
    return value;
  }

  /**
   * This version of getOrInsert supports multi-maps (maps storing lists as values).
   * @return The given value if it was inserted, or the previous value if it
   * was already contained by the map.
   */
  public static <K,V> List<V> getOrInsertList(Map<K,List<V>> map, K key) {
    if (map.containsKey(key))
      return map.get(key);
    List<V> value = new ArrayList<V>();
    map.put(key, value);
    return value;
  }

  /**
   * This version of getOrInsert is for values that are expensive to create,
   * and allows passing in a factory method which, if needed, will be invoked with the given argument.
   * @return The value created by the factory if it was inserted,
   * or the previous value if it was already contained by the map.
   */
  public static <K,V,A> V getOrInsert(Map<K,V> map, K key, Function1<A, V> factory, A factoryArg) {
    if (map.containsKey(key))
      return map.get(key);
    V value = factory.call(factoryArg);
    map.put(key, value);
    return value;
  }

  /**
   * This version of getOrInsert is for values that are expensive to create,
   * and allows passing in a factory method which will be invoked only if needed.
   * @return The value created by the factory if it was inserted,
   * or the previous value if it was already contained by the map.
   */
  public static <K,A1,A2,V> V getOrInsert(Map<K,V> map, K key, Function2<A1, A2, V> factory, A1 factoryArg1, A2 factoryArg2) {
    if (map.containsKey(key))
      return map.get(key);
    V value = factory.call(factoryArg1, factoryArg2);
    map.put(key, value);
    return value;
  }

  /**
   * Removes all the entries matching predicate from the given map.
   * Returns the same map to allow method chaining.
   */
  public static <K,V> Map<K, V> removeMatchingEntries(Map<K,V> map, Predicate<Map.Entry<K,V>> predicate) {
    Iterator<Map.Entry<K, V>> entryIterator = map.entrySet().iterator();
    while (entryIterator.hasNext()) {
      Map.Entry<K, V> entry = entryIterator.next();
      if (predicate.apply(entry))
        entryIterator.remove();
    }
    return map;
  }

  /**
   * Adds all the entries in the given {@link Map} to the given {@link Multimap}.
   * @param to the recipient of the data
   * @param from the source of the data
   * @return the same {@link Multimap} instance that was passed in
   */
  public static <K,V> Multimap<K, V> putAllToMultimap(Multimap<K, V> to, Map<K, V> from) {
    for (Map.Entry<K, V> entry : from.entrySet()) {
      to.put(entry.getKey(), entry.getValue());
    }
    return to;
  }
}
