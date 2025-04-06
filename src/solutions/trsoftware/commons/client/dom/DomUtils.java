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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Window;
import solutions.trsoftware.commons.client.css.CSSStyleDeclaration;
import solutions.trsoftware.commons.client.jso.JsWindow;
import solutions.trsoftware.commons.shared.util.geometry.Rectangle2D;

import javax.annotation.Nonnull;
import java.util.List;

import static solutions.trsoftware.commons.client.css.CSSStyleDeclaration.parsePx;

/**
 * @author Alex
 * @since 4/12/2019
 */
public class DomUtils {

  /**
   * Wraps the given {@link NodeList} to make it compatible with the Java Collections Framework.
   *
   * @return a new instance of {@link NodeListWrapper} for the given {@link NodeList}
   */
  public static <T extends Node> List<T> asList(@Nonnull NodeList<T> nodeList) {
    return new NodeListWrapper<>(nodeList);
  }

  /**
   * Alias for {@link JsWindow#getComputedStyle(Element)}.
   *
   * Computes the values of all the CSS properties of an element after applying the active stylesheets
   * and resolving any basic computation those values may contain.
   *
   * @param element the Element for which to get the computed style; must be attached to the DOM
   * @param pseudoElement Optional string specifying the pseudo-element to match (e.g. {@code ::before}, {@code ::after}).
   *     Omitted (or null) for real elements
   * @return live CSSStyleDeclaration object, which updates automatically when the element's styles are changed,
   *     or {@code null} if the browser doesn't support this API or the element is not valid.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Window/getComputedStyle">Window.getComputedStyle (MDN)</a>
   * @see JsWindow#getComputedStyle(Element, String)
   */
  public static CSSStyleDeclaration getComputedStyle(Element element) {
    return JsWindow.get().getComputedStyle(element);
  }

  /**
   * Returns a {@code DOMRect} object providing information about the size of an element
   * and its position relative to the viewport.
   * <p>
   * The returned value is the smallest rectangle which contains the entire element, including its padding and
   * border-width. The left, top, right, bottom, x, y, width, and height properties describe the position and size of
   * the overall rectangle in pixels. Properties other than width and height are relative to the top-left of the
   * viewport.
   * <p>
   * <b>Note:</b> the returned x and y coordinates could be negative if the window is currently scrolled such
   * that the element is outside the currently-visible viewport area.
   * The absolute coordinates can be obtained by adjusting for the window scroll offset, e.g.
   * <code>x + {@link Window#getScrollLeft()}</code> and <code>y + {@link Window#getScrollLeft()}</code>.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/getBoundingClientRect">MDN</a>
   *
   * @return the value of {@code elem.getBoundingClientRect()} or {@code null} if the method isn't supported by the browser (which is unlikely)
   */
  public static DOMRect getBoundingClientRect(Element elem) {
    return JsElement.as(elem).getBoundingClientRect();
  }

  /**
   * Returns the result of {@link #getBoundingClientRect(Element)} as a {@link Rectangle2D} object, with {@code x}/{@code y}
   * coords computed using {@link DOMRect#getAbsoluteX()} and {@link DOMRect#getAbsoluteY()}.
   */
  public static Rectangle2D getBoundingRectangle2D(Element elem) {
    return new Rectangle2D(getBoundingClientRect(elem));
  }

  /**
   * @return {@code true} if the given node or element is a descendant of the document's {@code <body>}
   * @see JsElement#contains(Node)
   */
  public static boolean isAttached(Node node) {
    return JsElement.as(Document.get().getBody()).contains(node);
  }

  // sub-pixel element size/position methods (see https://developer.mozilla.org/en-US/docs/Web/API/CSS_Object_Model/Determining_the_dimensions_of_elements):

  /**
   * Gets an element's absolute left coordinate in the document's coordinate system, as a sub-pixel {@code double} value,
   * providing greater precision than GWT's {@link Element#getAbsoluteLeft()}.
   * @see #getBoundingClientRect(Element)
   * @see DOMRect#getAbsoluteLeft()
   */
  public static double getAbsoluteLeft(Element elem) {
    DOMRect rect = getBoundingClientRect(elem);
    return rect != null ? rect.getAbsoluteLeft() : elem.getAbsoluteLeft();
  }

