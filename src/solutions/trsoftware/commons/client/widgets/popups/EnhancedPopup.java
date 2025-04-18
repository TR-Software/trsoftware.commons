/*
 * Copyright 2021 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.commons.client.widgets.popups;

import com.google.common.base.Preconditions;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.animations.OpacityTweenAnimation;
import solutions.trsoftware.commons.client.animations.ShakePopupAnimation;
import solutions.trsoftware.commons.client.dom.DomUtils;
import solutions.trsoftware.commons.client.jso.JsDocument;
import solutions.trsoftware.commons.client.jso.JsWindow;
import solutions.trsoftware.commons.client.util.geometry.Alignment;
import solutions.trsoftware.commons.client.util.geometry.RelativePosition;
import solutions.trsoftware.commons.client.util.geometry.WindowGeometry;
import solutions.trsoftware.commons.client.widgets.HasFocusTarget;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nullable;

/**
 * Extends {@link PopupPanel} with additional functionality and bug fixes.
 *
 * @author Alex
 * @since Apr 16, 2008
 */
public class EnhancedPopup extends PopupPanel implements HasFocusTarget {

  private ShakePopupAnimation shakingAnimation;
  private boolean closerControlsEnabled = true;

  public EnhancedPopup() {
    super();
  }

  public EnhancedPopup(boolean autoHide, boolean modal) {
    super(autoHide, modal);
  }

  public EnhancedPopup(boolean autoHide) {
    super(autoHide);
  }

