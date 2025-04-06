package solutions.trsoftware.commons.client.jso.observer;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import solutions.trsoftware.commons.client.dom.observer.ResizeObserver;

import java.util.function.BiConsumer;

/**
 * Overlay for a native {@code ResizeObserver}.
 * 
 * @author Alex
 * @since 12/19/2024
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Resize_Observer_API">ResizeObserver API (MDN)</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserver">ResizeObserver (MDN)</a>
 * @see NativeResizeObserverEntry
 */
public class NativeResizeObserver extends JavaScriptObject implements ResizeObserver {

  /**
   * Checks whether the browser supports the ResizeObserver API.
   * <p>
   * <b>Note:</b> use {@link ResizeObserver.Impl#create(Callback)} to automatically obtain an emulated implementation if
   * the native API is not supported.
   *
   * @see <a href="https://caniuse.com/?search=ResizeObserver">Browser compatibility (caniuse.com)</a>
   * @return {@code true} iff {@code window} contains a {@code ResizeObserver} function
   */
  public native static boolean isSupported() /*-{
    return 'ResizeObserver' in $wnd;
  }-*/;

  protected NativeResizeObserver() {
  }

  /**
   * Creates a new ResizeObserver object, which can be used to report changes to the content or border box of an {@code Element}
   * or the bounding box of an {@code SVGElement}.
   * <p>
   * The {@code callback} is function called whenever an observed resize occurs, with two parameters:
   * <ul>
   *   <li>{@code entries}: An array of {@code ResizeObserverEntry} objects that can be used to access the new dimensions
   *       of each observed element after each change.
   *       This array contains an entry for each element passed to {@link #observe(Element)}.
   *   <li>{@code observer}: A reference to the {@code ResizeObserver} itself,
   *       which could be used for example to automatically unobserve the observer when a certain condition is reached.
   * </ul>
   * <p>
   * <b>Note:</b> use {@link ResizeObserver.Impl#create(Callback)} to automatically obtain an emulated implementation if
   * the native API is not supported.
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserver/ResizeObserver">ResizeObserver constructor (MDN)</a>
   * @deprecated prefer {@link ResizeObserver.Impl#create(Callback)} to automatically obtain an emulated implementation
   *   if the native API is not supported
   */
  public static native NativeResizeObserver create(BiConsumer<JsArray<NativeResizeObserverEntry>, NativeResizeObserver> callback) /*-{
    return new ResizeObserver(
      $entry(function (entries, observer) {
        callback.@java.util.function.BiConsumer::accept(*)(entries, observer);
      })
    );
  }-*/;

  /**
   * Starts observing the specified {@code Element} or {@code SVGElement}.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserver/observe">MDN</a>
   */
  @Override
  public final native void observe(Element target) /*-{
    this.observe(target);
  }-*/;

  /**
   * Stops observing the specified {@code Element} or {@code SVGElement}.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ResizeUnobserver/unobserve">MDN</a>
   */
  @Override
  public final native void unobserve(Element target) /*-{
    this.unobserve(target);
  }-*/;

  /**
   * Stops observing all observed {@code Element} or {@code SVGElement} targets.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ResizeDisconnectr/disconnect">MDN</a>
   */
  @Override
  public final native void disconnect() /*-{
    this.disconnect();
  }-*/;
}
