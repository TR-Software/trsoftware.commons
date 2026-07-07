package solutions.trsoftware.commons.client.event.animation;

import com.google.common.collect.ImmutableMap;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.web.bindery.event.shared.HandlerRegistration;
import solutions.trsoftware.commons.client.css.CSSStyleDeclaration;
import solutions.trsoftware.commons.client.event.NativeEvents;
import solutions.trsoftware.commons.shared.util.LazyReference;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

/**
 * Polyfill for adding native {@link AnimationEvent} handlers and accessing CSS animation properties,
 * with legacy browser support.
 *
 * @author Alex
 * @since 6/24/2026
 */
public class CssAnimations {
  /* @see:
     - https://www.google.com/search?q=animationend+event+polyfill&oq=animationend+event+poly&aqs=chrome.1.69i57j33i160l5.5686j0j7&sourceid=chrome&ie=UTF-8
     - https://gist.github.com/ionurboz/43ebf7b9e919f07651b209c5cf2dc5fc
     - https://www.google.com/search?q=animationend+listener+when+element+has+multiple+animations&oq=animationend+listener+when+element+has+multiple+animations&aqs=chrome..69i57j33i160l4.12696j0j7&sourceid=chrome&ie=UTF-8
     - https://developer.mozilla.org/en-US/docs/Glossary/Vendor_Prefix
   */

  /**
   * The CSS animation event types:
   * <ul>
   *   <li><a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/animationstart_event">animationstart</a>
   *   <li><a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/animationend_event">animationend</a>
   *   <li><a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/animationcancel_event">animationcancel</a>
   *   <li><a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/animationiteration_event">animationiteration</a>
   * </ul>
   */
  public enum AnimationEventType {
    AnimationStart,
    AnimationEnd,
    /** <b>Note:</b> this event has limited browser support (specifically, not supported on Chrome 35) */
    AnimationCancel,
    AnimationIteration;
    // NOTE: do not rename these constants, the camel-casing is important for getSupportedEventName
  }

  // TODO: maybe extract the vendor prefix selection to a separate util class
  private static final Map<String, String> vendorPrefixToEventPrefix = ImmutableMap.of(
      "", "",
      "webkit", "webkit",
      "Moz", "",
      "O", "o",
      "ms", "MS"
  );

  /**
   * The <a href="https://developer.mozilla.org/en-US/docs/Glossary/Vendor_Prefix">vendor prefix</a>
   * (if any) required by the current browser for CSS animations,
   * if it was an experimental feature at the time the browser was released.
   * <p>
   * Empty string if no prefix is needed (i.e. the browser is fully compliant with modern standards),
   * or {@code null} if CSS animations are not supported at all by the current browser.
   * @see #getCurrentVendorPrefix()
   */
  private static final LazyReference<String> currentVendorPrefix = LazyReference.fromSupplier(
      CssAnimations::getCurrentVendorPrefix);

  /**
   * Adds a listener for the specified animation event type.
   *
   * @param eventType the animation event type (e.g. "animationstart" or  "animationend")
   * @param target the element to which the listener will be added
   * @param useCapture if {@code true}, events of this type will be dispatched to the registered listener
   *   <em>before</em> being dispatched to any {@code EventTarget} beneath it in the DOM tree.
   * @param listener to be invoked for the specified event
   * @return memento that can remove this event listener from the element,
   *   or {@code null} if the listener wasn't added because the current browser doesn't support CSS animations
   *
   * @see #isAnimationSupported()
   * @see NativeEvents#addNativeEventListener
   */
  @Nullable
  public static HandlerRegistration addAnimationListener(AnimationEventType eventType, Element target, boolean useCapture, AnimationEvent.Listener listener) {
    Optional<String> supportedEventName = getSupportedEventName(eventType);
    return supportedEventName
        .map(eventName -> NativeEvents.addNativeEventListener(target, eventName, useCapture, listener))
        .orElse(null);
  }

