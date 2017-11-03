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

package solutions.trsoftware.commons.client.bridge.json;

/**
 * Supports cross platform (GWT-json.org) json parsing.
 *
 * @author Alex
 */
public interface JSONParser {

  JSONObject parseObject(String json);
  JSONArray parseArray(String json);

  /**
   * URL-decodes the argument without throwing an exception.
   * @return The URL-decoded version of the arg, or if it's null or not properly encoded, the arg is returned unmodified.
   */
  String safeUrlDecode(String str);
}