  /**
   * Sets whether this object is visible.
   *
   * @param visible <code>true</code> to show the object, <code>false</code> to
   * hide it
   */
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if (visible)
      applyFocus();
  }

  /**
   * Subclasses may override this method to return a child widget that should receive focus
   * after the popup is shown.
   */
  @Override
  @Nullable
  public Focusable getFocusTarget() {
    return null;
  }

  /**
   * Sets the focus on the widget returned by {@link #getFocusTarget()}, and if that's a text box box,
   * places the cursor at the end of its text.
   */
  private void applyFocus() {
    final Focusable focusTarget = getFocusTarget();
    if (focusTarget instanceof IsWidget) {
      // focusTarget not null and is a widget
      focusTarget.setFocus(true);
      // In most browsers, the above invocation of focusTarget.setFocus(true) would suffice, however with IE11, it won't work
      // until some time later (might have something to do with the resize animation), so we keep trying to
      // focus the widget in a repeating command
      Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
        private int i;
        @Override
        public boolean execute() {
          i++;
          if (!isShowing() || JsDocument.get().getActiveElement() == ((IsWidget)focusTarget).asWidget().getElement() || i > 50)
            return false;  // either our work here is done or we're giving up after 50 attempts (we might never succeed if the browser doesn't support Document.activeElement
          // NOTE: we have to call getPrimaryWidget().setFocus(true) after we already checked Document.activeElement, not before
          focusTarget.setFocus(true);
          return true;  // run this command again to make sure that the widget indeed has the focus
        }
      }, 10);

      if (focusTarget instanceof TextBoxBase) {
        TextBoxBase focusableTextBox = (TextBoxBase)focusTarget;
        focusableTextBox.setCursorPos(focusableTextBox.getText().length());
      }
    }
  }

  public boolean isCloserControlsEnabled() {
    return closerControlsEnabled;
  }

  public void setCloserControlsEnabled(boolean enabled) {
    this.closerControlsEnabled = enabled;
  }

  public PopupBuilder<? extends EnhancedPopup> configure() {
    return new PopupBuilder<EnhancedPopup>(this);
  }

  @Override
  public void setPopupPosition(int left, int top) {
    /*
      Width Preservation Workaround:

      If the popup didn't have an explicit width assigned with CSS (or its style.width attribute), then browsers
      will shrink its width when it's positioned near the east side of the browser window, so as a workaround,
      we detect when the call to super.setPopupPosition (which sets its style.left and style.top attrs) has the
      unwanted side-effect of changing the actual (computed) width of the popup, we give it an explicit value for
      style.width, to match the computed width before the shrinkage.

      NOTE: this workaround will only work if browser supports Window.getComputedStyle (most modern browsers do)
      */
    int offsetWidthBefore = getOffsetWidth();
    Style computedStyle = JsWindow.get().getComputedStyle(getStyleElement());
    String computedWidth = null;
    if (computedStyle != null) {
      computedWidth = computedStyle.getWidth();
    }
    setCorrectPopupPosition(left, top);  // reposition the popup
    // now check if repositioning the element caused the rendering engine to change its width
    if (offsetWidthBefore > 0 && offsetWidthBefore != getOffsetWidth()) {
      if (StringUtils.notBlank(computedWidth)) {
        // if the element's style.width doesn't already have an explicit value, assign it to be what it was before repositioning
        Style explicitStyle = getStyleElement().getStyle();
        if (StringUtils.isBlank(explicitStyle.getWidth()))
          explicitStyle.setProperty("width", computedWidth);
      }
    }
  }

  /**
   * Invokes {@link PopupPanel#setPopupPosition(int, int) super.setPopupPosition(left, top)} and then corrects the misguided
   * adjustment in the super method, which, for no good reason, subtracts {@link Document#getBodyOffsetLeft()}
   * and {@link Document#getBodyOffsetTop()} from the intended position.
   * <p>
   * That seems to be a GWT bug that has gone unnoticed for a long time because it is almost never the case
   * when that adjustment actually affects anything.  However, it does appear to be misguided because
   *   (a) it's useless at best, because Document.get().getBodyOffset[Left|Right]() only returns a non-0 value with
   *       DOMImplMozilla and DOMImplTrident (FF and IE<=9), and
   *   (b) it's harmful at worst: if when we simulate a non-0 value for getBodyOffsetLeft
   *       (by calling document.documentElement.style.marginLeft = "150px" in FF
   *       or document.documentElement.style.borderLeft = "150px solid" in IE), it actually messes up the popup position.
   */
  private void setCorrectPopupPosition(int left, int top) {
    // TODO: report this GWT bug (and maybe submit a patch)
    super.setPopupPosition(left, top);
    // set the correct position (to be what the caller actually intended)
    Style style = getStyleElement().getStyle();
    style.setPropertyPx("left", left);
    style.setPropertyPx("top", top);
  }

  /**
   * Sets the position of the popup such that it's center point would be offset it from the top left corner
   * of the currently-visible window rectangle (scroll position) by the given (x,y) percentages of the window size.
   * Example:
   * <pre>
   *   setPopupPositionRelativeToWindowScrollAndSize(0.5, 0.5) // centers both vertically and horizontally
   * </pre>
   */
  public void setPopupPositionRelativeToWindowScrollAndSize(final double leftPct, final double topPct) {
    setPopupPositionRelativeToWindowScroll(
        (int)(Window.getClientWidth() * leftPct) - (getOffsetWidth() / 2),
        (int)(Window.getClientHeight() * topPct) - (getOffsetHeight() / 2));
  }

  /**
   * Sets the position of the popup such that it would be offset it from the currently-visible window
   * rectangle (scroll position) by (x, y) pixels.
   */
  public void setPopupPositionRelativeToWindowScroll(int leftOffset, int topOffset) {
    //    return getScrollTopNative();
    //    return getScrollLeftNative();
    setPopupPosition(
        Math.max(0, Window.getScrollLeft() + leftOffset),
        Math.max(0, Window.getScrollTop() + topOffset));
  }


  /**
   * Sets the position of the popup relative to another widget (the "pivot") using the prefs defined by the argument.
   * Does nothing if that the pivot widget is not attached to the DOM (when a widget is not attached, it's top and left
   * coordinates are both 0).
   */
  public void setPopupPositionRelativeTo(RelativePosition position) {
    WindowGeometry.positionPopupNextToWidget(this, position);
  }


  /** Shows the popup directly underneath the given widget, right-aligned */
  public void showSouthOf(Widget widget) {
    showRelativeTo(new RelativePosition(widget, Alignment.BELOW_RIGHT_EDGES));
  }

  /** Shows the popup directly above the given widget, right-aligned */
  public void showNorthOf(Widget widget) {
    showRelativeTo(new RelativePosition(widget, Alignment.ABOVE_RIGHT_EDGES));
  }

  /**
   * Shows the popup next to the given widget.
   * The meaning of "next to" is determined using the given order of
   * positions, e.g. left, right, above, below.
   */
  public void showNextTo(Widget widget) {
    showRelativeTo(RelativePosition.nextTo(widget));
  }

  public void showRelativeTo(final RelativePosition position) {
    if (!maybeShowRelativeTo(position)) {
      // it doesn't make sense to show the popup until the pivot widget is attached (otherwise it's top and left coordinates are both 0)
      // so we schedule a deferred command to try again
      Scheduler.get().scheduleDeferred(new Command() {
        public void execute() {
          if (!maybeShowRelativeTo(position))
            center();  // if the pivot is still not attached, we just show the popup centered in the browser window
        }
      });
    }
  }

  /**
   * Show the popup relative to the given widget, with the given prefs defining the meaning of "relative".
   *
   * @see PopupPanel#showRelativeTo(UIObject)
   */
  public void showRelativeTo(Widget pivot, Alignment... alignmentPrefs) {
    showRelativeTo(new RelativePosition(pivot, alignmentPrefs));
  }

  /**
   * @return true iff the {@linkplain RelativePosition#getPivot() relative widget} is attached and therefore this call succeeded
   */
  private boolean maybeShowRelativeTo(final RelativePosition position) {
    if (DomUtils.isAttached(position.getPivot())) {
      setPopupPositionAndShow(new PopupPanel.PositionCallback() {
        public void setPosition(int offsetWidth, int offsetHeight) {
          setPopupPositionRelativeTo(position);
        }
      });
      return true;
    }
    return false;
  }

  /**
   * Shows the popup at the given multiples from the current window scroll position, such that the args
   * (0.5, 0.5) would show it centered both vertically and horizontally.
   */
  public void showRelativeToWindow(final double leftPct, final double topPct) {
    setPopupPositionAndShow(new PopupPanel.PositionCallback() {
      public void setPosition(int offsetWidth, int offsetHeight) {
        setPopupPositionRelativeToWindowScrollAndSize(leftPct, topPct);
      }
    });
  }

  /**
   * Shows the popup in the center of the browser window.
   *
   * @see PopupPanel#center()
   */
  public void showCenteredInWindow() {
    showRelativeToWindow(.5, .5);
  }


  public void shake() {
    if (shakingAnimation == null)
      shakingAnimation = new ShakePopupAnimation(this);
    if (!shakingAnimation.isRunning())
      shakingAnimation.run(400);  // if already running we don't want to cancel it and start over
  }

  @Override
  public void hide(boolean autoClosed) {
    if (closerControlsEnabled) {
      if (isShowing() && shakingAnimation != null)
        shakingAnimation.cancel();
      super.hide(autoClosed);
    }
  }

  private OpacityTweenAnimation fadeOutAnimation;
  private Timer fadeOutTimer;

  /**
   * Runs a fade-out animation (opacity 100% to 0) and hides the popup upon its completion.
   * @param animationDuration the animation duration, in millis
   * @return {@code true} if the animation was started;
   *   {@code false} if the popup isn't showing or a fade-out animation is already running
   *
   */
  public boolean fadeOut(int animationDuration) {
    if (fadeOutAnimation == null && isShowing()) {
      fadeOutAnimation = new OpacityTweenAnimation(this, 1, 0) {
        @Override
        protected void onComplete() {
          super.onComplete();
          hide(false);
          fadeOutAnimation = null;
        }
      };
      fadeOutAnimation.run(animationDuration, getElement());
      return true;
    }
    return false;
  }

  /**
   * Starts a timer that invokes {@link #fadeOut(int)} after the given delay.  If this method is invoked again
   * before the popup is hidden, the previously-set timer will be replaced with a new one.
   *
   * @param delayMillis millis to wait before invoking {@link #fadeOut(int)}
   * @param animationDuration arg for {@link #fadeOut(int)}
   * @return {@code true} if a timer was started;
   *         {@code false} if the popup isn't showing or a fade-out animation is already running
   */
  public boolean fadeOutAfterDelay(int delayMillis, int animationDuration) {
    if (!isShowing() || fadeOutAnimation != null)
      return false;
    if (fadeOutTimer != null) {
      // cancel the previous timer and start a new one
      fadeOutTimer.cancel();
      fadeOutTimer = null;
    }
    fadeOutTimer = new Timer() {
      @Override
      public void run() {
        fadeOut(animationDuration);
      }
    };
    fadeOutTimer.schedule(delayMillis);
    return true;
    // TODO: test this
  }

  /**
   * A custom "glass" widget to be displayed instead of the default {@link PopupPanel#glass}
   */
  private PopupGlassSvg glass;

  public PopupGlassSvg getCustomGlass() {
    return glass;
  }

  /**
   * Assigns a custom SVG "glass" widget to be displayed instead of the default {@link PopupPanel#glass}.
   * This glass element will be displayed in the background every time the popup is shown,
   * and overrides any prior invocation of {@link #setGlassEnabled(boolean)}.
   */
  public EnhancedPopup setCustomGlass(PopupGlassSvg glass) {
    // TODO: maybe don't allow calling this method while the popup is already showing (with or without the glass)
    if (this.glass != null && this.glass != glass) {
      // removing or replacing the custom glass
      this.glass.hide();
    }
    if (glass != null) {
      super.setGlassEnabled(false);
    }
    this.glass = glass;
    return this;
  }

  /**
   * When enabled, the background will be blocked with a semi-transparent pane
   * the next time it is shown. If the PopupPanel is already visible, the glass
   * will not be displayed until it is hidden and shown again.
   *
   * @param enabled true to enable, false to disable
   * @throws IllegalStateException if a custom "glass" was assigned via {@link #setCustomGlass(PopupGlassSvg)}
   */
  @Override
  public void setGlassEnabled(boolean enabled) {
    Preconditions.checkState(!enabled || glass == null, "Using a custom glass widget");
    super.setGlassEnabled(enabled);
  }

  /**
   * Overriding {@link PopupPanel#show()} in order to display the {@linkplain #glass custom glass}.
   * The "glass" widget must be attached to document's body
   */
  @Override
  public void show() {
    // must add the custom glass element before the popup is displayed
    if (glass != null && !isShowing()) {
      assert !glass.isShowing();
      glass.show();
    }
    super.show();
  }

  @Override
  protected void onUnload() {
    super.onUnload();
    if (glass != null && glass.isShowing()) {
      glass.hide();
    }
    if (shakingAnimation != null) {
      shakingAnimation.cancel();
      shakingAnimation = null;
    }
  }

  /**
   * Factory method that wraps the given widget with event handlers to show this popup.
   *
   * @param opener The widget to which the event handlers (that show the popup) will be added.
   * @param eventBits Defines which event handlers to add to the opener. Use the constants {@link PopupOpener#CLICK} and/or
   * {@link PopupOpener#HOVER} to set the value.
   */
  public <W extends Widget, P extends EnhancedPopup> PopupOpener<W, P> openWith(W opener, int eventBits) {
    return openWith(opener, eventBits, RelativePosition.nextTo(opener));
  }

  /**
   * Factory method that wraps the given widget with event handlers to show this popup.
   *
   * @param opener The widget to which the event handlers (that show the popup) will be added.
   * @param eventBits Defines which event handlers to add to the opener. Use the constants {@link PopupOpener#CLICK} and/or
   * {@link PopupOpener#HOVER} to set the value.
   * @param position How to position the popup on the screen.
   */
  public <W extends Widget, P extends EnhancedPopup> PopupOpener<W, P> openWith(W opener, int eventBits, RelativePosition position) {
    return new ConcretePopupOpener<W, P>(opener, (P)this, eventBits, position);
  }

  /**
   * Factory method that wraps the given widget with event handlers to show this popup.
   *
   * @param opener The widget to which the event handlers (that show the popup) will be added, also used to
   * calculate the relative position of the popup using the given {@code alignmentPrefs}.
   * @param eventBits Defines which event handlers to add to the opener. Use the constants {@link PopupOpener#CLICK} and/or
   * {@link PopupOpener#HOVER} to set the value.
   * @param alignmentPrefs How to position the popup on the screen, in relation to the opener widget.
   */
  public <W extends Widget, P extends EnhancedPopup> PopupOpener<W, P> openWith(W opener, int eventBits, Alignment... alignmentPrefs) {
    return new ConcretePopupOpener<W, P>(opener, (P)this, eventBits, alignmentPrefs);
  }
}
