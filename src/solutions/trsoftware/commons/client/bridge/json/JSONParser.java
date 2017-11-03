package solutions.trsoftware.commons.client.bridge.json;

/**
 * Supports cross platform (GWT-json.org) json parsing.
 *
 * @author Alex
 */
public interface JSONParser {

  public JSONObject parseObject(String json);
  public JSONArray parseArray(String json);

  /**
   * URL-decodes the argument without throwing an exception.
   * @return The URL-decoded version of the arg, or if it's null or not properly encoded, the arg is returned unmodified.
   */
  public String safeUrlDecode(String str);
}
