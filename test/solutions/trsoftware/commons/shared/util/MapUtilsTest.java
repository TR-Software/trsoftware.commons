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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.testutil.MockException;
import solutions.trsoftware.commons.shared.util.callables.Function0;
import solutions.trsoftware.commons.shared.util.callables.Function1;
import solutions.trsoftware.commons.shared.util.callables.Function2;
import solutions.trsoftware.commons.shared.util.function.ThrowingRunnable;
import solutions.trsoftware.commons.shared.util.mutable.MutableFloat;
import solutions.trsoftware.commons.shared.util.mutable.MutableInteger;
import solutions.trsoftware.commons.shared.util.mutable.MutableNumber;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;
import static solutions.trsoftware.commons.shared.util.MapUtils.*;

public class MapUtilsTest extends TestCase {

  // TODO: it would be good to have the same test run not under GWT for speed, but this can't be accomplished with inheritance

  public void testFilterMap() throws Exception {
    HashMap<String, Integer> map = hashMap("a", 1, "b", 2, "c", 3, "d", 4, "e", 5);
    Map<String, Integer> result = filterMap(map, new HashSet<String>(Arrays.asList("a", "d")));
    // the returned map should be a new instance
    assertNotSame(map, result);
    // check that the result map contains just "a" and "d"
    assertEquals(2, result.size());
    assertEquals(1, (int)result.get("a"));
    assertEquals(4, (int)result.get("d"));
    // check that the original map was not modified
    assertEquals(5, map.size());
    assertEquals(1, (int)map.get("a"));
    assertEquals(2, (int)map.get("b"));
    assertEquals(3, (int)map.get("c"));
    assertEquals(4, (int)map.get("d"));
    assertEquals(5, (int)map.get("e"));
  }

  public void testRetainAll() throws Exception {
    HashMap<String, Integer> map = hashMap("a", 1, "b", 2, "c", 3, "d", 4, "e", 5);
    Map<String, Integer> result = retainAll(map, new HashSet<String>(Arrays.asList("a", "d")));
    assertSame(map, result);
    assertEquals(2, result.size());
    assertEquals(1, (int)result.get("a"));
    assertEquals(4, (int)result.get("d"));
  }

  public void testUnion() throws Exception {
    HashMap<String, Integer> map1 = linkedHashMap("a", 1, "b", 2);
    HashMap<String, Integer> map2 = linkedHashMap("b", 3, "c", 3, "d", 4);
    HashMap<String, Integer> map3 = linkedHashMap("d", 0, "e", 5);

    HashMap<String, Integer> unionWithSelf = union(map1, map1);
    assertNotSame(unionWithSelf, map1);
    assertEquals(map1, unionWithSelf);

    assertEquals(linkedHashMap("a", 1, "b", 3, "c", 3, "d", 4),
        union(map1, map2));
    assertEquals(linkedHashMap("a", 1, "b", 3, "c", 3, "d", 0, "e", 5),
        union(map1, map2, map3));
    assertEquals(linkedHashMap("a", 1, "b", 2, "c", 3, "d", 4, "e", 5),
        union(map3, map2, map1));
  }

  public void testUnionMerge() throws Exception {
    class MapFactory {
      SortedMap<String, MutableNumber> map1() {
        return sortedMap(
            "a", new MutableInteger(1),
            "b", new MutableInteger(2));
      }

      SortedMap<String, MutableNumber> map2() {
        return sortedMap(
            "b", new MutableFloat(3),
            "c", new MutableInteger(3),
            "d", new MutableFloat(4));
      }

      SortedMap<String, MutableNumber> map3() {
        return sortedMap(
            "d", new MutableInteger(0),
            "e", new MutableFloat(5));
      }
    }

    MapFactory mapFactory = new MapFactory();
    {
      SortedMap<String, MutableNumber> map1 = mapFactory.map1();
      SortedMap<String, MutableNumber> mergedWithSelf = unionMerge(map1, mapFactory.map1());
      assertSame(mergedWithSelf, map1);
      assertEquals(linkedHashMap(
              "a", new MutableInteger(2),
              "b", new MutableInteger(4)),
          mergedWithSelf);
    }

    assertEquals(sortedMap(
            "a", new MutableInteger(1),
            "b", new MutableInteger(5),
            "c", new MutableInteger(3),
            "d", new MutableFloat(4)),
        unionMerge(mapFactory.map1(), mapFactory.map2()));

    assertEquals(sortedMap(
            "a", new MutableInteger(1),
            "b", new MutableInteger(5),
            "c", new MutableInteger(3),
            "d", new MutableFloat(4),
            "e", new MutableFloat(5)),
        unionMerge(mapFactory.map1(), unionMerge(mapFactory.map2(), mapFactory.map3())));

    assertEquals(sortedMap(
            "a", new MutableInteger(1),
            "b", new MutableFloat(5),
            "c", new MutableInteger(3),
            "d", new MutableInteger(4),
            "e", new MutableFloat(5)),
        unionMerge(mapFactory.map3(), unionMerge(mapFactory.map2(), mapFactory.map1())));
  }

