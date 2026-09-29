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

package solutions.trsoftware.commons.server.bridge.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import solutions.trsoftware.commons.client.bridge.json.JSONArray;
import solutions.trsoftware.commons.client.bridge.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * @since Jan 13, 2009
 * @author Alex
 */
public class GsonJSONArray implements JSONArray {
  private final JsonArray array;

  public GsonJSONArray(@Nonnull JsonArray array) {
    this.array = requireNonNull(array, "array");
  }

  public int size() {
    return array.size();
  }

  public boolean getBoolean(int index) {
    return array.get(index).getAsBoolean();
  }

  public int getInteger(int index) {
    return array.get(index).getAsInt();
  }

  public long getLong(int index) {
    return array.get(index).getAsLong();
  }

  public double getDouble(int index) {
    return array.get(index).getAsDouble();
  }

  public String getString(int index) {
    JsonElement value = array.get(index);
    if (!value.isJsonNull())
      return value.getAsString();
    return null;
  }

  public JSONObject getObject(int index) {
    JsonElement value = array.get(index);
    if (!value.isJsonNull())
      return new GsonJSONObject(value.getAsJsonObject());
    return null;
  }

  public JSONArray getArray(int index) {
    JsonElement value = array.get(index);
    if (!value.isJsonNull())
      return new GsonJSONArray(value.getAsJsonArray());
    return null;
  }

  @Override
  public boolean isNumber(int index) {
    return testPrimitive(index, JsonPrimitive::isNumber);
  }

  @Override
  public boolean isBoolean(int index) {
    return testPrimitive(index, JsonPrimitive::isBoolean);
  }

  @Override
  public boolean isString(int index) {
    return testPrimitive(index, JsonPrimitive::isString);
  }

  @Override
  public boolean isObject(int index) {
    return test(index, JsonElement::isJsonObject);
  }

  @Override
  public boolean isArray(int index) {
    return test(index, JsonElement::isJsonArray);
  }

  @Override
  public boolean isNull(int index) {
    return test(index, JsonElement::isJsonNull);
  }

  /**
   * Tests the given predicate on the value of the specified index if the index exists.
   * @return {@code true} iff the key exists and its value satisfies the predicate
   */
  private boolean test(int index, Predicate<JsonElement> predicate) {
    JsonElement el = get(index);
    return el != null && predicate.test(el);
  }

  /**
   * Tests the given predicate on the value of the specified index if the index exists and the value is a {@link JsonPrimitive}.
   * @return {@code true} iff the key exists and its value satisfies the predicate
   */
  private boolean testPrimitive(int index, Predicate<JsonPrimitive> predicate) {
    JsonElement el = get(index);
    return el != null && el.isJsonPrimitive() && predicate.test(el.getAsJsonPrimitive());
  }

  /**
   * @return the element at the specified index or {@code null} if the index is out-of-bounds
   */
  @Nullable
  private JsonElement get(int index) {
    try {
      return array.get(index);
    }
    catch (IndexOutOfBoundsException ex) {
      return null;
    }
  }
}
