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

package solutions.trsoftware.commons.client.dom;

import solutions.trsoftware.commons.shared.util.StringUtils;

/**
 * A lookup table of key-value pairs used for translating between human readable
 * names for styles and their obfuscated counterparts.
 *
 * Jan 9, 2013
 *
 * @author Alex
 */
public class StyleObfuscator {

  /** The dynamic stylesheet that will be augmented with new rules */
  private DynamicStylesheet stylesheet = new DynamicStylesheet();

  /**
   * Generates a CSS class rule using an obfuscated class name.
   * Example: given "color:red;align:left;" this method might add a stylesheet
   * entry like ".zcvzsaf {color:red;align:left;}" and return the name "zcvzsaf".
   * @param cssRules the css markup rule for a new css class to be created.
   * @return the name of the generated class name.
   */
  public String newClassRule(String cssRules) {
    String name = StringUtils.randString(8);// TODO: store all values in hash set to ensure uniqueness
    StringBuilder cssBuilder = new StringBuilder(cssRules.length() + 15);
    cssBuilder.append(".").append(name).append("{").append(cssRules).append("}");
    System.out.println("Appending dynamic CSS: " + cssBuilder.toString());
    stylesheet.append(cssBuilder.toString());
    return name;
  }

}
