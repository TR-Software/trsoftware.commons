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

package solutions.trsoftware.commons.shared.util.collections;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.util.MapUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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