package solutions.trsoftware.commons.client.dom.observer;

import com.google.gwt.dom.client.Element;
import solutions.trsoftware.commons.client.jso.observer.NativeResizeObserver;

import java.util.function.BiConsumer;

/**
 * A {@code ResizeObserver} reports changes to the dimensions of an {@code Element}'s content or border box,
 * or the bounding box of an {@code SVGElement}.
 * <p>
 * Use {@link Impl#create} to obtain an appropriate instance of this class:
 *   a native instance if supported by the host browser or
 *   an {@linkplain EmulatedResizeObserver emulated instance} otherwise.
 *
 * @author Alex
 * @since 2/26/2025
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Resize_Observer_API">ResizeObserver API (MDN)</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserver">ResizeObserver (MDN)</a>
 * @see NativeResizeObserver
 * @see EmulatedResizeObserver
 */
public interface ResizeObserver {
  /**
   * Starts observing the specified {@code Element} or {@code SVGElement}.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserver/observe">MDN</a>
   */
  void observe(Element target);

  /**
   * Stops observing the specified {@code Element} or {@code SVGElement}.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ResizeUnobserver/unobserve">MDN</a>
   */
  void unobserve(Element target);

  /**
   * Stops observing all observed {@code Element} or {@code SVGElement} targets.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ResizeDisconnectr/disconnect">MDN</a>
   */
  void disconnect();

  /**
   * Wrapper for the {@link #create(Callback)} method.
   */
  class Impl {
    /*
    NOTE: originally had this as a static method directly on ResizeObserver, but that doesn't work with GWT's legacy devMode,
    which throws NoSuchMethodError when attempting to invoke a static method on an interface
     */

    /**
     * Creates an implementation-specific instance of {@code ResizeObserver}:
     * a native instance if the Resize Observer API is supported by the current browser,
     * or an {@linkplain EmulatedResizeObserver emulated instance} otherwise.
     *
     * @param callback a function called whenever an observed resize occurs, with two parameters:
     *     <ul>
     *       <li>{@code entries}: An array of {@link ResizeObserverEntry} objects that can be used to access the new dimensions
     *           of each observed element after each change.
     *           This array contains an entry for each element passed to {@link #observe(Element)}.
     *       <li>{@code observer}: A reference to the {@link ResizeObserver} itself,
     *           which could be used for example to automatically unobserve the observer when a certain condition is reached.
     *     </ul>
     */
    public static ResizeObserver create(Callback callback) {
      if (NativeResizeObserver.isSupported())
        return NativeResizeObserver.create((nativeEntries, resizeObserver) ->
            callback.accept(ResizeObserverEntryAdapter.toEntryArray(nativeEntries), resizeObserver));
      else
        return new EmulatedResizeObserver(callback);
    }
  }

  /**
   * A callback function passed to a {@code ResizeObserver} constructor; invoked whenever an observed resize occurs,
   * with two parameters:
   * <ul>
   * <li>{@code entries}: An array of {@link ResizeObserverEntry} objects that can be used to access the new dimensions
   *     of each observed element after each change.
   *     This array contains an entry for each element passed to {@link #observe(Element)}.
   * <li>{@code observer}: A reference to the {@link ResizeObserver} itself,
   *     which could be used for example to automatically unobserve the observer when a certain condition is reached.
   * </ul>
   */
  interface Callback extends BiConsumer<ResizeObserverEntry[], ResizeObserver> {
    /**
     * Invoked when a {@link ResizeObserver} detects a change to an observed element's size.
     *
     * @param resizeObserverEntries An array of {@link ResizeObserverEntry} objects that can be used to access the
     *     new dimensions of each observed element after each change.
     * @param resizeObserver A reference to the {@link ResizeObserver} itself, which could be used for example
     *     to automatically unobserve the observer when a certain condition is reached.
     */
    @Override
    void accept(ResizeObserverEntry[] resizeObserverEntries, ResizeObserver resizeObserver);
  }

}
