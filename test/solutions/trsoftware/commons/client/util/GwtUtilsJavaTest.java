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

import java.util.Map;

import static solutions.trsoftware.commons.client.util.GwtUtils.getSimpleName;
import static solutions.trsoftware.commons.client.util.GwtUtils.isAssignableFrom;


/**
 * Mar 21, 2011
 *
 * @author Alex
 */
public class GwtUtilsJavaTest extends TestCase {

  public void testIsAssignableFrom() throws Exception {
    Class<Number> number = Number.class;
    Class<Integer> integer = Integer.class;

    assertTrue(number.isAssignableFrom(integer));
    assertTrue(isAssignableFrom(number, integer));

    assertFalse(integer.isAssignableFrom(number));
    assertFalse(isAssignableFrom(integer, number));
  }

  public void testGetClassSimpleName() throws Exception {
    Class<Integer> integer = Integer.class;
    assertEquals("java.lang.Integer", integer.getName());
    assertEquals("Integer", integer.getSimpleName());
    assertEquals("Integer", getSimpleName(integer));

    Class<Map.Entry> mapEntry = Map.Entry.class;
    assertEquals("java.util.Map$Entry", mapEntry.getName());
    assertEquals("Entry", mapEntry.getSimpleName());
    assertEquals("Entry", getSimpleName(mapEntry));
  }
}