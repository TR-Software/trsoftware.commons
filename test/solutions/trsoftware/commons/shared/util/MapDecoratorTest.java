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

package solutions.trsoftware.commons.shared.util;

import junit.framework.TestCase;

import java.util.LinkedHashMap;

/**
 * @author Alex
 * @since 11/11/2017
 */
public class MapDecoratorTest extends TestCase {

  public void testBuilder() throws Exception {
    assertEquals(newLinkedHashMap(), new MapDecorator<Integer, String>(newLinkedHashMap()).getMap());
    assertEquals(MapUtils.<Integer, String>linkedHashMap(1, "foo", 2, "bar"),
        new MapDecorator<Integer, String>(newLinkedHashMap()).put(1, "foo").put(2, "bar").getMap());
  }

  private static LinkedHashMap<Integer, String> newLinkedHashMap() {
    return new LinkedHashMap<Integer, String>();
  }

}