  /**
   * Gets an element's absolute top coordinate in the document's coordinate system, as a sub-pixel {@code double} value,
   * providing greater precision than GWT's {@link Element#getAbsoluteTop()}.
   * @see #getBoundingClientRect(Element)
   * @see DOMRect#getAbsoluteTop()
   */
  public static double getAbsoluteTop(Element elem) {
    DOMRect rect = getBoundingClientRect(elem);
    return rect != null ? rect.getAbsoluteTop() : elem.getAbsoluteTop();
  }

  /**
   * Gets an element's absolute right coordinate in the document's coordinate system, as a sub-pixel {@code double} value,
   * providing greater precision than GWT's {@link Element#getAbsoluteRight()}.
   * @see #getBoundingClientRect(Element)
   * @see DOMRect#getAbsoluteRight()
   */
  public static double getAbsoluteRight(Element elem) {
    DOMRect rect = getBoundingClientRect(elem);
    return rect != null ? rect.getAbsoluteRight() : elem.getAbsoluteRight();
  }

  /**
   * Gets an element's absolute bottom coordinate in the document's coordinate system, as a sub-pixel {@code double} value,
   * providing greater precision than GWT's {@link Element#getAbsoluteBottom()}.
   * @see #getBoundingClientRect(Element)
   * @see DOMRect#getAbsoluteBottom()
   */
  public static double getAbsoluteBottom(Element elem) {
    DOMRect rect = getBoundingClientRect(elem);
    return rect != null ? rect.getAbsoluteBottom() : elem.getAbsoluteBottom();
  }

  /**
   * Gets an element's actual <i>rendering width</i>, as a sub-pixel {@code double} value,
   * providing greater precision than {@link Element#getOffsetWidth()}.
   * <p>
   * Note: this method obtains the width from the element's {@link #getBoundingClientRect(Element) DOMRect}, which
   * returns the element's <i>rendering width</i> (which accounts for any transforms applied to the element), whereas
   * {@code Element.offsetWidth} returns the <i>layout width</i>:
   * <blockquote cite="https://developer.mozilla.org/en-US/docs/Web/API/CSS_Object_Model/Determining_the_dimensions_of_elements">
   * Most of the time these are the same as width and height of Element.getBoundingClientRect(), when there aren't any
   * transforms applied to the element. In case of transforms, the offsetWidth and offsetHeight returns the element's
   * layout width and height, while getBoundingClientRect() returns the rendering width and height. As an example, if
   * the element has width: 100px; and transform: scale(0.5); the getBoundingClientRect() will return 50 as the width,
   * while offsetWidth will return 100. Another difference is that offsetWidth and offsetHeight round the values to
   * integers, while getBoundingClientRect() provides more precise decimal point values.
   * </blockquote>
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSS_Object_Model/Determining_the_dimensions_of_elements">
   *   Determining the dimensions of elements (MDN)</a>
   * @see #getBoundingClientRect(Element)
   * @see DOMRect#getWidth()
   */
  public static double getRenderedWidth(Element elem) {
    DOMRect rect = getBoundingClientRect(elem);
    return rect != null ? rect.getWidth() : elem.getOffsetWidth();
  }

  /**
   * Gets an element's actual <i>rendering height</i>, as a sub-pixel {@code double} value,
   * providing greater precision than {@link Element#getOffsetHeight()}.
   * <p>
   * Note: this method obtains the height from the element's {@link #getBoundingClientRect(Element) DOMRect}, which
   * returns the element's <i>rendering height</i> (which accounts for any transforms applied to the element), whereas
   * {@code Element.offsetHeight} returns the <i>layout height</i>:
   * <blockquote cite="https://developer.mozilla.org/en-US/docs/Web/API/CSS_Object_Model/Determining_the_dimensions_of_elements">
   * Most of the time these are the same as height and height of Element.getBoundingClientRect(), when there aren't any
   * transforms applied to the element. In case of transforms, the offsetHeight and offsetHeight returns the element's
   * layout height and height, while getBoundingClientRect() returns the rendering height and height. As an example, if
   * the element has height: 100px; and transform: scale(0.5); the getBoundingClientRect() will return 50 as the height,
   * while offsetHeight will return 100. Another difference is that offsetHeight and offsetHeight round the values to
   * integers, while getBoundingClientRect() provides more precise decimal point values.
   * </blockquote>
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSS_Object_Model/Determining_the_dimensions_of_elements">
   *   Determining the dimensions of elements (MDN)</a>
   * @see #getBoundingClientRect(Element)
   * @see DOMRect#getHeight()
   */
  public static double getRenderedHeight(Element elem) {
    DOMRect rect = getBoundingClientRect(elem);
    return rect != null ? rect.getHeight() : elem.getOffsetHeight();
  }

