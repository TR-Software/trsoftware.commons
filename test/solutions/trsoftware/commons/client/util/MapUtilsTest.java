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

package solutions.trsoftware.commons.client.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.util.callables.Function0;
import solutions.trsoftware.commons.client.util.callables.Function1;
import solutions.trsoftware.commons.client.util.callables.Function2;
import solutions.trsoftware.commons.client.util.mutable.MutableFloat;
import solutions.trsoftware.commons.client.util.mutable.MutableInteger;
import solutions.trsoftware.commons.client.util.mutable.MutableNumber;

import java.util.*;

import static solutions.trsoftware.commons.client.util.MapUtils.*;

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
    assertEquals("bar", extractSingleValue(MapUtils.<String, String[]>hashMap("foo", new String[]{"bar"}), "foo"));
    assertEquals("bar", extractSingleValue(MapUtils.<String, String[]>hashMap("foo", "bar"), "foo"));
    assertEquals("bar", extractSingleValue(MapUtils.<String, String[]>hashMap("foo", new String[]{"bar", "baz"}), "foo"));
    assertEquals("bar", extractSingleValue(MapUtils.<String, String[]>hashMap("foo", new String[]{"bar"}, "a", new String[]{"b", "c"}), "foo"));
    assertEquals("b", extractSingleValue(MapUtils.<String, String[]>hashMap("foo", new String[]{"bar"}, "a", new String[]{"b", "c"}), "a"));
    assertNull(extractSingleValue(MapUtils.<String, String[]>hashMap("foo", new String[]{"bar"}), "bar"));
    assertNull(extractSingleValue(MapUtils.<String, String[]>hashMap("foo", new String[]{}), "foo"));
    assertNull(extractSingleValue(MapUtils.<String, String[]>hashMap("foo", new int[]{}), "foo"));
    assertNull(extractSingleValue(MapUtils.<String, String[]>hashMap("foo", new int[]{1, 2, 3}), "foo"));
    assertNull(extractSingleValue(MapUtils.<String, String[]>hashMap("foo", null), "foo"));
    assertNull(extractSingleValue(MapUtils.<String, String[]>hashMap("foo", "bar"), "a"));
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
}