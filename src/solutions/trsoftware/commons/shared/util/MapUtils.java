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

package solutions.trsoftware.commons.shared.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import solutions.trsoftware.commons.shared.util.function.ThrowingFunction;
import solutions.trsoftware.commons.shared.util.stats.Mergeable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Date: Jun 6, 2008 Time: 2:15:43 PM
 *
 * @author Alex
 */
public class MapUtils {

  /**
   * Creates a {@link TreeMap} from the given key-value pairs (treated as {@code key1, value1, key2, value2, ...})
   * @deprecated use {@link MapDecorator} for type-safety
   */
  public static <K extends Comparable<K>, V> TreeMap<K, V> sortedMap(Object... keyValuePairs) {
    return putAll(new TreeMap<>(), keyValuePairs);
  }

  /**
   * Creates a {@link TreeMap} from the given key-value pair
   */
  public static <K extends Comparable<K>, V> TreeMap<K, V> sortedMap(K key, V value) {
    return putAll(new TreeMap<>(), key, value);
  }

  /**
   * Creates a {@link TreeMap} from the given key-value pairs
   */
  public static <K extends Comparable<K>, V> TreeMap<K, V> sortedMap(K key1, V value1, K key2, V value2) {
    return putAll(new TreeMap<>(), key1, value1, key2, value2);
  }

  /**
   * Creates a {@link TreeMap} from the given key-value pairs
   */
  public static <K extends Comparable<K>, V> TreeMap<K, V> sortedMap(K key1, V value1, K key2, V value2, K key3, V value3) {
    return putAll(new TreeMap<>(), key1, value1, key2, value2, key3, value3);
  }

  /**
   * Creates a {@link TreeMap} from the given key-value pairs
   */
  public static <K extends Comparable<K>, V> TreeMap<K, V> sortedMap(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4) {
    return putAll(new TreeMap<>(), key1, value1, key2, value2, key3, value3, key4, value4);
  }

  /**
   * Creates a {@link TreeMap} from the given key-value pairs
   */
  public static <K extends Comparable<K>, V> TreeMap<K, V> sortedMap(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5, V value5) {
    return putAll(new TreeMap<>(), key1, value1, key2, value2, key3, value3, key4, value4, key5, value5);
  }

  /**
   * Creates a {@link HashMap} from the given key-value pairs (treated as {@code key1, value1, key2, value2, ...})
   * @deprecated use {@link MapDecorator} for type-safety
   */
  public static <K,V> HashMap<K, V> hashMap(Object... keyValuePairs) {
    return putAll(new HashMap<>(), keyValuePairs);
  }

  /**
   * Creates a {@link HashMap} from the given key-value pairs
   */
  public static <K,V> HashMap<K, V> hashMap(K key, V value) {
    return putAll(new HashMap<>(), key, value);
  }

  /**
   * Creates a {@link HashMap} from the given key-value pairs
   */
  public static <K,V> HashMap<K, V> hashMap(K key1, V value1, K key2, V value2) {
    return putAll(new HashMap<>(), key1, value1, key2, value2);
  }

  /**
   * Creates a {@link HashMap} from the given key-value pairs
   */
  public static <K,V> HashMap<K, V> hashMap(K key1, V value1, K key2, V value2, K key3, V value3) {
    return putAll(new HashMap<>(), key1, value1, key2, value2, key3, value3);
  }

  /**
   * Creates a {@link HashMap} from the given key-value pairs
   */
  public static <K,V> HashMap<K, V> hashMap(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4) {
    return putAll(new HashMap<>(), key1, value1, key2, value2, key3, value3, key4, value4);
  }
  
  /**
   * Creates a {@link HashMap} from the given key-value pairs
   */
  public static <K,V> HashMap<K, V> hashMap(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5, V value5) {
    return putAll(new HashMap<>(), key1, value1, key2, value2, key3, value3, key4, value4, key5, value5);
  }

