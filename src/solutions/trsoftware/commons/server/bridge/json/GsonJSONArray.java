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

package solutions.trsoftware.commons.server.bridge.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import solutions.trsoftware.commons.client.bridge.json.JSONArray;
import solutions.trsoftware.commons.client.bridge.json.JSONObject;

/**
 * Jan 13, 2009
 *
 * @author Alex
 */
public class GsonJSONArray implements JSONArray {
  private JsonArray delegate;

  public GsonJSONArray(JsonArray delegate) {
    this.delegate = delegate;
  }

  public int size() {
    return delegate.size();
  }

  public boolean getBoolean(int index) {
    return delegate.get(index).getAsBoolean();
  }

  public int getInteger(int index) {
    return delegate.get(index).getAsInt();
  }

  public long getLong(int index) {
    return delegate.get(index).getAsLong();
  }

  public double getDouble(int index) {
    return delegate.get(index).getAsDouble();
  }

  public String getString(int index) {
    JsonElement value = delegate.get(index);
    if (!value.isJsonNull())
      return value.getAsString();
    return null;
  }

  public JSONObject getObject(int index) {
    JsonElement value = delegate.get(index);
    if (!value.isJsonNull())
      return new GsonJSONObject(value.getAsJsonObject());
    return null;
  }

  public JSONArray getArray(int index) {
    JsonElement value = delegate.get(index);
    if (!value.isJsonNull())
      return new GsonJSONArray(value.getAsJsonArray());
    return null;
  }
}
