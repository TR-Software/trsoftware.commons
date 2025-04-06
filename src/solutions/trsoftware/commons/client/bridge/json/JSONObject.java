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

package solutions.trsoftware.commons.client.bridge.json;

import javax.annotation.Nullable;
import java.util.Set;


/**
 * Supports cross platform (GWT-json.org) json parsing.
 *
 * @author Alex
 */
public interface JSONObject {

  int getInteger(String key);

  @Nullable
  Integer getNullableInteger(String key);
  /* TODO(10/12/2024): create method getNumber, returning a nullable Number
       (wrapped with new Double(JSONNumber.doubleValue()) in GWT, and jsonElement.getAsNumber() in GSON) */

  long getLong(String key);
  boolean getBoolean(String key);
  double getDouble(String key);
  /**
   * @return The value of the given attribute or null if either the mapping doesn't exist or the value is not a string.
   * Call {@link #hasKey(String)} to determine if the mapping doesn't exist or if the mapped value is null or not a
   * string. TODO: unit test the null case
   */
  String getString(String key);
  /**
   * @return The value of the given attribute or null if either the mapping doesn't exist or the value is either null
   * or not a string. Call {@link #hasKey(String)} to determine if the mapping doesn't exist or if the mapped value is
   * null or not an object. TODO: unit test the null case
   */
  JSONObject getObject(String key);
  JSONArray getArray(String key);
  boolean hasKey(String key);
  /** Returns a properly formatted JSON string representation of this object. */
  String toString();
  Set<String> keys();
}
