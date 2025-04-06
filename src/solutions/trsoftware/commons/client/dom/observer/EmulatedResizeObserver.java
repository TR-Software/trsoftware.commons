package solutions.trsoftware.commons.client.dom.observer;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.dom.client.Element;
import solutions.trsoftware.commons.client.css.CSSStyleDeclaration;
import solutions.trsoftware.commons.client.dom.DOMRect;
import solutions.trsoftware.commons.client.dom.DomUtils;
import solutions.trsoftware.commons.client.jso.JsWindow;
import solutions.trsoftware.commons.shared.util.MathUtils;
import solutions.trsoftware.commons.shared.util.geometry.Rect;
import solutions.trsoftware.commons.shared.util.geometry.Rectangle2D;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static solutions.trsoftware.commons.client.css.CSSStyleDeclaration.parsePx;

/**
 * Emulates the functionality of the native {@code ResizeObserver} API for browsers that don't support it.
 * Use {@link ResizeObserver.Impl#create(Callback)} to obtain an instance of this class.
 *
 * @author Alex
 * @since 2/26/2025
 */
public class EmulatedResizeObserver implements ResizeObserver {
  // Note: based on the polyfill from https://codepen.io/dgca/pen/WoJoNB

  private final BiConsumer<ResizeObserverEntry[], ResizeObserver> callback;  // TODO: maybe change field type to ResizeObserver.Callback
  private final Map<Element, ObservableEntry> observables = new LinkedHashMap<>();

  /**
   * Runs the size checks using {@code requestAnimationFrame}
   */
  private final AnimationScheduler animationScheduler;
  /**
   * The ID of the pending animation request.
   */
  private AnimationScheduler.AnimationHandle animationHandle;


  /**
   * Package-private constructor.
   * Use {@link ResizeObserver.Impl#create(Callback)} to obtain a new {@link ResizeObserver} instance.
   */
  EmulatedResizeObserver(BiConsumer<ResizeObserverEntry[], ResizeObserver> callback) {
    // TODO: maybe make constructor package-private to force usage of the ResizeObserver.Impl.create factory method
    this.callback = callback;
    animationScheduler = AnimationScheduler.get();  // TODO: maybe allow passing StubAnimationScheduler for testing
  }

  @Override
  public void observe(Element target) {
    observables.computeIfAbsent(target, ObservableEntry::new);
    scheduleNextCheck();
    // TODO: maybe return a HandlerRegistration?
  }

  @Override
  public void unobserve(Element target) {
    observables.remove(target);
    if (observables.isEmpty())
      stopChecking();  // cancel animationHandle if observables becomes empty
  }

  @Override
  public void disconnect() {
    observables.clear();
    stopChecking();
  }

  /**
   * Will be executed repeatedly via {@code requestAnimationFrame} to
   * check for changes to the size of any {@link #observables} and
   * invoke the {@link #callback} if needed.
   */
  private void check() {
    if (!observables.isEmpty()) {
      ResizeObserverEntry[] changedEntries = observables.values().stream()
          .filter(ObservableEntry::updateSize).toArray(ResizeObserverEntry[]::new);
      if (changedEntries.length > 0)
        callback.accept(changedEntries, this);
    }
    scheduleNextCheck();

  }

  private void scheduleNextCheck() {
    animationHandle = animationScheduler.requestAnimationFrame(timestamp -> check());
  }

  private void stopChecking() {
    if (animationHandle != null) {
      animationHandle.cancel();
      animationHandle = null;
    }
  }


  static class ObservableEntry implements ResizeObserverEntry {
    // TODO: maybe don't implement ResizeObserverEntry directly - replace with inner class
    private final Element element;
    private double width, height;
    private CSSStyleDeclaration computedStyle;  // cached value of window.getComputedStyle(element)

    // see https://developer.mozilla.org/en-US/docs/Web/API/CSS_Object_Model/Determining_the_dimensions_of_elements

