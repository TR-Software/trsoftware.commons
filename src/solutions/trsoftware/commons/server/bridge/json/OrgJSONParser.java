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
