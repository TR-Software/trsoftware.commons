package solutions.trsoftware.commons.client.dom.observer;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import solutions.trsoftware.commons.client.jso.observer.NativeResizeObserverEntry;
import solutions.trsoftware.commons.client.jso.observer.NativeResizeObserverSize;
import solutions.trsoftware.commons.shared.util.geometry.Rect;

/**
 * Wrapper for a native {@link NativeResizeObserverEntry}
 *
 * @author Alex
 * @since 2/27/2025
 */
class ResizeObserverEntryAdapter implements ResizeObserverEntry {

  private NativeResizeObserverEntry entry;

  public ResizeObserverEntryAdapter(NativeResizeObserverEntry entry) {
    this.entry = entry;
  }

  @Override
  public ResizeObserverSize[] getBorderBoxSize() {
    return toSizeArray(entry.getBorderBoxSize());
  }

  @Override
  public ResizeObserverSize[] getContentBoxSize() {
    return toSizeArray(entry.getContentBoxSize());
  }

  @Override
  public ResizeObserverSize[] getDevicePixelContentBoxSize() {
    return toSizeArray(entry.getDevicePixelContentBoxSize());
  }

  @Override
  public Rect getContentRect() {
    return entry.getContentRect();
  }

  @Override
  public Element getTarget() {
    return entry.getTarget();
  }

  public static ResizeObserverSize[] toSizeArray(JsArray<NativeResizeObserverSize> jsArray) {
    ResizeObserverSize[] ret = new ResizeObserverSize[jsArray.length()];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = jsArray.get(i);
    }
    return ret;
  }

  public static ResizeObserverEntry[] toEntryArray(JsArray<NativeResizeObserverEntry> jsArray) {
    ResizeObserverEntry[] ret = new ResizeObserverEntry[jsArray.length()];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = new ResizeObserverEntryAdapter(jsArray.get(i));
    }
    return ret;
  }
}
