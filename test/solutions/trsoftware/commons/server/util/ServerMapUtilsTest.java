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

package solutions.trsoftware.commons.server.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.callables.Function0;
import solutions.trsoftware.commons.shared.util.callables.Function1;
import solutions.trsoftware.commons.shared.util.callables.Function2;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static solutions.trsoftware.commons.server.util.ServerMapUtils.getOrInsertConcurrent;

/**
 * Oct 28, 2009
 *
 * @author Alex
 */
public class ServerMapUtilsTest extends TestCase {

  public void testGetOrInsertConcurrentConcurrent() throws Exception {
    ConcurrentMap<String, String> map = new ConcurrentHashMap<String, String>();
    map.put("a", "x");
    final String foo = "foo";
    String bar = "bar";
    assertEquals("x", getOrInsertConcurrent(map, "a", foo));  // the prior value is returned
    assertSame(bar, getOrInsertConcurrent(map, "b", bar));  // the new value is returned for a new key

    // repeat the same experiment with a factory method
    Function0<String> factoryNoArgs = new Function0<String>() {
      public String call() {
        return foo;
      }
    };
    assertEquals(bar, getOrInsertConcurrent(map, "b", factoryNoArgs));
    assertSame(foo, getOrInsertConcurrent(map, "c", factoryNoArgs));

    // test the factory with the 1 args version
    Function1<Integer, String> factory1Arg = new Function1<Integer, String>() {
      public String call(Integer arg) {
        return foo + arg;
      }
    };
    assertEquals(bar, getOrInsertConcurrent(map, "b", factory1Arg, 123));
    assertEquals("foo123", getOrInsertConcurrent(map, "d", new Function1<Integer, String>() {
      public String call(Integer arg) {
        return foo+arg;
      }
    }, 123));

    // test the factory with args version
    Function2<Integer, Double, String> factory2Args = new Function2<Integer, Double, String>() {
      public String call(Integer arg1, Double arg2) {
        return foo + arg1 + arg2;
      }
    };
    assertEquals(bar, getOrInsertConcurrent(map, "b", factory2Args, 123, 2.3));
    assertEquals("foo1232.3", getOrInsertConcurrent(map, "e", factory2Args, 123, 2.3));
  }

}