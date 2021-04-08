/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.commons.shared.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple builder for HTML strings.
 * <p>
 * <i>Note</i>: newer versions of GWT now provide a more powerful implementation of this concept (see
 * {@link com.google.gwt.dom.builder.shared.HtmlBuilderFactory})
 *
 * @author Alex
 * @since Nov 4, 2008
 */
public class HtmlBuilder {

  private static abstract class Node {
    List<Node> children = new ArrayList<>();
    Node parent;

    private Node(Node parent) {
      this.parent = parent;
    }

    public boolean addChild(Node node) {
      return children.add(node);
    }

    /**
     * Asserts that this node is a {@link ElementNode}
     * @return this node cast to {@link ElementNode}
     */
    ElementNode isElement() {
      // TODO: explicitly throw a ClassCastException?  (otherwise will be hard to tell what's up in compiled code)
      return (ElementNode)this;
    }

    /**
     * Writes itself to the given buffer.
     * @return the given buffer, for chaining
     */
    abstract StringBuilder write(StringBuilder str);
  }

  private static class ElementNode extends Node {
    String tagName;
    Map<String, String> attrs = new LinkedHashMap<>();
    String innerHtml;

    private ElementNode(String tagName, Node parent) {
      super(parent);
      this.tagName = tagName;
    }

    StringBuilder write(StringBuilder str) {
      str.append('<').append(tagName);
      for (Map.Entry<String, String> attr : attrs.entrySet()) {
        str.append(' ').append(attr.getKey()).append("=\"").append(attr.getValue()).append('"');
      }
      if (children.isEmpty() && innerHtml == null)
        str.append("/>");
      else {
        str.append('>');
        for (Node child : children)
          child.write(str);
        if (innerHtml != null)
          str.append(innerHtml);
        str.append("</").append(tagName).append('>');
      }
      return str;
    }
  }

  private static class TextNode extends Node {
    String text;

    public TextNode(String text, Node parent) {
      super(parent);
      this.text = text;
    }

    @Override
    StringBuilder write(StringBuilder str) {
      return str.append(text);
    }

    @Override
    public boolean addChild(Node node) {
      throw new UnsupportedOperationException("Text nodes can't have children");
    }
  }

  private Node current;
  private Node currentRoot;
  /** The top-level elements */
  private List<Node> roots = new ArrayList<>();

  public HtmlBuilder openTag(String tagName) {
    if (current instanceof TextNode)
      closeTag();  // close the current text node (they can't have children)
    return add(new ElementNode(tagName, current));
  }

  /** Adds a new TEXT node to the current node */
  public HtmlBuilder text(String text) {
    if (current instanceof TextNode) {
      // append to existing text node rather than adding a child
      ((TextNode)current).text += text;
      return this;
    }
    return add(new TextNode(text, current));
  }

  private HtmlBuilder add(Node node) {
    if (currentRoot == null) {
      currentRoot = node;
      current = currentRoot;
      roots.add(currentRoot);
    }
    else {
      current.addChild(node);
      current = node;
    }
    return this;
  }

  public HtmlBuilder attr(String name, String value) {
    if (value != null) {
      current.isElement().attrs.put(name, value);
    }
    return this;
  }

  /**
   * Adds a {@code style} attribute to the current element.
   *
   * @param value the value for the {@code style} attribute (e.g. {@code "background-color: red; font-size: 2em;"}).
   *     Suggestion: use {@link CssBuilder} to construct a string of CSS declarations.
   */
  public HtmlBuilder style(String value) {
    return attr("style", value);
  }

  public HtmlBuilder id(String value) {
    return attr("id", value);
  }

  public HtmlBuilder classAttr(String value) {
    return attr("class", value);
  }

  /**
   * Closes the tag corresponding to the last invocation of {@link #openTag(String)}
   */
  public HtmlBuilder closeTag() {
    current = current.parent;
    if (current == null)
      currentRoot = null;  // we've gotten back up to the top level
    return this;
  }

  /**
   * Closes all open tags up to the root level.
   */
  public HtmlBuilder closeAll() {
    while (current != null)
      closeTag();
    return this;
  }

  public HtmlBuilder innerHtml(String html) {
    current.isElement().innerHtml = html;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder(512);
    for (Node root : roots) {
      root.write(buffer);
    }
    return buffer.toString();
  }

}
