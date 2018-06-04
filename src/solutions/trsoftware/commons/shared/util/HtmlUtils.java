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

package solutions.trsoftware.commons.shared.util;


/**
 * These utility methods are useful for creating raw HTML elements with GWT,
 * for the HTML nad HTMLPanel widgets.
 *
 * @author Alex
 */
public class HtmlUtils {

  public static final String nbsp = "&nbsp;";

  /**
   * No-op value for the {@code href} attribute of an anchor element.
   * @see com.google.gwt.user.client.ui.Anchor#DEFAULT_HREF
   */
  public static final String VOID_HREF = "javascript:;";

  /**
   * Returns a "<span id=...</span>" string
   * Warning - the body string is not escaped and allows valid HTML.
   */
  public static String span(String id, String cssClassName, String body) {
    return element("span", id, cssClassName, body);
  }

  /**
   * Returns a "<div id=...</div>" string
   * Warning - the body string is not escaped and allows valid HTML.
   */
  public static String div(String id, String cssClassName, String body) {
    return element("div", id, cssClassName, body);
  }

  /**
   * Returns a "<elementName id=...</elementName>" string
   * Warning - the body string is not escaped and allows valid HTML. 
   */
  public static String element(String elementName, String id, String cssClassName, String innerHtml) {
    if (innerHtml == null)
      innerHtml = "";
    return "<" + elementName + attr("id", id) + attr("class", cssClassName) + ">" + innerHtml + "</"+elementName+">";
  }

  /**
   * Returns a "<elementName id=...</elementName>" string
   * Warning - the body string is not escaped and allows valid HTML.
   */
  public static String element(String elementName, String innerHtml) {
    if (innerHtml == null)
      innerHtml = "";
    return "<" + elementName + ">" + innerHtml + "</"+elementName+">";
  }

  /**
   * @param text the content of the comment
   * @return a valid HTML/XML comment string containing the given text.  For example, {@code "<!-- foo -->"} if the given
   * string is {@code "foo"}
   */
  public static String comment(String text) {
    return "<!-- " + text + " -->";
  }

  /**
   * Returns a name="value" string if the value arg is not null
   */
  private static String attr(String name, String value) {
    if (value == null)
      return "";
    return " " + name + "=\"" + value + "\"";
  }
}