  /**
   * Creates a {@link LinkedHashMap} from the given key-value pairs
   */
  public static <K,V> LinkedHashMap<K, V> linkedHashMap(K key, V value) {
    return putAll(new LinkedHashMap<>(), key, value);
  }

  /**
   * Creates a {@link LinkedHashMap} from the given key-value pairs
   */
  public static <K,V> LinkedHashMap<K, V> linkedHashMap(K key1, V value1, K key2, V value2) {
    return putAll(new LinkedHashMap<>(), key1, value1, key2, value2);
  }

  /**
   * Creates a {@link LinkedHashMap} from the given key-value pairs
   */
  public static <K,V> LinkedHashMap<K, V> linkedHashMap(K key1, V value1, K key2, V value2, K key3, V value3) {
    return putAll(new LinkedHashMap<>(), key1, value1, key2, value2, key3, value3);
  }

  /**
   * Creates a {@link LinkedHashMap} from the given key-value pairs
   */
  public static <K,V> LinkedHashMap<K, V> linkedHashMap(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4) {
    return putAll(new LinkedHashMap<>(), key1, value1, key2, value2, key3, value3, key4, value4);
  }

  /**
   * Creates a {@link LinkedHashMap} from the given key-value pairs
   */
  public static <K,V> LinkedHashMap<K, V> linkedHashMap(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5, V value5) {
    return putAll(new LinkedHashMap<>(), key1, value1, key2, value2, key3, value3, key4, value4, key5, value5);
  }
  
  /**
   * Creates a {@link LinkedHashMap} from the given key-value pairs (treated as {@code key1, value1, key2, value2, ...})
   * @deprecated use {@link #linkedHashMapBuilder()} for type-safety
   */
  public static <K,V> LinkedHashMap<K, V> linkedHashMap(Object... keyValuePairs) {
    return putAll(new LinkedHashMap<>(), keyValuePairs);
  }

  /**
   * @return a new {@link MapDecorator} instance for building a {@link LinkedHashMap} using method chaining.
   */
  public static <K, V> MapDecorator<K, V> linkedHashMapBuilder() {
    return new MapDecorator<>(new LinkedHashMap<>());
  }

  /** This a frequently-used special case of {@link #hashMap(Object...)}, for String keys and values */
  public static HashMap<String, String> stringMap(String... keyValuePairs) {
    return putAll(new HashMap<>(), keyValuePairs);
  }

  /** This a frequently-used special case of {@link #linkedHashMap(Object...)}, for String keys and values */
  public static LinkedHashMap<String, String> stringLinkedHashMap(String... keyValuePairs) {
    return putAll(new LinkedHashMap<>(), keyValuePairs);
  }

  /**
   * Creates an {@link EnumMap} of the given the args in order key1, value2, key2, value2, ...
   * @deprecated use {@link MapDecorator} for type-safety
   */
  public static <K extends Enum<K>,V> EnumMap<K, V> enumMap(Class<K> enumClass, Object... keyValuePairs) {
    return putAll(new EnumMap<>(enumClass), keyValuePairs);
  }
  
  /** Creates an {@link EnumMap} from the given key-value pair */
  public static <K extends Enum<K>,V> EnumMap<K, V> enumMap(Class<K> enumClass, K key, V value) {
    return putAll(new EnumMap<>(enumClass), key, value);
  }
  
  /** Creates an {@link EnumMap} from the given key-value pairs */
  public static <K extends Enum<K>,V> EnumMap<K, V> enumMap(Class<K> enumClass, K key1, V value1, K key2, V value2) {
    return putAll(new EnumMap<>(enumClass), key1, value1, key2, value2);
  }
  
  /** Creates an {@link EnumMap} from the given key-value pairs */
  public static <K extends Enum<K>,V> EnumMap<K, V> enumMap(Class<K> enumClass, K key1, V value1, K key2, V value2, K key3, V value3) {
    return putAll(new EnumMap<>(enumClass), key1, value1, key2, value2, key3, value3);
  }
  
