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

package solutions.trsoftware.commons.server.bridge.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import solutions.trsoftware.commons.client.bridge.json.JSONArray;
import solutions.trsoftware.commons.client.bridge.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Jan 13, 2009
 *
 * @author Alex
 */
public class GsonJSONObject implements JSONObject {
  private final JsonObject object;

  public GsonJSONObject(@Nonnull JsonObject object) {
    this.object = requireNonNull(object, "object");
  }

  public int getInteger(String key) {
    return get(key).getAsInt();
  }

  @Nullable
  @Override
  public Integer getNullableInteger(String key) {
    JsonElement jsonElement = get(key);
    if (jsonElement != null && jsonElement.isJsonPrimitive())
      return jsonElement.getAsInt();
    return null;
    /*
     TODO(10/5/2024): create equivalents of this method for other wrapper types (Long, Boolean, Double, etc.),
      and deprecate the original methods in favor of the nullable kind
      - or even better, create method getNumber, returning a nullable Number
        (using jsonElement.getAsNumber())
    */
  }

  public long getLong(String key) {
    return get(key).getAsLong();
  }

  public boolean getBoolean(String key) {
    return get(key).getAsBoolean();
  }

  public double getDouble(String key) {
    return get(key).getAsDouble();
  }

  public String getString(String key) {
    JsonElement value = get(key);
    if (value != null && !value.isJsonNull() && value.isJsonPrimitive())
      return value.getAsString();
    return null;
  }

  public JSONObject getObject(String key) {
    if (object.has(key) && !get(key).isJsonNull())  // we don't emulate JSON nulls as objects
      return new GsonJSONObject(object.getAsJsonObject(key));
    return null;
  }

  public JSONArray getArray(String key) {
    if (object.has(key) && !get(key).isJsonNull())  // we don't emulate JSON nulls as objects
      return new GsonJSONArray(object.getAsJsonArray(key));
    return null;
  }

  public boolean hasKey(String key) {
    return object.has(key);
  }

  public Set<String> keys() {
    return object.keySet();
  }

  // TODO(9/8/2026): unused method: getDelegate
  public JsonObject getDelegate() {
    return object;
  }

  @Override
  public boolean isNumber(String key) {
    return testPrimitive(key, JsonPrimitive::isNumber);
  }

  @Override
  public boolean isBoolean(String key) {
    return testPrimitive(key, JsonPrimitive::isBoolean);
  }

  @Override
  public boolean isString(String key) {
    return testPrimitive(key, JsonPrimitive::isString);
  }

  @Override
  public boolean isObject(String key) {
    return test(key, JsonElement::isJsonObject);
  }

  @Override
  public boolean isArray(String key) {
    return test(key, JsonElement::isJsonArray);
  }

  @Override
  public boolean isNull(String key) {
    return test(key, JsonElement::isJsonNull);
  }

  /**
   * Tests the given predicate on the value of the specified key if the key exists.
   * @return {@code true} iff the key exists and its value satisfies the predicate
   */
  private boolean test(String key, Predicate<JsonElement> predicate) {
    JsonElement el = get(key);
    return el != null && predicate.test(el);
  }

  /**
   * Tests the given predicate on the value of the specified key if the key exists and the value is a {@link JsonPrimitive}.
   * @return {@code true} iff the key exists and its value satisfies the predicate
   */
  private boolean testPrimitive(String key, Predicate<JsonPrimitive> predicate) {
    JsonElement el = get(key);
    return el != null && el.isJsonPrimitive() && predicate.test(el.getAsJsonPrimitive());
  }

  /**
   * Returns the member with the specified key.
   *
   * @param key name of the member that is being requested.
   * @return the member matching the name, or {@code null} if no such member exists.
   */
  @Nullable
  private JsonElement get(String key) {
    return object.get(key);
  }

  @Override
  public String toString() {
    return object.toString();
  }
}
