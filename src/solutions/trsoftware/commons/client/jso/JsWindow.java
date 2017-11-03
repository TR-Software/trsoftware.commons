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

package solutions.trsoftware.commons.client.jso;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Element;

/**
 * A JSNI overlay type for the window object. Supports a subset of the methods provided by the various browser implementations
 * of window.
 *
 * NOTE: {@link elemental.js.html.JsWindow} is a more full-featured implementation
 * of this concept and is part of GWT's experimental new "Elemental" package.
 * However, that class doesn't compensate for lack of functionality of certain methods, and also Elemental only
 * works with SuperDevMode (will produce a GWT compiler error when running under the regular DevMode, see
 * http://stackoverflow.com/questions/17428265/adding-elemental-to-gwt )
 *
 * @author Alex, 10/6/2015
 */
public class JsWindow extends JsObject {

  protected JsWindow() {
  }

  /**
   * Gets the window within which this script is running.
   */
  public static native JsWindow get() /*-{
    return $wnd;
  }-*/;

  /**
   * @return {@code document.activeElement}, which is the currently focused element
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/activeElement#Browser_compatibility">Document.activeElement browser compatibility</a>
   */
  public final Element getActiveElement() {
    return (Element)getObject("activeElement");
  }

  /**
   * Gives the values of all the CSS properties of an element after applying the active stylesheets and resolving any basic computation those values may contain.
   * @return a style object containing the computed styles for the given element, or {@code null} if the browser doesn't support this API or the element is not valid.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/getComputedStyle#Browser_compatibility">Window.getComputedStyle browser compatibility</a>
   */
  public final native Style getComputedStyle(Element element, String pseudoElement) /*-{
    if (this.getComputedStyle)
      return this.getComputedStyle(element, pseudoElement);
  }-*/;


  /**
   * Same as {@link #getComputedStyle(Element, String)}, but without a pseudo-element.
   */
  public final Style getComputedStyle(Element element) {
    return getComputedStyle(element, null);
  }
}