  public void testRemoveNullValues() throws Exception {
    HashMap<String, Integer> map = hashMap("a", 1, "b", null, "c", null, "d", 4, "e", null);
    assertEquals(5, map.size());
    Map<String, Integer> result = removeNullValues(map);
    assertSame(map, result);
    assertEquals(2, result.size());
    assertEquals(1, (int)result.get("a"));
    assertEquals(4, (int)result.get("d"));
  }

  public void testExtractSingleValue() throws Exception {
    assertEquals("bar", extractSingleValue(MapUtils.hashMap("foo", new String[]{"bar"}), "foo"));
    assertEquals("bar", extractSingleValue(MapUtils.hashMap("foo", "bar"), "foo"));
    assertEquals("bar", extractSingleValue(MapUtils.hashMap("foo", new String[]{"bar", "baz"}), "foo"));
    assertEquals("bar", extractSingleValue(MapUtils.hashMap("foo", new String[]{"bar"}, "a", new String[]{"b", "c"}), "foo"));
    assertEquals("b", extractSingleValue(MapUtils.hashMap("foo", new String[]{"bar"}, "a", new String[]{"b", "c"}), "a"));
    assertNull(extractSingleValue(MapUtils.hashMap("foo", new String[]{"bar"}), "bar"));
    assertNull(extractSingleValue(MapUtils.hashMap("foo", new String[]{}), "foo"));
    assertNull(extractSingleValue(MapUtils.hashMap("foo", new int[]{}), "foo"));
    assertNull(extractSingleValue(MapUtils.hashMap("foo", new int[]{1, 2, 3}), "foo"));
    assertNull(extractSingleValue(MapUtils.hashMap("foo", null), "foo"));
    assertNull(extractSingleValue(MapUtils.hashMap("foo", "bar"), "a"));
  }

  public void testGetOrInsert() throws Exception {
    Map<String, String> map = hashMap("a", "x");
    final String foo = "foo";
    String bar = "bar";
    assertEquals("x", getOrInsert(map, "a", foo));  // the prior value is returned
    assertSame(bar, getOrInsert(map, "b", bar));  // the new value is returned for a new key

    // repeat the same experiment with a factory method
    Function0<String> factoryNoArgs = new Function0<String>() {
      public String call() {
        return foo;
      }
    };
    assertEquals(bar, getOrInsert(map, "b", factoryNoArgs));
    assertSame(foo, getOrInsert(map, "c", factoryNoArgs));

    // test the factory with the 1 args version
    Function1<Integer, String> factory1Arg = new Function1<Integer, String>() {
      public String call(Integer arg) {
        return foo + arg;
      }
    };
    assertEquals(bar, getOrInsert(map, "b", factory1Arg, 123));
    assertEquals("foo123", getOrInsert(map, "d", new Function1<Integer, String>() {
      public String call(Integer arg) {
        return foo + arg;
      }
    }, 123));

    // test the factory with args version
    Function2<Integer, Double, String> factory2Args = new Function2<Integer, Double, String>() {
      public String call(Integer arg1, Double arg2) {
        return foo + arg1 + arg2;
      }
    };
    assertEquals(bar, getOrInsert(map, "b", factory2Args, 123, 2.3));
    assertEquals("foo1232.3", getOrInsert(map, "e", factory2Args, 123, 2.3));
  }

  public void testRemoveMatchingEntries() throws Exception {
    assertEquals(MapUtils.<String, Integer>hashMap("a", 1), removeMatchingEntries(MapUtils.<String, Integer>hashMap("b", 2, "a", 1),
        new Predicate<Map.Entry<String, Integer>>() {
          public boolean apply(Map.Entry<String, Integer> item) {
            return item.getValue() == 2;
          }
        }));

    // test with an empty map
    assertEquals(MapUtils.<String, Integer>hashMap(), removeMatchingEntries(MapUtils.<String, Integer>hashMap(),
        new Predicate<Map.Entry<String, Integer>>() {
          public boolean apply(Map.Entry<String, Integer> item) {
            return item.getValue() == 2;
          }
        }));
  }

  private static final ImmutableMultimap<String, Integer> multimap = ImmutableMultimap.<String, Integer>builder()
      .putAll("even", 0, 2, 4)
      .putAll("odd", 1, 3, 5)
      .build();

