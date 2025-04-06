package solutions.trsoftware.commons.client.jso.observer;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import solutions.trsoftware.commons.client.dom.DOMRect;
import solutions.trsoftware.commons.client.dom.JsElement;

/**
 * Overlay for a native {@code ResizeObserverEntry} object, which is passed to a {@code ResizeObserver}'s callback function,
 * providing info about to the new dimensions of an observed {@code Element} or {@code SVGElement}.
 * 
 * @author Alex
 * @since 12/19/2024
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Resize_Observer_API">ResizeObserver API (MDN)</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserverEntry">ResizeObserverEntry (MDN)</a>
 * @see NativeResizeObserver
 */
public class NativeResizeObserverEntry extends JavaScriptObject {

  protected NativeResizeObserverEntry() {
  }

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
  public native final JsArray<NativeResizeObserverSize> getBorderBoxSize() /*-{
    return this.borderBoxSize;
  }-*/;

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
  public native final JsArray<NativeResizeObserverSize> getContentBoxSize() /*-{
    return this.contentBoxSize;
  }-*/;

  /**
   * An array of objects containing the new content box size in device pixels of the observed element when the callback is run.
   * <p>
   * This method returns an array to support elements that have multiple fragments, which occur in multi-column scenarios.
   * However the current version of the the spec doesn't yet implement this feature (as of 2025)
   * and this method currently returns an array with only 1 element.
   *
   * @see <a href="https://drafts.csswg.org/resize-observer/#dom-resizeobserverentry-devicepixelcontentboxsize">W3C Spec</a>
   */
  public native final JsArray<NativeResizeObserverSize> getDevicePixelContentBoxSize() /*-{
    return this.devicePixelContentBoxSize;
  }-*/;

  /**
   * A {@code DOMRectReadOnly} object containing the new size of the observed element when the callback is run.
   * Note that this is now a legacy property that is retained in the spec for backward-compatibility reasons only.
   */
  public native final DOMRect getContentRect() /*-{
    return this.contentRect;
  }-*/;

  /**
   * A reference to the {@code Element} or {@code SVGElement} being observed.
   */
  public native final JsElement getTarget() /*-{
    return this.target;
  }-*/;

}
