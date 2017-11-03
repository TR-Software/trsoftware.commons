package solutions.trsoftware.commons.client.bridge.json;

import java.util.Collection;


/**
 * Supports cross platform (GWT-json.org) json parsing.
 *
 * @author Alex
 */
public interface JSONObject {

  public int getInteger(String key);
  public long getLong(String key);
  public boolean getBoolean(String key);
  public double getDouble(String key);
  /**
   * @return The value of the given attribute or null if either the mapping doesn't exist or the value is not a string.
   * Call {@link #hasKey(String)} to determine if the mapping doesn't exist or if the mapped value is null or not a
   * string. TODO: unit test the null case
   */
  public String getString(String key);
  /**
   * @return The value of the given attribute or null if either the mapping doesn't exist or the value is either null
   * or not a string. Call {@link #hasKey(String)} to determine if the mapping doesn't exist or if the mapped value is
   * null or not an object. TODO: unit test the null case
   */
  public JSONObject getObject(String key);
  public JSONArray getArray(String key);
  public boolean hasKey(String key);
  /** Returns a properly formatted JSON string representation of this object. */
  public String toString();
  public Collection<String> keys();
}