  /** Creates an {@link EnumMap} from the given key-value pairs */
  public static <K extends Enum<K>,V> EnumMap<K, V> enumMap(Class<K> enumClass, K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4) {
    return putAll(new EnumMap<>(enumClass), key1, value1, key2, value2, key3, value3, key4, value4);
  }
  
  /** Creates an {@link EnumMap} from the given key-value pairs */
  public static <K extends Enum<K>,V> EnumMap<K, V> enumMap(Class<K> enumClass, K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5, V value5) {
    return putAll(new EnumMap<>(enumClass), key1, value1, key2, value2, key3, value3, key4, value4, key5, value5);
  }

  /**
   * Adds the given items to the given map instance.
   * @param keyValuePairs should be K key1, V value1, K key2, V value2, ...
   * @deprecated use {@link MapDecorator} for type-safety
   */
  @SuppressWarnings("unchecked")
  public static <M extends Map<K,V>,K,V> M putAll(M map, Object... keyValuePairs) {
    int n = keyValuePairs.length;
    if (n % 2 == 1)
      throw new IllegalArgumentException("Even number of args required.");
    for (int i = 0; i < n; i+=2)
      map.put((K)keyValuePairs[i], (V)keyValuePairs[i+1]);
    return map;
  }

  /**
   * Adds the given items to the given map instance.
   * @param keyValuePairs should be K key1, V value1, K key2, V value2, ...
   */
  public static <M extends Map<String, String>> M putAll(M map, String... keyValuePairs) {
    int n = keyValuePairs.length;
    if (n % 2 == 1)
      throw new IllegalArgumentException("Even number of args required.");
    for (int i = 0; i < n; i+=2)
      map.put(keyValuePairs[i], keyValuePairs[i+1]);
    return map;
  }

  /**
   * Adds the given items to the given map instance.
   */
  public static <M extends Map<K,V>,K,V> M putAll(M map, K key1, V value1) {
    map.put(key1, value1);
    return map;
  }

  /**
   * Adds the given items to the given map instance.
   */
  public static <M extends Map<K,V>,K,V> M putAll(M map, K key1, V value1, K key2, V value2) {
    map.put(key1, value1);
    map.put(key2, value2);
    return map;
  }

  /**
   * Adds the given items to the given map instance.
   */
  public static <M extends Map<K,V>,K,V> M putAll(M map, K key1, V value1, K key2, V value2, K key3, V value3) {
    map.put(key1, value1);
    map.put(key2, value2);
    map.put(key3, value3);
    return map;
  }

  /**
   * Adds the given items to the given map instance.
   */
  public static <M extends Map<K,V>,K,V> M putAll(M map, K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4) {
    map.put(key1, value1);
    map.put(key2, value2);
    map.put(key3, value3);
    map.put(key4, value4);
    return map;
  }

  /**
   * Adds the given items to the given map instance.
   */
  public static <M extends Map<K,V>,K,V> M putAll(M map, K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4, K key5, V value5) {
    map.put(key1, value1);
    map.put(key2, value2);
    map.put(key3, value3);
    map.put(key4, value4);
    map.put(key5, value5);
    return map;
  }

  /**
   * Creates a map that incorporates all the entries in the given maps.  If a map contains a key that is present
   * in a previous map (according to the order of the arguments to this method), then that key will be overwritten.
   * @return A new map that contains all the entries from the given maps, in the same order.
   */
  @SafeVarargs
  public static <M extends Map<K,V>, K, V> LinkedHashMap<K, V> union(M... maps) {
    LinkedHashMap<K, V> ret = new LinkedHashMap<>();
    for (M map : maps)
      ret.putAll(map);
    return ret;
  }

