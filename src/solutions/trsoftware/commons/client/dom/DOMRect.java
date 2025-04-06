package solutions.trsoftware.commons.client.dom;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import solutions.trsoftware.commons.shared.util.geometry.Rect;

/**
 * JSNI overlay for the native {@code DOMRect} and {@code DOMRectReadOnly} interfaces,
 * which describes the size and position of a rectangle.
 *
 * @see JsElement#getBoundingClientRect()
 * @see DomUtils#getBoundingClientRect(Element)
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/DOMRect">MDN Reference</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/DOMRectReadOnly">MDN Reference</a>
 * @see <a href="https://caniuse.com/?search=DOMRect">Browser Compatibility</a>
 * @author Alex
 * @since 12/5/2024
 */
public class DOMRect extends JavaScriptObject implements Rect {

  protected DOMRect() {
  }

  /**
   * Returns the x coordinate of the DOMRect's origin (typically the top-left corner of the rectangle)
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/DOMRect/x"><tt>DOMRect.x</tt></a>
   */
  public native final double getX() /*-{
    return this.x !== undefined ? this.x : this.left;  // Chrome<61 doesn't support x,y properties (see https://caniuse.com/?search=DOMRect)
  }-*/;

  /**
   * Returns the y coordinate of the DOMRect's origin (typically the top-left corner of the rectangle)
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/DOMRect/y"><tt>DOMRect.y</tt></a>
   */
  public native final double getY() /*-{
    return this.y !== undefined ? this.y : this.top;  // Chrome<61 doesn't support x,y properties (see https://caniuse.com/?search=DOMRect)
  }-*/;

  /**
   * Returns the width of the DOMRect
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/DOMRect/width"><tt>DOMRect.width</tt></a>
   */
  public native final double getWidth() /*-{
    return this.width;
  }-*/;

  /**
   * Returns the height of the DOMRect
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/DOMRect/height"><tt>DOMRect.height</tt></a>
   */
  public native final double getHeight() /*-{
    return this.height;
  }-*/;

  /**
   * Returns the top coordinate value of the DOMRect (has the same value as y, or y + height if height is negative).
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/DOMRectReadOnly/top"><tt>DOMRect.top</tt></a>
   */
  public native final double getTop() /*-{
    return this.top;
  }-*/;

  /**
   * Returns the right coordinate value of the DOMRect (has the same value as x + width, or x if width is negative).
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/DOMRectReadOnly/right"><tt>DOMRect.right</tt></a>
   */
  public native final double getRight() /*-{
    return this.right;
  }-*/;

  /**
   * Returns the bottom coordinate value of the DOMRect (has the same value as y + height, or y if height is negative).
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/DOMRectReadOnly/bottom"><tt>DOMRect.bottom</tt></a>
   */
  public native final double getBottom() /*-{
    return this.bottom;
  }-*/;

  /**
   * Returns the left coordinate value of the DOMRect (has the same value as x, or x + width if width is negative).
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/DOMRectReadOnly/left"><tt>DOMRect.left</tt></a>
   */
  public native final double getLeft() /*-{
    return this.left;
  }-*/;

  // absolute coords (offset by Window scroll position)

  /**
   * @return {@link #getX() x} offset by {@link Window#getScrollLeft()}
   */
  public final double getAbsoluteX() {
    return getX() + Window.getScrollLeft();
  }

  /**
   * @return {@link #getLeft() left} offset by {@link Window#getScrollLeft()}
   */
  public final double getAbsoluteLeft() {
    return getLeft() + Window.getScrollLeft();
  }

  /**
   * @return {@link #getRight() right} offset by {@link Window#getScrollLeft()}
   */
  public final double getAbsoluteRight() {
    return getRight() + Window.getScrollLeft();
  }

  /**
   * @return {@link #getY() y} offset by {@link Window#getScrollTop()}
   */
  public final double getAbsoluteY() {
    return getY() + Window.getScrollTop();
  }

  /**
   * @return {@link #getTop() top} offset by {@link Window#getScrollTop()}
   */
  public final double getAbsoluteTop() {
    return getTop() + Window.getScrollTop();
  }

  /**
   * @return {@link #getBottom() bottom} offset by {@link Window#getScrollTop()}
   */
  public final double getAbsoluteBottom() {
    return getBottom() + Window.getScrollTop();
  }

  /**
   * @return {@code true} iff the given rects have the same values for
   *   {@code x}, {@code y}, {@code width}, and {@code height}
   */
  public static native boolean equals(DOMRect a, DOMRect b) /*-{
    return a.x === b.x
        && a.y === b.y
        && a.width === b.width
        && a.height === b.height;
  }-*/;
}
