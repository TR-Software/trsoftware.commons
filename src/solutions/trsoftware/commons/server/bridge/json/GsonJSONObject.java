package solutions.trsoftware.commons.server.bridge.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import solutions.trsoftware.commons.client.bridge.json.JSONArray;
import solutions.trsoftware.commons.client.bridge.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Jan 13, 2009
 *
 * @author Alex
 */
public class GsonJSONObject implements JSONObject {
  JsonObject delegate;

  public GsonJSONObject(JsonObject delegate) {
    this.delegate = delegate;
  }

  public int getInteger(String key) {
    return delegate.get(key).getAsInt();
  }

  public long getLong(String key) {
    return delegate.get(key).getAsLong();
  }

  public boolean getBoolean(String key) {
    return delegate.get(key).getAsBoolean();
  }

  public double getDouble(String key) {
    return delegate.get(key).getAsDouble();
  }

  public String getString(String key) {
    JsonElement value = delegate.get(key);
    if (value != null && !value.isJsonNull() && value.isJsonPrimitive())
      return value.getAsString();
    return null;
  }

  public JSONObject getObject(String key) {
    if (delegate.has(key) && !delegate.get(key).isJsonNull())  // we don't emulate JSON nulls as objects
      return new GsonJSONObject(delegate.getAsJsonObject(key));
    return null;
  }

  public JSONArray getArray(String key) {
    if (delegate.has(key) && !delegate.get(key).isJsonNull())  // we don't emulate JSON nulls as objects
      return new GsonJSONArray(delegate.getAsJsonArray(key));
    return null;
  }

  public boolean hasKey(String key) {
    return delegate.has(key);
  }

  public Collection<String> keys() {
    Set<Map.Entry<String,JsonElement>> entries = delegate.entrySet();
    ArrayList<String> keys = new ArrayList<String>(entries.size());
    for (Map.Entry<String, JsonElement> entry : entries) {
      keys.add(entry.getKey());
    }
    return keys;
  }

  public JsonObject getDelegate() {
    return delegate;
  }
}