  /**
   * Adds the entries from the second map to the first map, merging values that already exist.
   * This is a specialization of {@link #mergeAll} for maps containing {@link Mergeable} values.
   *
   * @param destMap the map into which entries will be merged.
   * @param srcMap the entries from this map will be added to the first map, if the values implement {@link Mergeable},
   * then instead of overwriting values in the first map, the values from the second map will be merged.
   * @return the map to which entries were added (i.e. the first argument: {@code map}), for method chaining.
   * @see #mergeAll(Map, Map, BiFunction)
   */
  public static <M extends Map<K,V>, K, V extends Mergeable<V>> M unionMerge(M destMap, M srcMap) {
    for (K k : srcMap.keySet()) {
      V map1Value = destMap.get(k);
      V map2Value = srcMap.get(k);
      if (map1Value != null && map2Value != null)
        map1Value.merge(map2Value);
      else
        destMap.put(k, map2Value);
    }
    return destMap;
  }

  /**
   * Merges the entries from the second map into the first map, by repeatedly invoking {@link Map#merge} for all
   * entries in the second map.
   *
   * <h3>Example:</h3>
   * If both maps contain {@link Integer} values, can use the method reference {@link Integer#sum Integer::sum} as the
   * remapping function to produce the sum of the entries in both maps.
   *
   * @param destMap the map into which entries will be merged
   * @param srcMap the entries from this map will be merged into the first map using the given remapping function
   * @param remappingFunction the function to recompute a value if present (see {@link Map#merge} for details).
   * @return the map into which entries were merged (i.e. the first argument), for method chaining.
   * @see #unionMerge(Map, Map)
   * @since 10/29/2019
   */
  public static <M extends Map<K,V>, K, V> M mergeAll(M destMap, M srcMap,
                                                      BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
    srcMap.forEach((k, v) -> destMap.merge(k, v, remappingFunction));
    return destMap;
  }

  /**
   * Removes all mappings whose values are null
   * @return the same map, post-modification.
   */
  public static <K,V> Map<K,V> removeNullValues(Map<K,V> map) {
    map.keySet().removeIf(k -> map.get(k) == null);
    return map;
  }

  /**
   * Removes all the keys from the given map that aren't in the given set.
   * Returns the same map post-modification.
   *
   * @return the same map instance, for call chaining
   * @see #filterMap(Map, Set)
   */
  public static <K,V> Map<K,V> retainAll(Map<K,V> map, Set<K> keysToRetain) {
    // TODO: perhaps better to return a boolean? (i.e. whether any elements were removed)
    map.keySet().removeIf(k -> !keysToRetain.contains(k));
    return map;
  }

