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

package solutions.trsoftware.commons.client.util.template;

import java.util.Map;

/**
 * Dec 10, 2008
*
* @author Alex
*/
public class StringPart implements TemplatePart {
  private final String str;

  public StringPart(String str) {
    this.str = str;
  }

  public StringBuilder write(StringBuilder buffer, Map<String, String> substitutions) {
    return buffer.append(str);
  }

  @Override
  public String toString() {
    return str;
  }
}
