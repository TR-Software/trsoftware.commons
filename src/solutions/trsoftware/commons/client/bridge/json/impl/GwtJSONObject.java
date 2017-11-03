package solutions.trsoftware.commons.client.bridge.json.impl;

import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import solutions.trsoftware.commons.client.bridge.json.JSONArray;
import solutions.trsoftware.commons.client.bridge.json.JSONObject;

import java.util.Collection;

/**
 * Date: May 30, 2008 Time: 4:42:24 PM
 *
 * @author Alex
 */
public class GwtJSONObject implements JSONObject {

  com.google.gwt.json.client.JSONObject delegate;

  public GwtJSONObject(com.google.gwt.json.client.JSONObject object) {
    if (object == null) // the whole object should be null, if the delegate is null
      throw new NullPointerException();
    this.delegate = object;
  }

  public int getInteger(String key) {
    return (int)delegate.get(key).isNumber().doubleValue();
  }

  public long getLong(String key) {
    return (long)delegate.get(key).isNumber().doubleValue();
  }

  public boolean getBoolean(String key) {
    return delegate.get(key).isBoolean().booleanValue();
  }

  public double getDouble(String key) {
    return delegate.get(key).isNumber().doubleValue();
  }

  public String getString(String key) {
    JSONValue value = delegate.get(key);
    if (value != null) {
      JSONString jsonString = value.isString();
      if (jsonString != null)
        return jsonString.stringValue();
    }
    return null;
  }

  public JSONObject getObject(String key) {
    com.google.gwt.json.client.JSONObject obj = delegate.get(key).isObject();
    if (obj != null)
      return new GwtJSONObject(obj);
    return null;
  }

  public JSONArray getArray(String key) {
    com.google.gwt.json.client.JSONArray arr = delegate.get(key).isArray();
    if (arr != null)
      return new GwtJSONArray(arr);
    return null;
  }

  public boolean hasKey(String key) {
    return delegate.containsKey(key);
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  public Collection<String> keys() {
    return delegate.keySet();
  }
}
