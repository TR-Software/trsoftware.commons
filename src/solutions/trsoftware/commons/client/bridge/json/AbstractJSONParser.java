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
 * @author Alex, 3/4/2016
 */
public abstract class AbstractJSONParser implements JSONParser {

  /**
   * {@inheritDoc}
   */
  @Override
  public final String safeUrlDecode(String str) {
    if (str == null)
      return null;
    try {
      return unsafeUrlDecode(str);
    }
    catch (Throwable e) {
      return str;  // return the original string, which could not be decoded
    }
  }

  protected abstract String unsafeUrlDecode(String str);
}
