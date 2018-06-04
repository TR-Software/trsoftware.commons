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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: Nov 4, 2008 Time: 3:46:29 PM
 *
 * @author Alex
 */
public class HtmlBuilder {

  private static class HtmlElement {
    String name;
    Map<String,String> attrs = new LinkedHashMap<String, String>();
    String innerHtml;
    List<HtmlElement> children = new ArrayList<HtmlElement>();
    HtmlElement parent;

    private HtmlElement(String name, HtmlElement parent) {
      this.name = name;
      this.parent = parent;
    }
  }

  private HtmlElement current;
  private HtmlElement currentRoot;
  /** The top-level elements */
  private List<HtmlElement> roots = new ArrayList<HtmlElement>();

  public HtmlBuilder openTag(String elementName) {
    HtmlElement elt = new HtmlElement(elementName, current);
    if (currentRoot == null) {
      currentRoot = elt;
      current = currentRoot;
      roots.add(currentRoot);
    }
    else {
      current.children.add(elt);
      current = elt;
    }
    return this;
  }

  public HtmlBuilder attr(String name, String value) {
    if (value != null) {
      current.attrs.put(name, value);
    }
    return this;
  }

  public HtmlBuilder style(String value) {
    return attr("style", value);
  }

  public HtmlBuilder id(String value) {
    return attr("id", value);
  }

  public HtmlBuilder classAttr(String value) {
    return attr("class", value);
  }

  public HtmlBuilder closeTag() {
    current = current.parent;
    if (current == null)
      currentRoot = null;  // we've gotten back up to the top level
    return this;
  }

  public HtmlBuilder innerHtml(String html) {
    current.innerHtml = html;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder(512);
    for (HtmlElement root : roots) {
      writeElement(buffer, root);
    }
    return buffer.toString();
  }

  private StringBuilder writeElement(StringBuilder str, HtmlElement elt) {
    if (elt == null)
      return str;
    str.append("<").append(elt.name);
    for (Map.Entry<String, String> attr : elt.attrs.entrySet()) {
      str.append(" ").append(attr.getKey()).append("=\"").append(attr.getValue()).append("\"");
    }
    if (elt.children.isEmpty() && elt.innerHtml == null)
      str.append("/>");
    else {
      str.append(">");
      for (HtmlElement child : elt.children)
        writeElement(str, child);
      if (elt.innerHtml != null)
        str.append(elt.innerHtml);
      str.append("</").append(elt.name).append(">");
    }
    return str;
  }

}
