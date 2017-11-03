package solutions.trsoftware.commons.client.bridge.json.impl;

import com.google.gwt.core.client.JavaScriptObject;
import solutions.trsoftware.commons.client.bridge.json.AbstractJSONParser;
import solutions.trsoftware.commons.client.bridge.json.JSONArray;
import solutions.trsoftware.commons.client.bridge.json.JSONObject;
import solutions.trsoftware.commons.client.util.JavascriptUtils;

/**
 * GWT implementation of the cross-platform JSON parsing interface. 
 *
 * @author Alex
 */
public class GwtJSONParser extends AbstractJSONParser {
  
  public JSONObject parseObject(String json) {
    return new GwtJSONObject(com.google.gwt.json.client.JSONParser.parseStrict(json).isObject());
  }

  public static JSONObject parseObject(JavaScriptObject nativeObject) {
     return new GwtJSONObject(new com.google.gwt.json.client.JSONObject(nativeObject));
  }

  public JSONArray parseArray(String json) {
    return new GwtJSONArray(com.google.gwt.json.client.JSONParser.parseStrict(json).isArray());
  }

  public static JSONArray parseArray(JavaScriptObject nativeObject) {
    return new GwtJSONArray(new com.google.gwt.json.client.JSONArray(nativeObject));
  }

  @Override
  protected String unsafeUrlDecode(String str) {
    return JavascriptUtils.unsafeDecodeURIComponentImpl(str);
  }
}