  /**
   * @return {@code true} if the current browser supports CSS animations
   */
  public static boolean isAnimationSupported() {
    return currentVendorPrefix.get() != null;
  }

  /**
   * Translates the specified animation event type to the corresponding event type string for the current browser
   * based on {@link #currentVendorPrefix}.
   *
   * @return the event type string for {@code addEventListener},
   *   or empty optional if the current browser doesn't support CSS animations
   */
  private static Optional<String> getSupportedEventName(AnimationEventType eventType) {
    // see https://www.google.com/search?q=animationend+event+polyfill&oq=animationend+event+poly&aqs=chrome.1.69i57j33i160l5.5686j0j7&sourceid=chrome&ie=UTF-8
    String vendorPrefix = currentVendorPrefix.get();
    if (vendorPrefix == null)
      return Optional.empty();
    String eventName = eventType.name();  // a CamelCase name
    String eventPrefix = vendorPrefixToEventPrefix.get(vendorPrefix);
    return Optional.of(eventPrefix.isEmpty() ? eventName.toLowerCase() : eventPrefix + eventName);
  }

  /**
   * Determines the <a href="https://developer.mozilla.org/en-US/docs/Glossary/Vendor_Prefix">vendor prefix</a>
   * (if any) required by the current browser for Web APIs and CSS properties that were experimental at the time
   * the browser was released.
   *
   * @return the required prefix (empty string if the browser is fully compliant with modern standards),
   *   or {@code null} if CSS animations are not supported at all by the current browser
   */
  @Nullable
  private static String getCurrentVendorPrefix() {
    // see https://www.google.com/search?q=animationend+event+polyfill&oq=animationend+event+poly&aqs=chrome.1.69i57j33i160l5.5686j0j7&sourceid=chrome&ie=UTF-8
    CSSStyleDeclaration style = Document.get().getBody().getStyle().cast();
    for (String prefix : vendorPrefixToEventPrefix.keySet()) {
      String checkProp = prefix.isEmpty() ? "animation" : prefix + "Animation";
      String propValue = style.getProperty(checkProp);
      if (propValue != null) { // !== undefined
        return prefix;
      }
    }
    return null;  // animation property not supported by this browser
  }


  /**
   * The standard CSS animation properties, providing a {@link #getValue(CSSStyleDeclaration)} method
   * to access the property value (automatically prepending a vendor prefix if required by the current browser).
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/Guides/Animations#properties">MDN reference</a>
   * @see #getCurrentVendorPrefix()
   */
  public enum AnimationProperty {
    /** The "animation" shorthand CSS property */
    Animation("animation"),
    // constituent properties of the shorthand CSS "animation" property:
    Name("animation-name"),
    Duration("animation-duration"),
    TimingFunction("animation-timing-function"),
    Delay("animation-delay"),
    Direction("animation-direction"),
    IterationCount("animation-iteration-count"),
    FillMode("animation-fill-mode"),
    PlayState("animation-play-state"),
    Timeline("animation-timeline"),
    // other animation properties:
    Composition("animation-composition"),
    ;

    /** The standard CSS name of this property */
    private final String cssName;

    AnimationProperty(String cssName) {
      this.cssName = cssName;
    }

    public String getCssName() {
      return cssName;
    }

    /**
     * Retrieves the value of this property from the given style declaration,
     * (potentially using a vendor prefix if required by the current browser).
     *
     * @return the value of this property in the specified style declaration
     */
    public String getValue(CSSStyleDeclaration style) {
      return style.getPropertyValue(maybePrefixCssName());
    }

    private String maybePrefixCssName() {
      String vendorPrefix = currentVendorPrefix.get();
      if (StringUtils.isBlank(vendorPrefix))
        return cssName;
      else {
        String prefix = "-" + vendorPrefix.toLowerCase() + "-";  // TODO: maybe cache this value
        return prefix + cssName;
      }
    }
  }
}
