package solutions.trsoftware.commons.client.jso.observer;

import com.google.gwt.core.client.JavaScriptObject;
import solutions.trsoftware.commons.client.dom.observer.ResizeObserverSize;

/**
 * Overlay for a native {@code ResizeObserverSize} interface of the Resize Observer API,
 * which is used by the {@code ResizeObserverEntry} interface to access the box sizing properties of the element being observed.
 * 
 * @author Alex
 * @since 12/19/2024
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Resize_Observer_API">ResizeObserver API (MDN)</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserverSize">ResizeObserverSize (MDN)</a>
 */
public class NativeResizeObserverSize extends JavaScriptObject implements ResizeObserverSize {

  protected NativeResizeObserverSize() {
  }

  /**
   * The length of the observed element's border box in the block dimension.
   * For boxes with a horizontal writing-mode, this is the vertical dimension, or height;
   * if the writing-mode is vertical, this is the horizontal dimension, or width.
   */
  public native final double getBlockSize() /*-{
    return this.blockSize;
  }-*/;

  /**
   * The length of the observed element's border box in the inline dimension.
   * For boxes with a horizontal writing-mode, this is the horizontal dimension, or width;
   * if the writing-mode is vertical, this is the vertical dimension, or height.
   */
  public native final double getInlineSize() /*-{
    return this.inlineSize;
  }-*/;
  
}
