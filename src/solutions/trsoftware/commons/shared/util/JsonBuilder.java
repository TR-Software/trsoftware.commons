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

import solutions.trsoftware.commons.shared.util.mutable.MutableNumber;

import java.util.Map;

/**
 * Uses the builder pattern to write simple JSON data. This enables both GWT and
 * serverside code to easily emit JSON.
 *
 * <p>
 * <b>Object example</b>:
 *
 * <pre>
 *   new JsonBuilder().beginObject()
 *     .key("a").value("foo")
 *     .key("b").value(123)
 *     .key("c")
 *     .beginArray()
 *       .value("x")
 *       .value(1)
 *     .endArray()
 *   .endObject().toString();
 * </pre>
 *
 * will produce:
 *
 * <code>{"a": "foo", "b": 123, "c": ["x", 1]}</code>
 * </p>
 *
 * <p>
 * <b>Array example</b>:
 *
 * <pre>
 *   new JsonBuilder().beginArray()
 *     .value(1)
 *     .value(2)
 *     .value(3)
 *   .endArray().toString();
 * </pre>
 *
 * will produce:
 *
 * <code>[1, 2, 3]</code>
 * </p>
 *
 * <p>
 * <b>Complex array example</b>:
 *
 *
 * <pre>
 *   new JsonBuilder().beginArray()
 *     .value(1).value(
 *       new JsonBuilder().beginObject()
 *         .key("foo").value("bar")
 *       .endObject())
 *     .value(3)
 *   .endArray().toString();
 * </pre>
 *
 * will produce:
 *
 * <code>[1, {"foo": "bar"}, 3]</code>
 * </p>
 *
 * @author Alex
 */
public class JsonBuilder implements Jsonizable {
  private StringBuilder s;

  public JsonBuilder() {
    s = new StringBuilder();
  }

  public JsonBuilder(int capacity) {
    s = new StringBuilder(capacity);
  }

  public JsonBuilder beginObject() {
    s.append("{");
    return this;
  }

  public JsonBuilder endObject() {
    stripTrailingComma().append("}");
    return this;
  }

  public JsonBuilder beginArray() {
    s.append("[");
    return this;
  }

  public JsonBuilder endArray() {
    stripTrailingComma().append("]");
    return this;
  }

  private StringBuilder stripTrailingComma() {
    // strip any trailing spaces first
    int lastCharIndex = s.length() - 1;
    char lastChar = s.charAt(lastCharIndex);
    while (lastChar == ' ') {
      s.deleteCharAt(lastCharIndex);
      lastCharIndex = s.length() - 1;
      lastChar = s.charAt(lastCharIndex);
    }
    // now strip the trailing comma, if any
    if (s.charAt(lastCharIndex) == ',')
      return s.deleteCharAt(lastCharIndex);
    return s;
  }

  public JsonBuilder key(String key) {
    s.append('\"').append(key).append("\": ");
    return this;
  }

  public JsonBuilder value(String value) {
    s.append('\"').append(value).append("\", ");
    return this;
  }

  public JsonBuilder value(Number number) {
    s.append(numberToString(number)).append(", ");
    return this;
  }

  public JsonBuilder value(boolean value) {
    s.append(value).append(", ");
    return this;
  }

  /** Supports objects that define their own serialization policy */
  public JsonBuilder value(Jsonizable jsonizable) {
    jsonizable.dumpJson(s);
    s.append(", ");
    return this;
  }

  /**
   * The fallback method for all arguments that aren't obviously strings,
   * numbers, or booleans.  It will convert the argument to the closest
   * match to the above 3 types.
   * 
   * @param value
   * @return
   */
  public JsonBuilder value(Object value) {
    if (value == null)
      value("null");
    else if (value instanceof String)
      value((String)value);
    else if (value instanceof Number)
      value((Number)value);
    else if (value instanceof Boolean)
      value(((Boolean)value).booleanValue());
    else if (value instanceof Iterable)
      value((Iterable)value);
    else if (value instanceof Map)
      value((Map)value);
    else if (value instanceof Jsonizable)
      value((Jsonizable) value);
    else
      value(value.toString());  // use the toString representation of all other types
    return this;
  }

  // these are "macro" methods for easily dumping maps and collections into the JSON stream

  public JsonBuilder value(Iterable iterable) {
    writeIterable(iterable);
    s.append(", ");
    return this;
  }

  private JsonBuilder writeIterable(Iterable iterable) {
    beginArray();
    for (Object elt : iterable)
      value(elt);
    endArray();
    return this;
  }

  public JsonBuilder value(Map<?,?> map) {
    writeMap(map);
    s.append(", ");
    return this;
  }

  private JsonBuilder writeMap(Map<?,?> map) {
    beginObject();
    for (Map.Entry<?,?> entry : map.entrySet()) {
      key(entry.getKey().toString());  // the key must always be a string
      value(entry.getValue());
    }
    endObject();
    return this;
  }

  /**
   * An alternative to using this class as a builder.
   * @return the complete JSON string representation of the given map. 
   */
  public static String mapToJson(Map map) {
    return new JsonBuilder().writeMap(map).toString();
  }

  /**
   * An alternative to using this class as a builder.
   * @return the complete JSON string representation of the given collection. 
   */
  public static String iterableToJson(Iterable iterable) {
    return new JsonBuilder().writeIterable(iterable).toString();
  }


  public String toString() {
    return s.toString();
  }

  // The following two methods are borrowed from org.json.JSONObject, with slight changes

  /**
   * Produce a string from a Number.
   *
   * @param n A Number
   * @return A String.
   * @throws IllegalArgumentException If n is a non-finite number.
   */
  private String numberToString(Number n) throws IllegalArgumentException {
    testValidity(n);
    String s = n.toString();
    // NOTE (alexe): we use the default toString representation of the number,
    // with the exception of floating-point numbers, to which we
    // explicitly want to add a decimal point if it was stripped during the stringification
    // (to avoid them being parsed back as integer types)
    if (s.length() > 0 && isNumberFloatingPoint(n) && s.indexOf('.') < 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
      s += ".0";
    }
    return s;
  }


  /**
   * Throw an exception if the object is an NaN or infinite number.
   * @param o The object to test.
   * @throws IllegalArgumentException If o is a non-finite number.
   */
  private void testValidity(Object o) throws IllegalArgumentException {
    if (o != null) {
      if (o instanceof Double) {
        if (((Double)o).isInfinite() || ((Double)o).isNaN()) {
          throw new IllegalArgumentException("JSON does not allow non-finite numbers.");
        }
      }
      else if (o instanceof Float) {
        if (((Float)o).isInfinite() || ((Float)o).isNaN()) {
          throw new IllegalArgumentException("JSON does not allow non-finite numbers.");
        }
      }
    }
  }

  private boolean isNumberFloatingPoint(Number n) {
    if (n instanceof MutableNumber)
      n = ((MutableNumber)n).numberValue();
    return n instanceof Float || n instanceof Double;
  }

  public void dumpJson(StringBuilder sb) {
    sb.append(s);
  }

  /** Supports values that are already json strings, so we override the default behavior to write it out as an object (without quoting) */
  public static class PureJson implements Jsonizable {
    private String json;
    public PureJson(String json) {
      this.json = json;
    }
    public void dumpJson(StringBuilder sb) {
      sb.append(json);
    }
  }

}
