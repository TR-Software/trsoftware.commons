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

package solutions.trsoftware.commons.client.bridge.json.impl;

import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import solutions.trsoftware.commons.client.bridge.json.JSONArray;
import solutions.trsoftware.commons.client.bridge.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;

/**
 * Date: May 30, 2008 Time: 4:42:24 PM
 *
 * @author Alex
 */
public class GwtJSONObject implements JSONObject {

  private final com.google.gwt.json.client.JSONObject object;

  public GwtJSONObject(@Nonnull com.google.gwt.json.client.JSONObject object) {
    this.object = requireNonNull(object, "object");
  }

  public int getInteger(String key) {
    return (int)get(key).isNumber().doubleValue();
  }

  @Nullable
  @Override
  public Integer getNullableInteger(String key) {
    JSONValue jsonValue = get(key);
    if (jsonValue != null) {
      JSONNumber number = jsonValue.isNumber();
      if (number != null) {
        // TODO(3/15/2025): why parsing string rather than number.doubleValue()?
        return Integer.valueOf(number.toString());
      }
    }
    return null;
    /*
     TODO(10/5/2024): create equivalents of this method for other wrapper types (Long, Boolean, Double, etc.),
      and deprecate the original methods in favor of the nullable kind
      - or even better, create method getNumber, returning a nullable Number
        (wrapped with new Double(JSONNumber.doubleValue()) in GWT, and jsonElement.getAsNumber() in GSON)
    */
  }

  /**
   * @return the value of the specified property, or {@code null} if the property does not exist
   */
  @Nullable
  private JSONValue get(String key) {
    return object.get(key);
  }

  public long getLong(String key) {
    return (long)get(key).isNumber().doubleValue();
  }

  public boolean getBoolean(String key) {
    return get(key).isBoolean().booleanValue();
  }

  public double getDouble(String key) {
    return get(key).isNumber().doubleValue();
  }

  public String getString(String key) {
    JSONValue value = get(key);
    if (value != null) {
      JSONString jsonString = value.isString();
      if (jsonString != null)
        return jsonString.stringValue();
    }
    return null;
  }

  public JSONObject getObject(String key) {
    com.google.gwt.json.client.JSONObject obj = get(key).isObject();
    if (obj != null)
      return new GwtJSONObject(obj);
    return null;
  }

  public JSONArray getArray(String key) {
    com.google.gwt.json.client.JSONArray arr = get(key).isArray();
    if (arr != null)
      return new GwtJSONArray(arr);
    return null;
  }

  public boolean hasKey(String key) {
    return object.containsKey(key);
  }

  @Override
  public String toString() {
    return object.toString();
  }

  public Set<String> keys() {
    return object.keySet();
  }

  @Override
  public boolean isNumber(String key) {
    return test(key, JSONValue::isNumber);
  }

  @Override
  public boolean isBoolean(String key) {
    return test(key, JSONValue::isBoolean);
  }

  @Override
  public boolean isString(String key) {
    return test(key, JSONValue::isString);
  }

  @Override
  public boolean isObject(String key) {
    return test(key, JSONValue::isObject);
  }

  @Override
  public boolean isArray(String key) {
    return test(key, JSONValue::isArray);
  }

  @Override
  public boolean isNull(String key) {
    return test(key, JSONValue::isNull);
  }
  
  /**
   * Tests that the specified mapper returns a non-null result for the specified key if the key exists.
   *
   * @param mapper a typed value getter (e.g. {@link JSONValue#isNumber()})
   * @return {@code true} iff the key exists and the mapper returns a non-null result
   */
  private boolean test(String key, @Nonnull UnaryOperator<JSONValue> mapper) {
    return testValue(get(key), mapper);
  }

  /**
   * Tests that the mapper getter returns a non-null result for the specified value.
   *
   * @param mapper a typed value getter (e.g. {@link JSONValue#isNumber()})
   * @return {@code true} iff the value is non-null and the mapper returns a non-null result
   */
  static boolean testValue(@Nullable JSONValue value, @Nonnull UnaryOperator<JSONValue> mapper) {
    return value != null && mapper.apply(value) != null;
  }
}
