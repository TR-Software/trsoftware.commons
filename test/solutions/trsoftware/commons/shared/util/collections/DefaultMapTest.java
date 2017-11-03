package solutions.trsoftware.commons.shared.util.collections;

import solutions.trsoftware.commons.client.util.MapUtils;
import junit.framework.TestCase;

import java.util.*;

public class DefaultMapTest extends TestCase {

  private DefaultArrayListMap<String, Integer> map;

  public void setUp() throws Exception {
    super.setUp();
    map = new DefaultArrayListMap<String, Integer>();
  }

  public void tearDown() throws Exception {
    map = null;
    super.tearDown();
  }

  public void testDefaultMap() throws Exception {
    String[] keys = {"foo", "bar", "baz"};
    for (int i = 0; i < keys.length; i++) {
      String key = keys[i];
      assertEquals(i, map.size());
      assertFalse(map.containsKey(key));
      List<Integer> valueList = map.get(key);
      assertNotNull(valueList);
      assertTrue(valueList.isEmpty());
      int expectedSize = i + 1;
      assertEquals(expectedSize, map.size());
      assertTrue(map.containsKey(key));
      assertTrue(map.containsValue(valueList));
      valueList.add(i);
      assertSame(valueList, map.get(key));
      assertEquals(expectedSize, map.size());
      valueList.add(12345);
    }
    assertEquals(MapUtils.<String, List<Integer>>linkedHashMap(
        "foo", Arrays.asList(0, 12345),
        "bar", Arrays.asList(1, 12345),
        "baz", Arrays.asList(2, 12345)
    ), ((Map<String, List<Integer>>)map));

  }
}