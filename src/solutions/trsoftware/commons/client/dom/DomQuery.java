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

package solutions.trsoftware.commons.client.dom;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import solutions.trsoftware.commons.client.jso.JsDocument;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Performs lookups on the native DOM hierarchy.
 * 
 * Mar 25, 2010
 *
 * @author Alex
 */
public class DomQuery {


  /** Returns all children of parent matching the given tag name */
  public static <T extends Element> Set<T> findChildrenByTagName(Element parent, String tagName, int maxIterations) {
    if (parent == null)
      return Collections.emptySet();
    LinkedList<Node> toBeExpanded = new LinkedList<Node>();  // BFS queue
    toBeExpanded.add(parent);
    Set<Node> alreadyExpanded = new HashSet<Node>();
    Set<T> matched = new HashSet<T>();  // the matched children
    for (int i = 0; !toBeExpanded.isEmpty() && i < maxIterations; i++) {
      Node node = toBeExpanded.removeFirst();
      if (node == null || node.getNodeType() != Node.ELEMENT_NODE) // ignore non-element nodes (e.g. TEXT), which will throw an exception because they don't define hashCode
        continue;
      if (alreadyExpanded.contains(node))
        return Collections.emptySet();  // we have a cyclic tree for some reason, exit to avoid infinite recursion
      alreadyExpanded.add(node);
      if (node.getNodeName().equalsIgnoreCase(tagName)) {  // WARNING: instanceof usages like (node instanceof IFrameElement) will be true for any JSO, not just iframe nodes, so we have to check the tag name instead
        matched.add((T)node);
      }
      if (node.hasChildNodes()) {
        enqueueChildren(node, toBeExpanded);
      }
    }
    return matched;
  }

  /**
   * Adds the given node's children from a NodeList (which isn't a
   * java.util - compatible collection) to a standard List structure.
   *
   * This method may be overridden to facilitate unit testing.
   */
  private static void enqueueChildren(Node node, List<Node> queue) {
    NodeList<Node> childNodes = node.getChildNodes();
    if (childNodes == null)
      return;
    for (int i = 0; i < childNodes.getLength(); i++) {
      queue.add(childNodes.getItem(i));
    }
  }

  /**
   * Returns a {@link NodeList} of all of the document's {@link Element Elements}
   * matching the specified group of selectors.
   * <p>
   * <b>Example:</b> {@code document.querySelectorAll("#main, p.warning, p.note")}
   *
   * @param selectors A DOMString containing one or more selectors to match against.
   * This string must be a valid CSS selector string; if it's not, a SyntaxError exception is thrown.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document_object_model/Locating_DOM_elements_using_selectors">
   *   Locating DOM elements using selectors</a> for more information about using selectors to identify elements.
   * Multiple selectors may be specified by separating them using commas.
   *
   * @return A non-live {@link NodeList} containing one {@link Element} object for each element that matches at least one
   * of the specified selectors or an empty {@link NodeList} in case of no matches.  Returns {@code null} if the method
   * is not supported by the current browser.
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/querySelectorAll">MDN Reference</a>
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/querySelectorAll#Browser_compatibility">Browser compatibility</a>
   * @see <a href="https://caniuse.com/#feat=queryselector">Can I use querySelector/querySelectorAll?</a>
   * @see #querySelector(String)
   */
  @Nullable
  public static NodeList<Element> querySelectorAll(String selectors) {
    return JsDocument.get().querySelectorAll(selectors);
  }

  /**
   * Returns the first {@link Element} within the document that matches the specified selector, or group of selectors.
   * If no matches are found, null is returned.
   * <p>
   * <b>Example:</b> {@code document.querySelector("#main, p.warning, p.note")}
   *
   * @param selectors A DOMString containing one or more selectors to match against.
   * This string must be a valid CSS selector string; if it's not, a SyntaxError exception is thrown.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document_object_model/Locating_DOM_elements_using_selectors">
   *   Locating DOM elements using selectors</a> for more information about using selectors to identify elements.
   * Multiple selectors may be specified by separating them using commas.
   *
   * @return An {@link Element HTMLElement} representing the first element in the document that matches
   * the specified set of CSS selectors, or {@code null} if there are no matches (or if the browser doesn't support this
   * method).
   *
   * @see #querySelectorAll(String)
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/querySelector">MDN Reference</a>
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/querySelector#Browser_compatibility">Browser compatibility</a>
   * @see <a href="https://caniuse.com/#feat=queryselector">Can I use querySelector/querySelectorAll?</a>
   */
  @Nullable
  public static Element querySelector(String selectors) {
    return JsDocument.get().querySelector(selectors);
  };

}
