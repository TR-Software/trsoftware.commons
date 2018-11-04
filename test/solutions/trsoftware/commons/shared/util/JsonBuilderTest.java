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

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dec 22, 2008
 *
 * @author Alex
 */
public class JsonBuilderTest extends TestCase {

  public void testSimpleValues() throws Exception {
    assertEquals(
        "{'a': 'foo', 'b': 123, 'c': ['x', 1, true, 12.0]}".replaceAll("'", "\""),
        new JsonBuilder().beginObject()
            .key("a").value("foo")
            .key("b").value(123)
            .key("c").beginArray()
              .value("x")
              .value(1)
              .value(true)
              .value(12.0)  // make sure the .0 is kept to preserve type information
            .endArray()
            .endObject().toString()
    );
  }

  public void testCollections() throws Exception {
    // create a collection that will evaluate to ["x", 1, true, 12] in JSON form
    List<Object> collection = Arrays.asList(objectOverridesToString("x"), 1, Boolean.TRUE, 12);
    // create a map that will evaluate to {"a": "foo", "b": 123, "c": __collection__, "d": "bar"} in JSON form
    Map map = new LinkedHashMap();  // use LHM to preserve insertion order
    map.put("a", objectOverridesToString("foo"));
    map.put("b", 123);
    map.put("c", collection);
    map.put("d", "bar");

    // first, try using the map as a value to a builder object
    assertEquals("{'testKey': {'a': 'foo', 'b': 123, 'c': ['x', 1, true, 12], 'd': 'bar'}}".replaceAll("'", "\""),
        new JsonBuilder().beginObject().key("testKey").value(map).endObject().toString());

    // now try using the map as a value to a builder array
    assertEquals("[{'a': 'foo', 'b': 123, 'c': ['x', 1, true, 12], 'd': 'bar'}]".replaceAll("'", "\""),
        new JsonBuilder().beginArray().value(map).endArray().toString());

    // now try using the static shorcut methods
    assertEquals("{'a': 'foo', 'b': 123, 'c': ['x', 1, true, 12], 'd': 'bar'}".replaceAll("'", "\""),
        JsonBuilder.mapToJson(map));
    assertEquals("['x', 1, true, 12]".replaceAll("'", "\""),
        JsonBuilder.iterableToJson(collection));
  }


  private Object objectOverridesToString(final String string) {
    return new Object() {
      @Override
      public String toString() {
        return string;
      }
    };
  }

  public void testComplexArrays() throws Exception {
    assertEquals("[1, {\"foo\": \"bar\"}, 3]",
        new JsonBuilder().beginArray()
            .value(1)
            .value(new JsonBuilder().beginObject().key("foo").value("bar").endObject())
            .value(3)
            .endArray().toString());
  }

}