package solutions.trsoftware.commons.server.bridge.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import solutions.trsoftware.commons.client.bridge.json.AbstractJSONParser;
import solutions.trsoftware.commons.client.bridge.json.JSONArray;
import solutions.trsoftware.commons.client.bridge.json.JSONObject;
import solutions.trsoftware.commons.server.util.ServerStringUtils;

/**
 * Jan 13, 2009
 *
 * @author Alex
 */
public class GsonJSONParser extends AbstractJSONParser {
  public JSONObject parseObject(String json) {
    return new GsonJSONObject(parseWithGson(json).getAsJsonObject());
  }

  public JSONArray parseArray(String json) {
    return new GsonJSONArray(parseWithGson(json).getAsJsonArray());
  }

  private static JsonElement parseWithGson(String json) {
    try {
      return new JsonParser().parse(json);
    }
    catch (JsonSyntaxException e) {
      System.err.println("Error parsing JSON string: " + json);
      throw e;
    }
  }

  @Override
  protected String unsafeUrlDecode(String str) {
    return ServerStringUtils.urlDecode(str);
  }
}
