/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

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
