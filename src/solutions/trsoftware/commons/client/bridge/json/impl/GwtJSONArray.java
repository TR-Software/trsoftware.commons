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

import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import solutions.trsoftware.commons.client.bridge.json.JSONArray;
import solutions.trsoftware.commons.client.bridge.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;
import static solutions.trsoftware.commons.client.bridge.json.impl.GwtJSONObject.testValue;

/**
 * Date: May 30, 2008 Time: 4:46:18 PM
 *
 * @author Alex
 */
public class GwtJSONArray implements JSONArray {
  private final com.google.gwt.json.client.JSONArray array;

  public GwtJSONArray(com.google.gwt.json.client.JSONArray array) {
    this.array = requireNonNull(array, "array");
  }

  public int size() {
    return array.size();
  }

  /**
   * Returns the value at the specified index position.
   *
   * @param index the index of the array item to retrieve
   * @return the value at this index, or {@code null} if this index is empty
   */
  @Nullable
  private JSONValue get(int index) {
    return array.get(index);
  }

  public boolean getBoolean(int index) {
    return get(index).isBoolean().booleanValue();
  }

  public int getInteger(int index) {
    return (int)get(index).isNumber().doubleValue();
  }

  public long getLong(int index) {
    return (long)get(index).isNumber().doubleValue();
  }

  public double getDouble(int index) {
    return get(index).isNumber().doubleValue();
  }

  public String getString(int index) {
    JSONString jsonString = get(index).isString();
    if (jsonString != null)
      return jsonString.stringValue();
    return null;
  }

  public JSONObject getObject(int index) {
    com.google.gwt.json.client.JSONObject obj = get(index).isObject();
    if (obj != null)
      return new GwtJSONObject(obj);
    return null;
  }

  public JSONArray getArray(int index) {
    com.google.gwt.json.client.JSONArray arr = get(index).isArray();
    if (arr != null)
      return new GwtJSONArray(arr);
    return null;
  }

  @Override
  public String toString() {
    return array.toString();
  }

  @Override
  public boolean isNumber(int index) {
    return test(index, JSONValue::isNumber);
  }

  @Override
  public boolean isBoolean(int index) {
    return test(index, JSONValue::isBoolean);
  }

  @Override
  public boolean isString(int index) {
    return test(index, JSONValue::isString);
  }

  @Override
  public boolean isObject(int index) {
    return test(index, JSONValue::isObject);
  }

  @Override
  public boolean isArray(int index) {
    return test(index, JSONValue::isArray);
  }

  @Override
  public boolean isNull(int index) {
    return test(index, JSONValue::isNull);
  }
  
  /**
   * Tests that the specified mapper returns a non-null result for the specified index if the index exists.
   * @return {@code true} iff the index exists and the mapper returns a non-null result
   */
  private boolean test(int index, @Nonnull UnaryOperator<JSONValue> mapper) {
    return testValue(get(index), mapper);
  }
}
