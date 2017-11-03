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
import solutions.trsoftware.commons.client.util.CollectionUtils;

import java.util.Collection;

/**
 * Date: May 30, 2008 Time: 5:15:25 PM
 *
 * @author Alex
 * @deprecated Using GsonJSONParser now (Gson is a better quality lib than json.org) 
 */
public class OrgJSONObject implements JSONObject {
  org.json.JSONObject delegate;

  public OrgJSONObject(org.json.JSONObject delegate) {
    if (delegate == null) // the whole object should be null, if the delegate is null
      throw new NullPointerException();
    this.delegate = delegate;
  }

  public boolean getBoolean(String key) {
    try {
      return delegate.getBoolean(key);
    }
    catch (JSONException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public double getDouble(String key) {
    try {
      return delegate.getDouble(key);
    }
    catch (JSONException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public int getInteger(String key) {
    try {
      return delegate.getInt(key);
    }
    catch (JSONException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public JSONArray getArray(String key) {
    try {
      if (delegate.isNull(key))
        return null; // avoid exception
      return new OrgJSONArray(delegate.getJSONArray(key));
    }
    catch (JSONException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public JSONObject getObject(String key) {
    try {
      if (delegate.isNull(key))
        return null; // avoid exception
      return new OrgJSONObject(delegate.getJSONObject(key));
    }
    catch (org.json.JSONException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public long getLong(String key) {
    try {
      return delegate.getLong(key);
    }
    catch (org.json.JSONException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public String getString(String key) {
    try {
      if (delegate.isNull(key))
        return null;  // avoid returning the string "null"
      return delegate.getString(key);
    }
    catch (org.json.JSONException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public boolean hasKey(String key) {
    return delegate.has(key);
  }

  public Collection<String> keys() {
    return CollectionUtils.asList(delegate.keys());
  }

  @Override
  public String toString() {
    return delegate.toString();
  }
}
