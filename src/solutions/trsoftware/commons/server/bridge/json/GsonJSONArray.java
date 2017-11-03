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
