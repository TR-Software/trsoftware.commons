/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.client.jso;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import solutions.trsoftware.commons.client.dom.DomUtils;
import solutions.trsoftware.commons.client.dom.ParentNode;

import javax.annotation.Nullable;

/**
 * Extends {@link Document} to add certain DOM API methods not provided by GWT's {@link Document} class.
 *
 * @see Document
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document">MDN Reference</a>
 * @author Alex, 10/6/2015
 */
public class JsDocument extends Document {
  private static final String SVG_NAMESPACE_URI = "http://www.w3.org/2000/svg";

  // TODO: rename this class to Document and move to solutions.trsoftware.commons.client.dom

  protected JsDocument() {
  }

  public static JsDocument get() {
    return Document.get().cast();
  }

  /**
   * @return {@code document.activeElement}, which is the currently focused element
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/activeElement#Browser_compatibility">Browser compatibility</a>
   */
  public final native Element getActiveElement() /*-{
    return this.activeElement;
  }-*/;

  /**
   * @return the result of invoking the JS DOM method <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/getSelection">document.getSelection()</a>
   * or {@code null} if the browser doesn't support this API.
   */
  public final native JsSelection getSelection()/*-{
    if (this.getSelection)
      return this.getSelection();
    return null;
  }-*/;


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
   * @see ParentNode#querySelectorAll(String)
   * @see DomUtils#asList(NodeList)
   */
  @Nullable
  public final NodeList<Element> querySelectorAll(String selectors) {
    return ParentNode.as(get()).querySelectorAll(selectors);
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
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/querySelector">MDN Reference</a>
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/querySelector#Browser_compatibility">Browser compatibility</a>
   * @see <a href="https://caniuse.com/#feat=queryselector">Can I use querySelector/querySelectorAll?</a>
   * @see #querySelectorAll(String)
   * @see ParentNode#querySelector(String)
   * @see DomUtils#asList(NodeList)
   */
  @Nullable
  public final Element querySelector(String selectors) {
    return ParentNode.as(get()).querySelector(selectors);
  }

  /**
   * Creates an element with the SVG namespace URI and qualified name.
   * To create an element without specifying a namespace URI, use the {@link #createElement(String)} method.
   *
   * @param namespaceURI The namespace URI to associate with the element.
   *   The namespaceURI property of the created element is initialized with this value.
   * @param qualifiedName The type of element to be created.
   *   The nodeName property of the created element is initialized with this value.
   * @return the new element
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/createElementNS">MDN</a>
   */
  public final native Element createElementNS(String namespaceURI, String qualifiedName) /*-{
    return this.createElementNS(namespaceURI, qualifiedName);
  }-*/;

  /**
   * Creates an element with the SVG namespace URI ({@value #SVG_NAMESPACE_URI}).
   *
   * @param qualifiedName The type of element to be created.
   *   The nodeName property of the created element is initialized with this value.
   * @return the new element
   * @see #createElementNS(String, String)
   */
  public final Element createSVGElement(String qualifiedName) {
    return createElementNS(SVG_NAMESPACE_URI, qualifiedName);
  }
}