  /**
   * @see MapUtils#asMap(Multimap)
   */
  public void testAsMap() {
    // NOTE: although the above multimap contains multiple values for each key, this method will not throw an exception
    Map<String, Integer> map = asMap(multimap);
    assertEquals(2, map.size());
    assertNull(map.get("foo"));
    // however accessing keys with multiple values on this result will raise an IllegalArgumentException
    assertThrows(IllegalArgumentException.class, (Runnable)() -> map.get("even"));
    assertThrows(IllegalArgumentException.class, (Runnable)() -> new HashMap<>(map));
    // the inverse of the multimap, on the other hand, is going to be single-valued
    Map<Integer, String> inverseMap = asMap(multimap.inverse());
    System.out.println("inverseMap = " + inverseMap);
    ImmutableMap<Integer, String> expectedInverse = ImmutableMap.<Integer, String>builder()
        .put(0, "even")
        .put(2, "even")
        .put(4, "even")
        .put(1, "odd")
        .put(3, "odd")
        .put(5, "odd")
        .build();
    assertEquals(expectedInverse, inverseMap);
    assertEquals(expectedInverse, ImmutableMap.copyOf(inverseMap));
  }

  /**
   * @see MapUtils#maxValuesPerKey(Multimap)
   */
  public void testMaxValuesPerKey() throws Exception {
    assertEquals(3, maxValuesPerKey(multimap));
    assertEquals(1, maxValuesPerKey(multimap.inverse()));
    assertEquals(0, maxValuesPerKey(ImmutableMultimap.of()));
  }

  /**
   * @see MapUtils#mergeAll(Map, Map, BiFunction)
   */
  public void testMergeAll() {
    Map<String, Integer> map1 = hashMap("a", 1, "b", 2, "c", 3);
    Map<String, Integer> map2 = hashMap("a", 3, "b", -1, "d", 5);
    assertEquals(
        hashMap("a", 4, "b", 1, "c", 3, "d", 5),
        mergeAll(map1, map2, Integer::sum));
  }

  public void testComputeIfAbsent() throws Exception {
    Map<String, Integer> map = linkedHashMap("a", 1, "b", 2, "c", 3);
    Map<String, Integer> expected = new LinkedHashMap<>(map);

    // 1) with a method reference that doesn't throw an exception
    computeIfAbsent(map, "asdf", this::lengthOrThrow);
    assertEquals(put(expected, "asdf", 4), map);

    // 2) with a lambda that does the same thing as lengthOrThrow
    computeIfAbsent(map, "foo", message -> {
      if ("throw".equals(message))
        throw new MockException(message);
      return message.length();
    });
    assertEquals(put(expected, "foo", 3), map);

    // 3) with an arg that triggers an exception in lengthOrThrow (which should be rethrown)
    assertThrows(MockException.class,
        (ThrowingRunnable)() -> computeIfAbsent(map, "throw", this::lengthOrThrow));
    assertEquals(expected, map);  // not modified
  }

  /**
   * Returns the length of the given message or throws a {@link MockException} if the message is "throw"
   */
  private Integer lengthOrThrow(String message) throws MockException {
    if ("throw".equals(message))
      throw new MockException(message);
    return message.length();
  }


  public void testFirstKey() throws Exception {
    Map<String, Integer> map = linkedHashMap("a", 1, "b", 2);
    assertEquals("a", firstKey(map));
  }

  public void testFirstValue() throws Exception {
    Map<String, Integer> map = linkedHashMap("a", 1, "b", 2);
    assertEquals((Integer)1, firstValue(map));
  }

  public void testFirstEntry() throws Exception {
    Map<String, Integer> map = linkedHashMap("a", 1, "b", 2);
    assertEquals(new AbstractMap.SimpleEntry<>("a", 1), firstEntry(map));
  }

  public void testGetSingleValue() throws Exception {
    Map<String, List<Integer>> map = hashMap(
        "a", Arrays.asList(1, 2),
        "b", Collections.singletonList(3),
        "c", Collections.emptyList()
    );
    // 1) without default value
    assertEquals((Integer)1, getSingleValue(map, "a"));
    assertEquals((Integer)3, getSingleValue(map, "b"));
    assertNull(getSingleValue(map, "c"));
    assertNull(getSingleValue(map, "d"));
    // 2) with default value
    assertEquals((Integer)1, getSingleValue(map, "a", 0));
    assertEquals((Integer)3, getSingleValue(map, "b", 0));
    assertEquals((Integer)0, getSingleValue(map, "c", 0));
    assertEquals((Integer)5, getSingleValue(map, "d", 5));
  }

  public void testToMap() throws Exception {
    assertEquals(hashMap("a", 1, "ab", 2, "abc", 3),
        Stream.of("a", "ab", "abc").collect(toMap(Function.identity(), String::length, TreeMap::new)));
  }
}