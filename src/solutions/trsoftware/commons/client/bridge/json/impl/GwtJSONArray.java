package solutions.trsoftware.commons.client.bridge.json.impl;

import com.google.gwt.json.client.JSONString;
import solutions.trsoftware.commons.client.bridge.json.JSONArray;
import solutions.trsoftware.commons.client.bridge.json.JSONObject;

/**
 * Date: May 30, 2008 Time: 4:46:18 PM
 *
 * @author Alex
 */
public class GwtJSONArray implements JSONArray {
  private com.google.gwt.json.client.JSONArray delegate;

  public GwtJSONArray(com.google.gwt.json.client.JSONArray array) {
    if (array == null) // the whole object should be null, if the delegate is null
      throw new NullPointerException();
    this.delegate = array;
  }

  public int size() {
    return delegate.size();
  }

  public boolean getBoolean(int index) {
    return delegate.get(index).isBoolean().booleanValue();
  }

  public int getInteger(int index) {
    return (int)delegate.get(index).isNumber().doubleValue();
  }

  public long getLong(int index) {
    return (long)delegate.get(index).isNumber().doubleValue();
  }

  public double getDouble(int index) {
    return delegate.get(index).isNumber().doubleValue();
  }

  public String getString(int index) {
    JSONString jsonString = delegate.get(index).isString();
    if (jsonString != null)
      return jsonString.stringValue();
    return null;
  }

  public JSONObject getObject(int index) {
    com.google.gwt.json.client.JSONObject obj = delegate.get(index).isObject();
    if (obj != null)
      return new GwtJSONObject(obj);
    return null;
  }

  public JSONArray getArray(int index) {
    com.google.gwt.json.client.JSONArray arr = delegate.get(index).isArray();
    if (arr != null)
      return new GwtJSONArray(arr);
    return null;
  }

  @Override
  public String toString() {
    return delegate.toString();
  }
}
