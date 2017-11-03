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

import org.json.JSONException;
import solutions.trsoftware.commons.client.bridge.json.JSONArray;
import solutions.trsoftware.commons.client.bridge.json.JSONObject;

/**
 * Date: May 30, 2008 Time: 5:18:21 PM
 *
 * @author Alex
 * @deprecated Using GsonJSONParser now (Gson is a better quality lib than json.org)
 */
public class OrgJSONArray implements JSONArray {
  private org.json.JSONArray delegate;

  public OrgJSONArray(org.json.JSONArray jsonArray) {
    if (jsonArray == null) // the whole object should be null, if the delegate is null
      throw new NullPointerException();
    delegate = jsonArray;
  }

  public int size() {
    return delegate.length();
  }

  public boolean getBoolean(int index) {
    try {
      return delegate.getBoolean(index);
    }
    catch (JSONException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public double getDouble(int index) {
    try {
      return delegate.getDouble(index);
    }
    catch (JSONException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public int getInteger(int index) {
    try {
      return delegate.getInt(index);
    }
    catch (JSONException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public JSONArray getArray(int index) {
    try {
      if (delegate.isNull(index))
        return null; // avoid exception
      return new OrgJSONArray(delegate.getJSONArray(index));
    }
    catch (JSONException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public JSONObject getObject(int index) {
    try {
      if (delegate.isNull(index))
        return null; // avoid exception
      return new OrgJSONObject(delegate.getJSONObject(index));
    }
    catch (JSONException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public long getLong(int index) {
    try {
      return delegate.getLong(index);
    }
    catch (JSONException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public String getString(int index) {
    try {
      if (delegate.isNull(index))
        return null;  // avoid returning the string "null"
      return delegate.getString(index);
    }
    catch (JSONException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString() {
    return delegate.toString();
  }
}