  /**
   * Computes the width of the element's <a href="https://drafts.csswg.org/css-box-3/#content-area">content area</a>
   * by subtracting horizontal padding from the element's {@code clientWidth} property.
   *
   * @return the computed sub-pixel width of the element's content area (excluding all margins, borders, and padding)
   * @see <a href="https://drafts.csswg.org/css-box-3/#content-area">W3C definition of "content area" in the CSS Box Model</a>
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/clientWidth">Element.clientWidth</a>
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSS_Object_Model/Determining_the_dimensions_of_elements">
   *   Determining the dimensions of elements (MDN)</a>
   */
  public static double getContentWidth(Element element) {
    return getContentWidth(element, getComputedStyle(element));
  }

  /**
   * Computes the height of the element's <a href="https://drafts.csswg.org/css-box-3/#content-area">content area</a>
   * by subtracting horizontal padding from the element's {@code clientHeight} property.
   *
   * @return the computed sub-pixel height of the element's content area (excluding all margins, borders, and padding)
   * @see <a href="https://drafts.csswg.org/css-box-3/#content-area">W3C definition of "content area" in the CSS Box Model</a>
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/clientHeight">Element.clientHeight</a>
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSS_Object_Model/Determining_the_dimensions_of_elements">
   *   Determining the dimensions of elements (MDN)</a>
   */
  public static double getContentHeight(Element element) {
    return getContentHeight(element, getComputedStyle(element));
  }

  /**
   * Computes the width of the element's <a href="https://drafts.csswg.org/css-box-3/#content-area">content area</a>
   * by subtracting horizontal padding from the element's {@code clientWidth} property.
   *
   * @param computedStyle a pre-computed result of {@code getComputedStyle(element)}
   * @return the computed sub-pixel width of the element's content area (excluding all margins, borders, and padding)
   * @see <a href="https://drafts.csswg.org/css-box-3/#content-area">W3C definition of "content area" in the CSS Box Model</a>
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/clientWidth">Element.clientWidth</a>
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSS_Object_Model/Determining_the_dimensions_of_elements">
   *   Determining the dimensions of elements (MDN)</a>
   */
  public static double getContentWidth(Element element, CSSStyleDeclaration computedStyle) {
    // subtract computed padding from clientWidth (see https://stackoverflow.com/a/47224153/1965404)
    return element.getPropertyDouble("clientWidth")
        - parsePx(computedStyle.getPaddingLeft())
        - parsePx(computedStyle.getPaddingRight());
  }

  /**
   * Computes the height of the element's <a href="https://drafts.csswg.org/css-box-3/#content-area">content area</a>
   * by subtracting horizontal padding from the element's {@code clientHeight} property.
   *
   * @param computedStyle a pre-computed result of {@code getComputedStyle(element)}
   * @return the computed sub-pixel height of the element's content area (excluding all margins, borders, and padding)
   * @see <a href="https://drafts.csswg.org/css-box-3/#content-area">W3C definition of "content area" in the CSS Box Model</a>
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/clientHeight">Element.clientHeight</a>
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSS_Object_Model/Determining_the_dimensions_of_elements">
   *   Determining the dimensions of elements (MDN)</a>
   */
  public static double getContentHeight(Element element, CSSStyleDeclaration computedStyle) {
    // subtract computed padding from clientHeight (see https://stackoverflow.com/a/47224153/1965404)
    return element.getPropertyDouble("clientHeight")
        - parsePx(computedStyle.getPaddingTop())
        - parsePx(computedStyle.getPaddingBottom());
  }

}
