/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.client.jso;

import com.google.common.collect.ImmutableMultimap;
import com.google.gwt.core.client.JavaScriptObject;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.shared.util.ArrayUtils;

import java.util.Map;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertArraysEqual;

/**
 * @author Alex
 * @since 11/28/2021
 */
public class JsObjectTest extends CommonsGwtTestCase {

  private JsObject jsObject;
  private JsMixedArray array;
  private JavaScriptObject function;
  private JavaScriptObject emptyArray;
  private JavaScriptObject emptyObj;
  private String[] keys;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    jsObject = JsObject.create();
    array = JsMixedArray.create().add(1).add("hello").add(true);
    function = JavaScriptObject.createFunction();
    emptyArray = JavaScriptObject.createArray();
    emptyObj = JavaScriptObject.createObject();
    jsObject
        .set("a", 1)
        .set("b", 2.1)
        .set("c", 0)
        .set("d", Double.NaN)
        .set("str", "foo")
        .set("emptyStr", "")
        .set("yes", true)
        .set("no", false)
        .set("arr", array)
        .set("fun", function)
        .set("emptyArr", emptyArray)
        .set("obj", emptyObj)
        .set("obj2", (JavaScriptObject)null)
        .setUndefined("undef")
    ;
    keys = new String[]{"a", "b", "c", "d", "str", "emptyStr", "yes", "no", "arr", "fun", "emptyArr", "obj", "obj2", "undef"};
  }

  public void testGetString() throws Exception {
    assertEquals("foo", jsObject.getString("str"));
    assertEquals("", jsObject.getString("emptyStr"));
    assertNull(jsObject.getString("asdf"));  // undefined property
    assertNull(jsObject.getString("undef"));  // actual property set to undefined
  }

  public void testGetObject() throws Exception {
    // basic Object.equals should work here because our expected value has the same identity
    assertEquals(array, jsObject.getObject("arr"));
    assertEquals(function, jsObject.getObject("fun"));
    assertEquals(emptyArray, jsObject.getObject("emptyArr"));
    assertEquals(emptyObj, jsObject.getObject("obj"));
    assertNull(jsObject.getObject("obj2"));
    assertNull(jsObject.getObject("asdf"));  // undefined property
    assertNull(jsObject.getObject("undef"));  // actual property set to undefined

    // NOTE: attempting to use getObject on properties with other value types (e.g. booleans, numbers, strings, etc.)
    // will throw a ClassCastException in dev mode, but will succeed in prod mode
  }

  public void testGetBoolean() throws Exception {
    assertTrue(jsObject.getBoolean("yes"));
    assertFalse(jsObject.getBoolean("no"));

    // non-boolean values will be cast to Boolean
    assertTrue(jsObject.getBoolean("a"));
    assertTrue(jsObject.getBoolean("b"));
    assertTrue(jsObject.getBoolean("str"));
    assertTrue(jsObject.getBoolean("arr"));
    assertTrue(jsObject.getBoolean("fun"));
    assertTrue(jsObject.getBoolean("emptyArr"));
    assertTrue(jsObject.getBoolean("obj"));
    assertFalse(jsObject.getBoolean("c"));
    assertFalse(jsObject.getBoolean("d"));
    assertFalse(jsObject.getBoolean("obj2"));  // null
    assertFalse(jsObject.getBoolean("asdf"));  // undefined property
    assertFalse(jsObject.getBoolean("undef"));  // actual property set to undefined
  }

  public void testGetBoxedBoolean() throws Exception {
    assertEquals(Boolean.TRUE, jsObject.getBoxedBoolean("yes"));
    assertEquals(Boolean.FALSE, jsObject.getBoxedBoolean("no"));

    // non-boolean values will return null
    assertNull(jsObject.getBoxedBoolean("a"));
    assertNull(jsObject.getBoxedBoolean("b"));
    assertNull(jsObject.getBoxedBoolean("str"));
    assertNull(jsObject.getBoxedBoolean("arr"));
    assertNull(jsObject.getBoxedBoolean("fun"));
    assertNull(jsObject.getBoxedBoolean("emptyArr"));
    assertNull(jsObject.getBoxedBoolean("obj"));
    assertNull(jsObject.getBoxedBoolean("c"));
    assertNull(jsObject.getBoxedBoolean("d"));
    assertNull(jsObject.getBoxedBoolean("obj2"));  // null
    assertNull(jsObject.getBoxedBoolean("asdf"));  // undefined property
    assertNull(jsObject.getBoxedBoolean("undef"));  // actual property set to undefined
  }

  public void testGetNumber() throws Exception {
    assertEquals(1d, jsObject.getNumber("a"));
    assertEquals(2.1, jsObject.getNumber("b"));
    assertEquals(0d, jsObject.getNumber("c"));
    // will cast non-numeric values to Number
    assertTrue(Double.isNaN(jsObject.getNumber("asdf")));  // undefined property
    assertTrue(Double.isNaN(jsObject.getNumber("str")));
    assertTrue(Double.isNaN(jsObject.getNumber("undef")));  // actual property set to undefined
    assertEquals(1d, jsObject.getNumber("yes"));
    assertEquals(0d, jsObject.getNumber("no"));
    assertEquals(0d, jsObject.getNumber("emptyArr"));
    assertEquals(0d, jsObject.getNumber("obj2"));
  }

  public void testGetBoxedNumber() throws Exception {
    assertEquals(1d, jsObject.getBoxedNumber("a"));
    assertEquals(2.1, jsObject.getBoxedNumber("b"));
    assertEquals(0d, jsObject.getBoxedNumber("c"));
    // non-numeric values will return null
    assertNull(jsObject.getBoxedNumber("asdf"));  // undefined property
    assertNull(jsObject.getBoxedNumber("str"));
    assertNull(jsObject.getBoxedNumber("undef"));  // actual property set to undefined
    assertNull(jsObject.getBoxedNumber("yes"));
    assertNull(jsObject.getBoxedNumber("no"));
    assertNull(jsObject.getBoxedNumber("emptyArr"));
    assertNull(jsObject.getBoxedNumber("obj2"));
  }

  public void testHasKey() throws Exception {
    for (String key : keys) {
      assertTrue(jsObject.hasKey(key));
    }
    assertFalse(jsObject.hasKey("asdf"));  // undefined property
  }

  public void testHasOwnProperty() throws Exception {
    for (String key : keys) {
      assertTrue(jsObject.hasOwnProperty(key));
    }
    assertFalse(jsObject.hasOwnProperty("asdf"));  // undefined property

    // TODO: test some inherited properties by setting a prototype on the object?
  }

  public void testTypeOf() throws Exception {
    ImmutableMultimap<JsType, String> keysByType = ImmutableMultimap.<JsType, String>builder()
        .putAll(JsType.UNDEFINED, "undef")
        .putAll(JsType.BOOLEAN, "yes", "no")
        .putAll(JsType.STRING, "str")
        .putAll(JsType.FUNCTION, "fun")
        .putAll(JsType.OBJECT, "arr", "emptyArr", "obj", "obj2").build();

    for (Map.Entry<JsType, String> entry : keysByType.entries()) {
      assertTypeOf(entry.getKey(), entry.getValue());
    }
    // should return null (rather than "undefined") for missing properties
    assertTypeOf(null, "asdf");
    
  }

  private void assertTypeOf(JsType expected, String key) {
    assertEquals(key, expected, jsObject.typeOf(key));
    assertEquals(key, expected != null ? expected.getNativeName() : null, jsObject.nativeTypeOf(key));
  }

  public void testSet() throws Exception {
    // test overwriting existing properties
    jsObject.set("a", array);
    assertEquals(array, jsObject.getObject("a"));
    assertEquals(JsType.OBJECT, jsObject.typeOf("a"));
    
    jsObject.set("b", "Hello");
    assertEquals("Hello", jsObject.getString("b"));
    assertEquals(JsType.STRING, jsObject.typeOf("b"));
    
    jsObject.set("c", 12.34);
    assertEquals(12.34, jsObject.getNumber("c"));
    assertEquals(JsType.NUMBER, jsObject.typeOf("c"));

    jsObject.setUndefined("d");
    assertNull(jsObject.getObject("d"));
    assertEquals(JsType.UNDEFINED, jsObject.typeOf("d"));
  }

  public void testDelete() throws Exception {
    assertTrue(jsObject.hasKey("a"));
    assertEquals(1d, jsObject.getNumber("a"));
    jsObject.delete("a");
    assertFalse(jsObject.hasKey("a"));
    assertTrue(Double.isNaN(jsObject.getNumber("a")));
  }

  public void testKeys() throws Exception {
    assertArraysEqual(keys, jsObject.keys().toJavaArray());
  }

  public void testMerge() throws Exception {
    // will overwrite 2 props and add 2 new ones
    jsObject.merge(JsObject.create()
        .set("a", array)
        .set("b", "Hello")
        .set("x", 1)
        .set("y", true)
    );
    assertArraysEqual(ArrayUtils.merge(keys, new String[]{"x", "y"}), jsObject.keys().toJavaArray());

    assertEquals(array, jsObject.getObject("a"));
    assertEquals("Hello", jsObject.getString("b"));
    assertEquals(1d, jsObject.getNumber("x"));
    assertTrue(jsObject.getBoolean("y"));
  }

}