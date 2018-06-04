/*
 * Copyright 2018 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

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
