package solutions.trsoftware.commons.client.event.animation;

import com.google.common.collect.ImmutableSet;
import com.google.gwt.dom.client.Element;
import solutions.trsoftware.commons.client.debug.Debug;
import solutions.trsoftware.commons.client.dom.DomUtils;
import solutions.trsoftware.commons.client.event.MultiHandlerRegistration;
import solutions.trsoftware.commons.client.event.animation.CssAnimations.AnimationProperty;
import solutions.trsoftware.commons.client.jso.JsConsole;
import solutions.trsoftware.commons.client.jso.JsMixedArray;
import solutions.trsoftware.commons.client.widgets.WidgetDecorator;
import solutions.trsoftware.commons.shared.util.SetUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

import static solutions.trsoftware.commons.client.event.animation.CssAnimations.AnimationEventType.*;
import static solutions.trsoftware.commons.client.event.animation.CssAnimations.addAnimationListener;

/**
 * Uses {@link AnimationEvent} listeners to remove an animated CSS class after its animations are finished.
 *
 * @author Alex
 * @since 6/25/2026
 */
public class CssAnimationHelper {

  private final Element element;
  private final String styleName;
  private MultiHandlerRegistration handlerRegistrations;
  private Set<String> animationsToTrack;
  /**
   * Subset of {@link #animationsToTrack} that have started but not yet finished
   */
  private final LinkedHashSet<String> pendingAnimations = new LinkedHashSet<>();
  // TODO: do we actually need 2 different sets (pendingAnimations in addition to animationsToTrack)?


  public CssAnimationHelper(Element element, String styleName) {
    this.element = element;
    this.styleName = styleName;
  }

  /**
   * Applies a CSS style that contains animations to the specified element and forces layout reflow
   * to ensure that the animation will be restarted if the element already has the specified class name.
   * <p>
   * If the {@code removeOnAnimationEnd} arg is {@code true}, the style will be removed after the animations have finished
   * (using "animationend" and "animationcancel" event listeners on the element).
   * Otherwise, this method is equivalent to {@link WidgetDecorator#reapplyStyleName(Element, String)}.
   *
   * @param element the element to be animated using the specified {@code styleName}
   * @param styleName CSS class that contains one or more animations
   * @param removeOnAnimationEnd whether to remove the CSS class after animations have completed
   * @see WidgetDecorator#reapplyStyleName(Element, String)
   */
  public static void applyAnimatedStyle(Element element, String styleName, boolean removeOnAnimationEnd) {
    if (removeOnAnimationEnd && CssAnimations.isAnimationSupported())
      new CssAnimationHelper(element, styleName).applyStyle();
    else
      WidgetDecorator.reapplyStyleName(element, styleName);
  }

  void applyStyle() {
    // see WidgetDecorator.reapplyStyleName for explanation of the removeClassName/getOffsetHeight/addClassName paradigm
    element.removeClassName(styleName);
    Set<String> animationsBefore = getAnimationNames();
    handlerRegistrations = new MultiHandlerRegistration(
        addAnimationListener(AnimationStart, element, false, this::onAnimationStart),
        addAnimationListener(AnimationEnd, element, false, this::onAnimationEnd),
        // Note: the "animationcancel" event has limited browser support (specifically, not supported on Chrome 35)
        addAnimationListener(AnimationCancel, element, false, this::onAnimationEnd)
    );
    /*
     Note: checking element's computedStyle["animation-name"] list before & after adding this style
      to make sure we're tracking all animations defined after adding this style
      (maybe special case if animation-iteration-count is infinite for any of the animations)
    */
    // simply accessing element.offsetHeight triggers layout reflow, which removes the old animation so it can be restarted on the next call to addClassName
    element.getOffsetHeight();
    element.addClassName(styleName);
    Set<String> animationsAfter = getAnimationNames();
    animationsToTrack = SetUtils.difference(animationsAfter, animationsBefore);

  }

  private Set<String> getAnimationNames() {
    String names = AnimationProperty.Name.getValue(DomUtils.getComputedStyle(element));
    if (StringUtils.isBlank(names))
      return ImmutableSet.of();
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    StringUtils.splitAndTrim(names, ",", builder::add);
    return builder.build();
  }

  private void onAnimationStart(AnimationEvent event) {
    String animationName = event.getAnimationName();
    if (animationsToTrack.contains(animationName)) {
      pendingAnimations.add(animationName);
    }
    if (Debug.ENABLED) {
      JsConsole.get().logVarArgs(JsConsole.Level.DEBUG, JsMixedArray.create()
          .add("CssAnimationHelper.onAnimationStart").add(event));
    }
  }
  
  private void onAnimationEnd(AnimationEvent event) {
    String animationName = event.getAnimationName();
    if (animationsToTrack.contains(animationName)) {
      if (pendingAnimations.remove(animationName)) {
        animationsToTrack.remove(animationName);
      }
    }
    if (animationsToTrack.isEmpty()) {
      // all animations have completed; remove the class name and our handlers
      element.removeClassName(styleName);
      handlerRegistrations.removeHandler();
    }
    if (Debug.ENABLED) {
      JsConsole.get().logVarArgs(JsConsole.Level.DEBUG, JsMixedArray.create()
          .add("CssAnimationHelper.onAnimationEnd").add(event));
    }
  }
}
