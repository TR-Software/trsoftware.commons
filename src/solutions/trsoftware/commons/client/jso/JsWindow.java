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

import com.google.gwt.dom.client.Element;
import solutions.trsoftware.commons.client.css.CSSStyleDeclaration;

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
   * Computes the values of all the CSS properties of an element or pseudo-element after applying the active stylesheets
   * and resolving any basic computation those values may contain.
   *
   * @param element the Element for which to get the computed style; must be attached to the DOM
   * @param pseudoElement Optional string specifying the pseudo-element to match (e.g. {@code ::before}, {@code ::after}).
   *     Omitted (or null) for real elements
   * @return live CSSStyleDeclaration object, which updates automatically when the element's styles are changed,
   *     or {@code null} if the browser doesn't support this API or the element is not valid.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/getComputedStyle">Window.getComputedStyle (MDN)</a>
   */
  public final native CSSStyleDeclaration getComputedStyle(Element element, String pseudoElement) /*-{
    if (this.getComputedStyle)
      return this.getComputedStyle(element, pseudoElement);
  }-*/;


  /**
   * Computes the values of all the CSS properties of an element after applying the active stylesheets
   * and resolving any basic computation those values may contain.
   *
   * @param element the Element for which to get the computed style; must be attached to the DOM
   * @param pseudoElement Optional string specifying the pseudo-element to match (e.g. {@code ::before}, {@code ::after}).
   *     Omitted (or null) for real elements
   * @return live CSSStyleDeclaration object, which updates automatically when the element's styles are changed,
   *     or {@code null} if the browser doesn't support this API or the element is not valid.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/getComputedStyle">Window.getComputedStyle (MDN)</a>
   */
  public final CSSStyleDeclaration getComputedStyle(Element element) {
    return getComputedStyle(element, null);
  }

  /**
   * Returns the ratio of the resolution in physical pixels to the resolution in CSS pixels for the current display device.
   * <p>
   * This value could also be interpreted as the ratio of pixel sizes: the size of one CSS pixel to the size of one physical pixel.
   * In simpler terms, this tells the browser how many of the screen's actual pixels should be used to draw a single CSS pixel.
   * <p>
   * Page zooming affects the value of devicePixelRatio. When a page is zoomed in (made larger), the size of a CSS pixel increases,
   * and so the devicePixelRatio value increases.
   * Pinch-zooming does not affect devicePixelRatio, because this magnifies the page without changing the size of a CSS pixel.
   *
   * @return the value of {@code window.devicePixelRatio} or {@code 1} if not supported by current browser
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/devicePixelRatio">MDN reference</a>
   */
  public native final double getDevicePixelRatio() /*-{
    return this.devicePixelRatio || 1;
  }-*/;

}
