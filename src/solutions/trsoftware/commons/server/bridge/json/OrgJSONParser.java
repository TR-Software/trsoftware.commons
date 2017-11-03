package solutions.trsoftware.commons.server.bridge.json;

import org.json.JSONException;
import solutions.trsoftware.commons.client.bridge.json.AbstractJSONParser;
import solutions.trsoftware.commons.client.bridge.json.JSONArray;
import solutions.trsoftware.commons.client.bridge.json.JSONObject;
import solutions.trsoftware.commons.server.util.ServerStringUtils;

/**
 * Date: May 30, 2008 Time: 5:26:59 PM
 *
 * @author Alex
 * @deprecated Using GsonJSONParser now (Gson is a better quality lib than json.org)
 */
public class OrgJSONParser extends AbstractJSONParser {
  public JSONObject parseObject(String json) {
    try {
      return new OrgJSONObject(new org.json.JSONObject(json));
    }
    catch (JSONException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public JSONArray parseArray(String json) {
    try {
      return new OrgJSONArray(new org.json.JSONArray(json));
    }
    catch (JSONException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  protected String unsafeUrlDecode(String str) {
    return ServerStringUtils.urlDecode(str);
  }
}