    ObservableEntry(Element element) {
      this.element = element;
      updateSize();
    }

    /**
     * Updates the current dimensions of {@link #element} from {@code getBoundingClientRect} and
     * returns {@code true} if changed.
     * @return {@code true} if either the width or height changed
     */
    boolean updateSize() {
      DOMRect rect = DomUtils.getBoundingClientRect(element);
      double width, height;
      if (rect != null) {
        width = rect.getWidth();
        height = rect.getHeight();
      } else {
        // getBoundingClientRect not supported (must be very old browser), use the less accurate offset[Width|Height]
        width = element.getPropertyDouble("offsetWidth");
        height = element.getPropertyDouble("offsetHeight");
      }
      boolean changed = !equal(this.width, width) || !equal(this.height, height);
      if (changed) {
        this.width = width;
        this.height = height;
        // update the cached computedStyle value (for use by the ResizeObserverEntry methods)
        computedStyle = JsWindow.get().getComputedStyle(element);
      }
      return changed;
    }

    boolean isVertical() {
      CSSStyleDeclaration computedStyle = getComputedStyle();
      String writingMode = computedStyle.getPropertyValue("writing-mode");
      return writingMode.startsWith("vertical");
    }

    private CSSStyleDeclaration getComputedStyle() {
      if (computedStyle == null)
        return computedStyle = JsWindow.get().getComputedStyle(element);  // computed style is a "live" object that can be cached
      return computedStyle;
    }

    double getContentWidth() {
      return DomUtils.getContentWidth(element, getComputedStyle());
    }

    double getContentHeight() {
      return DomUtils.getContentHeight(element, getComputedStyle());
    }

    // ResizeObserverEntry interface methods:

    @Override
    public ResizeObserverSize[] getBorderBoxSize() {
      return singletonSizeArray(width, height, isVertical());
    }

    @Override
    public ResizeObserverSize[] getContentBoxSize() {
      return singletonSizeArray(getContentWidth(), getContentHeight(), isVertical());
    }

    @Override
    public ResizeObserverSize[] getDevicePixelContentBoxSize() {
      /* From W3C spec (https://drafts.csswg.org/resize-observer/#dom-resizeobserverboxoptions-device-pixel-content-box):
      The device-pixel-content-box can be approximated by multiplying devicePixelRatio by the content-box size. However, due to browser-specific subpixel snapping behavior, authors cannot determine the correct way to round this scaled content-box size. How a UA computes the device pixel box for an element is implementation-dependent. One possible implementation could be to multiply the box size and position by the device pixel ratio, then round both the resulting floating-point size and position of the box to integer values, in a way that maximizes the quality of the rendered output.
       */
      double devicePixelRatio = JsWindow.get().getDevicePixelRatio();
      return singletonSizeArray(
          getContentWidth() * devicePixelRatio,
          getContentHeight() * devicePixelRatio, isVertical());
    }

    @Override
    public Rect getContentRect() {
      /* see https://drafts.csswg.org/resize-observer/#content-rect:
         "DOM content rect is a rect whose:
            width is content width
            height is content height
            top is padding top (literally CSS padding-top)
            left is padding left (literally CSS padding-left)
       */
      CSSStyleDeclaration cs = getComputedStyle();
      double top = parsePx(cs.getPaddingTop());
      double left = parsePx(cs.getPaddingLeft());
      return new Rectangle2D(left, top, getContentWidth(), getContentHeight());
    }

    @Override
    public Element getTarget() {
      return element;
    }

    private static ResizeObserverSize[] singletonSizeArray(double width, double height, boolean vertical) {
      return new ResizeObserverSize[]{
          new ResizeObserverSizeImpl(width, height, vertical)
      };
    }
  }

  /**
   * @return {@code true} if the given doubles are equal to within a margin of {@value MathUtils#EPSILON}.
   */
  private static boolean equal(double a, double b) {
    return MathUtils.equal(b, a, MathUtils.EPSILON);
  }

}
