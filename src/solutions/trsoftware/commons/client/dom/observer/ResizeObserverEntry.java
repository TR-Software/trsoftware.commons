package solutions.trsoftware.commons.client.dom.observer;

import com.google.gwt.dom.client.Element;
import solutions.trsoftware.commons.client.dom.JsElement;
import solutions.trsoftware.commons.shared.util.geometry.Rect;

/**
 * A {@code ResizeObserverEntry} object passed to a {@link ResizeObserver}'s callback function,
 * providing info about to the new dimensions of an observed {@code Element} or {@code SVGElement}.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Resize_Observer_API">ResizeObserver API (MDN)</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserverEntry">ResizeObserverEntry (MDN)</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/CSS_Object_Model/Determining_the_dimensions_of_elements">
 *   Determining the dimensions of elements (MDN)</a>
 * @author Alex
 * @since 2/27/2025
 */
public interface ResizeObserverEntry {

  /**
   * An array of objects containing the new border box size of the observed element when the callback is run.
   * <p>
   * The "border box" refers to the total rendered area of the element (including padding, borders, etc.),
   * and is equivalent to {@link JsElement#getBoundingClientRect() Element.getBoundingClientRect()}
   * <p>
   * This method returns an array to support elements that have multiple fragments, which occur in multi-column scenarios.
   * However the current version of the the spec doesn't yet implement this feature (as of 2025)
   * and this method currently returns an array with only 1 element.
   *
   * @see <a href="https://drafts.csswg.org/resize-observer/#dom-resizeobserverentry-borderboxsize">W3C Spec</a>
   */
  ResizeObserverSize[] getBorderBoxSize();

  /**
   * An array of objects containing the new content box size of the observed element when the callback is run.
   * <p>
   * The "content box" is the area of just the element's inner content (excluding padding, borders, etc.),
   * and is equivalent to {@link #getContentRect()}
   * <p>
   * This method returns an array to support elements that have multiple fragments, which occur in multi-column scenarios.
   * However the current version of the the spec doesn't yet implement this feature (as of 2025)
   * and this method currently returns an array with only 1 element.
   *
   * @see <a href="https://drafts.csswg.org/resize-observer/#dom-resizeobserverentry-contentboxsize">W3C Spec</a>
   */
  ResizeObserverSize[] getContentBoxSize();

  /**
   * An array of objects containing the new content box size in device pixels of the observed element when the callback is run.
   * <p>
   * This method returns an array to support elements that have multiple fragments, which occur in multi-column scenarios.
   * However the current version of the the spec doesn't yet implement this feature (as of 2025)
   * and this method currently returns an array with only 1 element.
   *
   * @see <a href="https://drafts.csswg.org/resize-observer/#dom-resizeobserverentry-devicepixelcontentboxsize">W3C Spec</a>
   */
  ResizeObserverSize[] getDevicePixelContentBoxSize();

  /**
   * A {@code DOMRectReadOnly} object containing the new size of the observed element when the callback is run.
   * Note that this is now a legacy property that is retained in the spec for backward-compatibility reasons only.
   * According to the W3C spec:
   * <blockquote cite="https://drafts.csswg.org/resize-observer/#content-rect">
   *   DOM content rect is a rect whose: <ul>
   *     <li>width is content width
   *     <li>height is content height
   *     <li>top is padding-top
   *     <li>left is padding-left
   *  </ul>
   * </blockquote>
   * This gives a rectangle whose top/left are offsets of the element's {@linkplain #getContentBoxSize() content box}
   * from its "padding box" and width/height are the dimensions of the content box (same as {@link #getContentBoxSize()}),
   * which is very different from {@link JsElement#getBoundingClientRect() Element.getBoundingClientRect()}, whose top/left
   * are offsets from the top of the viewport.
   *
   * @return a rectangle whose top/left are offsets of the element's "content box" from its "padding box"
   *   and width/height are the dimensions of the "content box"
   * @see <a href="https://drafts.csswg.org/resize-observer/#content-rect">W3C Spec</a>
   */
  Rect getContentRect();

  /**
   * A reference to the {@code Element} or {@code SVGElement} being observed.
   */
  Element getTarget();
}