  /**
   * Creates a new HashMap with all the keys from the given map that aren't in
   * the given set.
   *
   * Similar to {@link #retainAll}, except the underlying map is not modified.
   *
   * @return the new map
   * @see #retainAll(Map, Set)
   */
  public static <K,V> Map<K,V> filterMap(Map<K,V> map, Set<K> keysToRetain) {
    HashMap<K,V> ret = new HashMap<>(keysToRetain.size() * 2);
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
  public static <K,V, M extends Map<K,V>> M put(M map, K key, V value) {
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
  public static String extractSingleValue(Map<String, ?> paramMap, String key) {
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
   * Returns the first value from the collection mapped to by the given key in the given map, or {@code null}
   * if either the mapping is not present or the collection is empty.
   *
   * @see #getSingleValue(Map, Object, Object)
   */
  @Nullable
  public static <K, V> V getSingleValue(Map<K, ? extends Collection<V>> map, K key) {
    return getSingleValue(map, key, null);
  }

  /**
   * Returns the first value from the collection mapped to by the given key in the given map, or the given
   * default value if either the mapping is not present or the collection is empty.
   */
  public static <K, V> V getSingleValue(Map<K, ? extends Collection<V>> map, K key, V defaultValue) {
    Collection<V> values = map.get(key);
    if (!CollectionUtils.isEmpty(values)) {
      return CollectionUtils.first(values);
    }
    return defaultValue;
  }

  /**
   * Inserts the given element into the map if the map doesn't already contain it.
   * NOTE: this is different from {@link Map#putIfAbsent(Object, Object)} (Java 1.8), which returns {@code null} if the
   * value wasn't already in the map.
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
   * @deprecated available as {@link Map#getOrDefault(Object, Object)} in Java 1.8+
   */
  public static <K,V> V getOrDefault(Map<K,V> map, K key, V defaultValue) {
    if (map.containsKey(key))
      return map.get(key);
    return defaultValue;
  }

  // TODO: write a GWT generator that will instantiate a class based on its literal - can get rid of many factories that way:

  /**
   * This version of {@link #getOrInsert} is for values that are expensive to create,
   * and allows passing in a factory method which will be invoked only if needed.
   *
   * @return The value created by the factory if it was inserted,
   * or the previous value if it was already contained by the map.
   * @see Map#computeIfAbsent(Object, Function)
   */
  public static <K,V> V getOrInsert(Map<K,V> map, K key, Supplier<V> factory) {
    if (map.containsKey(key))
      return map.get(key);
    V value = factory.get();
    map.put(key, value);
    return value;
  }

  /**
   * This version of {@link #getOrInsert} is for values that are expensive to create,
   * and allows passing in a factory method which, if needed, will be invoked with the given argument.
   * @return The value created by the factory if it was inserted,
   * or the previous value if it was already contained by the map.
   * @see Map#computeIfAbsent(Object, Function)
   */
  public static <K,V,A> V getOrInsert(Map<K,V> map, K key, Function<A, V> factory, A factoryArg) {
    if (map.containsKey(key))
      return map.get(key);
    V value = factory.apply(factoryArg);
    map.put(key, value);
    return value;
  }

  /**
   * This version of {@link #getOrInsert} is for values that are expensive to create,
   * and allows passing in a factory method which will be invoked only if needed.
   * @return The value created by the factory if it was inserted,
   * or the previous value if it was already contained by the map.
   * @see Map#computeIfAbsent(Object, Function)
   */
  public static <K,A1,A2,V> V getOrInsert(Map<K,V> map, K key, BiFunction<A1, A2, V> factory, A1 factoryArg1, A2 factoryArg2) {
    if (map.containsKey(key))
      return map.get(key);
    V value = factory.apply(factoryArg1, factoryArg2);
    map.put(key, value);
    return value;
  }

  /**
   * Removes all the entries matching predicate from the given map.
   * Returns the same map to allow method chaining.
   */
  public static <K,V> Map<K, V> removeMatchingEntries(Map<K,V> map, Predicate<Map.Entry<K,V>> predicate) {
    map.entrySet().removeIf(predicate);
    return map;
  }

  /**
   * Adds all the entries in the given {@link Map} to the given {@link Multimap}.
   * @param to the recipient of the data
   * @param from the source of the data
   * @return the same {@link Multimap} instance that was passed in
   * @see com.google.common.collect.Multimaps#forMap(Map)
   */
  public static <K,V> Multimap<K, V> putAllToMultimap(Multimap<K, V> to, Map<K, V> from) {
    for (Map.Entry<K, V> entry : from.entrySet()) {
      to.put(entry.getKey(), entry.getValue());
    }
    return to;
  }

  /**
   * Creates a view of the given {@link Multimap} as a normal 1-1 {@link Map}, if the multimap contains only unique
   * key-value mappings.  This is an alternative for {@link Multimap#asMap()} (which returns {@code Map<K, Collection<V>>}).
   * <p>
   * <b>WARNING:</b> this method will not throw an exception if the given multimap contains multiple values for any key,
   * but accessing such an entry via the returned map will throw an {@link IllegalArgumentException}, because the returned
   * view is backed by {@link Iterables#getOnlyElement(java.lang.Iterable)}.
   * It might be a good idea to preemptively call {@link #maxValuesPerKey(Multimap)} to check whether it's safe to call
   * this method on the given multimap.
   * <p>
   * <b>NOTE:</b> since modifications to the returned map will propagate to the given multimap, it might be a good idea
   * to create a defensive copy of the result (e.g. with {@link ImmutableMap#copyOf(Map)}).
   *
   * @param multimap should contain only unique key-value mappings
   * @return a view of the given multimap as a single-valued map (<b>WARNING:</b> if the given multimap contains multiple
   * values for any key, accessing that entry via the returned map will throw an {@link IllegalArgumentException})
   * @see <a href="https://stackoverflow.com/a/40432352/1965404">solution posted on StackOverflow</a>
   * @see Multimap#asMap()
   * @see Maps#transformValues
   */
  public static <K,V> Map<K, V> asMap(Multimap<K, V> multimap) {
    return Maps.transformValues(multimap.asMap(), Iterables::getOnlyElement);
  }

  /**
   * Determines the maximum number of values mapped to any key in the given multimap.  This can be used to check
   * whether it's safe to call {@link #asMap(Multimap)} on it.
   * <p>
   * <b>Example:</b>
   * <pre>{@code
   *   if (maxValuesPerKey(multimap) <= 1)
   *     // safe to call asMap(multimap)
   *   else
   *     // not safe to call asMap(multimap)
   * }</pre>
   * </p>
   * @return the maximum number of values mapped to any key in the given multimap.
   */
  public static <K,V> int maxValuesPerKey(Multimap<K, V> multimap) {
    return multimap.keySet().stream().mapToInt(k -> multimap.get(k).size()).max().orElse(0);
  }
  
  /**
   * Invokes {@link Map#computeIfAbsent(Object, Function)} on the given map using a mapping function that might throw
   * a checked exception.  This allows using the map's {@code computeIfAbsent} functionality with a lambda or a method
   * reference that throws a checked exception.
   * <p>
   * If an exception is thrown, it will be propagated to the caller,
   * while {@link Map#computeIfAbsent(Object, Function)} gets a
   * {@link RuntimeException} (specifically, {@link ThrowingFunction.WrappedException}),
   * which is caught in this method.
   *
   * @param <K> the key type
   * @param <V> the value type
   * @param <E> the checked exception declared by the mapping function
   * @return the result of {@link Map#computeIfAbsent(Object, Function)}
   * @throws E the exception thrown by the mapping function
   * @see ThrowingFunction
   */
  @SuppressWarnings("unchecked")
  public static <K, V, E extends Exception> V computeIfAbsent(Map<K, V> map, K key,
                                                              ThrowingFunction<? super K, ? extends V, E> mappingFunction) throws E {
    try {
      return map.computeIfAbsent(key, mappingFunction);
    }
    catch (ThrowingFunction.WrappedException e) {
      throw (E)e.getCause();
    }
  }

  /**
   * @return the first element returned by the map's {@link Map#keySet() keySet()} iterator
   * @throws NoSuchElementException if the map is empty
   * @see Iterables#getFirst(Iterable, Object)
   */
  public static <K,V> K firstKey(Map<K, V> map) {
    return map.keySet().iterator().next();
  }

  /**
   * @return the first element returned by the map's {@link Map#values() values()} iterator
   * @throws NoSuchElementException if the map is empty
   * @see Iterables#getFirst(Iterable, Object)
   */
  public static <K,V> V firstValue(Map<K, V> map) {
    return map.values().iterator().next();
  }

  /**
   * @return the first element returned by the map's {@link Map#entrySet() entrySet()} iterator
   * @throws NoSuchElementException if the map is empty
   * @see Iterables#getFirst(Iterable, Object)
   */
  public static <K,V> Map.Entry<K, V> firstEntry(Map<K, V> map) {
    return map.entrySet().iterator().next();
  }

  /**
   * @return {@code true} iff the given map is either {@code null} or empty
   */
  public static boolean isEmpty(Map<?, ?> map) {
    return map == null || map.isEmpty();
  }

}
