package solutions.trsoftware.commons.client.bridge.json;

/**
 * Supports cross platform (GWT-json.org) json parsing.
 *
 * @author Alex
 */
public interface JSONArray {

  public int size();

  public boolean getBoolean(int index);
  public int getInteger(int index);
  public long getLong(int index);
  public double getDouble(int index);
  public String getString(int index);
  public JSONObject getObject(int index);
  public JSONArray getArray(int index);
  /** Returns a properly formatted JSON string representation of this array. */
  public String toString();
}
