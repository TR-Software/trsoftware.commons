/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.client.widgets.popups;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.event.MultiHandlerRegistration;
import solutions.trsoftware.commons.client.util.geometry.Alignment;
import solutions.trsoftware.commons.client.util.geometry.RelativePosition;
import solutions.trsoftware.commons.shared.util.callables.Function0;

/**
 * Encapsulates the logic associated with showing a popup in response to a click or mouse hover event on a widget
 * (typically hyperlink or an image button).  This class reduces boilerplate in attaching handlers for this to a widget,
 * and also can be used to make sure that only one instance of the popup will be visible at any given time (if no
 * other means of showing the popup are used.  The {@link #setReusePopup(boolean)} method can be used to control
 * whether the same instance of the {@link EnhancedPopup} will be used every time, or whether to call the
 * {@link #createPopup()} method every time the popup is to be shown (default).
 *
 * @author Alex
 */
public abstract class PopupOpener<W extends Widget, P extends EnhancedPopup> implements ClickHandler, MouseOverHandler, MouseOutHandler, AttachEvent.Handler {

  /*
   NOTE: this class does not extend Composite, so that widgets that are already attached to the document
   (e.g. those created by wrapping a native element with a method like Anchor.wrap) can be used.
   Adding such a widget to a composite will un-attach it, and thus it will not receive events.
    */

  /**
   * A bitmask value that can be passed to {@link #PopupOpener(Widget, int, RelativePosition)} to indicate
   * that the the popup should be shown in response to a click on the opener widget.
   */
  public static final int CLICK = 1;

  /**
   * A bitmask value that can be passed to {@link #PopupOpener(Widget, int, RelativePosition)} to indicate
   * that the the popup should be shown in response to hovering the mouse over the opener widget.
   */
  public static final int HOVER = 2;

  /**
   * This option controls whether the same instance of popup is to be shown every time
   * (if {@code true} then {@link #createPopup()} will only be called once).
   */
  private boolean reusePopup;

  /**
   * This option controls whether {@link EnhancedPopup#shake()} will be called when the {@link #showPopup()} method
   * is invoked while the popup is already showing.
   */
  private boolean shakeIfAlreadyShowing = true;

  /** The "opener" - i.e. the widget to which event handlers will be added */
  private W widget;

  protected P popup = null;
  private RelativePosition popupPosition;
  private HandlerRegistration clickHandlerReg;
  private HandlerRegistration mouseHoverHandlersReg;
  private HandlerRegistration attachmentHandlerReg;

  /**
   * Wraps the given {@code opener} widget but does not add any event handlers to it.
   * The methods {@link #setShowPopupOnClick(boolean)} and {@link #setShowPopupOnMouseHover(boolean)} can be used
   * to add those event handlers later.
   *
   * @param opener The widget to which the event handlers (that show the popup) will be added.
   * @param position How to position the popup on the screen.
   */
  protected PopupOpener(W opener, RelativePosition position) {
    this.widget = opener;
    this.popupPosition = position;
  }

  /**
   * Shortcut constructor that relieves the need for calling {@link #setShowPopupOnClick(boolean)}
   * and {@link #setShowPopupOnMouseHover(boolean)} later.
   *
   * @param opener The widget to which the event handlers (that show the popup) will be added.
   * @param eventBits Defines which event handlers to add to the opener. Use the constants {@link #CLICK} and/or
   * {@link #HOVER} to set the value. This is parameter provides a shortcut that can be used instead of calling
   * {@link #setShowPopupOnClick(boolean)} and {@link #setShowPopupOnMouseHover(boolean)}.
   * @param position How to position the popup on the screen.
   */
  public PopupOpener(W opener, int eventBits, RelativePosition position) {
    this(opener, position);
    if ((eventBits & CLICK) != 0)
      setShowPopupOnClick(true);
    if ((eventBits & HOVER) != 0)
      setShowPopupOnMouseHover(true);
  }

  /**
   * Shortcut constructor that relieves the need for calling {@link #setShowPopupOnClick(boolean)}
   * and {@link #setShowPopupOnMouseHover(boolean)} later.
   *
   * @param opener The widget to which the event handlers (that show the popup) will be added, also used to
   * calculate the relative position of the popup using the given {@code alignmentPrefs}.
   * @param eventBits Defines which event handlers to add to the opener. Use the constants {@link #CLICK} and/or
   * {@link #HOVER} to set the value. This is parameter provides a shortcut that can be used instead of calling
   * {@link #setShowPopupOnClick(boolean)} and {@link #setShowPopupOnMouseHover(boolean)}.
   * @param alignmentPrefs How to position the popup on the screen, in relation to the opener widget.
   */
  public PopupOpener(W opener, int eventBits, Alignment... alignmentPrefs) {
    this(opener, eventBits, new RelativePosition(opener, alignmentPrefs));
  }

  /**
   * Called every time the popup needs to be shown.  Subclasses can decide whether to create a new instance
   * or to reuse an existing one ({@link ConcretePopupOpener} exists to support the latter case).
   */
  protected abstract P createPopup();

  /** @return {@link #reusePopup} */
  public boolean isReusePopup() {
    return reusePopup;
  }

  /** Sets {@link #reusePopup} */
  public PopupOpener<W, P> setReusePopup(boolean enable) {
    this.reusePopup = enable;
    return this;  // for method chaining
  }

  /** @return {@link #shakeIfAlreadyShowing} */
  public boolean isShakeIfAlreadyShowing() {
    return shakeIfAlreadyShowing;
  }

  /** Sets {@link #shakeIfAlreadyShowing} */
  public PopupOpener<W, P> setShakeIfAlreadyShowing(boolean shakeIfAlreadyShowing) {
    this.shakeIfAlreadyShowing = shakeIfAlreadyShowing;
    return this;  // for method chaining
  }

  /** @return whether the popup will be hidden when the opener widget is detached from the DOM. */
  public boolean isHideOnDetach() {
    return attachmentHandlerReg != null;
  }

  /** This option controls whether the popup should be hidden when the opener widget is detached from the DOM. */
  public PopupOpener<W, P> setHideOnDetach(boolean enable) {
    attachmentHandlerReg = addOrRemoveHandlers(attachmentHandlerReg, enable, new Function0<HandlerRegistration>() {
      @Override
      public HandlerRegistration call() {
        return getWidget().addAttachHandler(PopupOpener.this);
      }
    });
    return this;  // for method chaining
  }

  public boolean isShowPopupOnClick() {
    return clickHandlerReg != null;
  }

  public PopupOpener<W, P> setShowPopupOnClick(boolean enable) {
    clickHandlerReg = addOrRemoveHandlers(clickHandlerReg, enable, new Function0<HandlerRegistration>() {
      @Override
      public HandlerRegistration call() {
        return getWidget().addDomHandler(PopupOpener.this, ClickEvent.getType());
      }
    });
    return this;  // for method chaining
  }

  public boolean isShowPopupOnMouseHover() {
    return mouseHoverHandlersReg != null;
  }

  public PopupOpener<W, P> setShowPopupOnMouseHover(boolean enable) {
    mouseHoverHandlersReg = addOrRemoveHandlers(mouseHoverHandlersReg, enable, new Function0<HandlerRegistration>() {
      @Override
      public HandlerRegistration call() {
        return new MultiHandlerRegistration(
            addHoverShowHandler(),
            getWidget().addDomHandler(PopupOpener.this, MouseOutEvent.getType())
        ).asLegacyGwtRegistration();
      }
    });
    return this;  // for method chaining
  }

  protected HandlerRegistration addHoverShowHandler() {
    // we allow subclasses to override this method because PIP needs to be able to handle MouseMove instead of MouseOver
    // events, because it doesn't want the popup to appear a player's car enters the mouse cursor
    return getWidget().addDomHandler(PopupOpener.this, MouseOverEvent.getType());
  }

  private static HandlerRegistration addOrRemoveHandlers(HandlerRegistration priorRegistration, boolean enable, Function0<HandlerRegistration> addHandlers) {
    if (enable && priorRegistration == null)
      return addHandlers.call();
    else if (!enable && priorRegistration != null) {
      priorRegistration.removeHandler();
      return null;
    }
    return priorRegistration;
  }


  public void hidePopup() {
    if (isPopupShowing())
      popup.hide();
  }

  public boolean isPopupShowing() {
    return popup != null && popup.isShowing();
  }

  /** @return The opener widget encapsulated by this composite */
  public W getWidget() {
    return widget;
  }

  public void showPopup() {
    if (isPopupShowing()) {
      if (shakeIfAlreadyShowing)
        popup.shake();  // draw user's attention to the popup that's already showing
    }
    else {
      getOrCreatePopup();
      if (popupPosition != null)
        popup.showRelativeTo(popupPosition); // TODO: move the popupPosition pref field to EnhancedPopup?
      else
        popup.show();
    }
  }

  public P getOrCreatePopup() {
    if (popup == null || !reusePopup)
      popup = createPopup();
    return popup;
  }

  public void onClick(ClickEvent event) {
    showPopup();
  }

  public void onMouseOver(MouseOverEvent event) {
    showPopup();
  }

  public void onMouseOut(MouseOutEvent event) {
    hidePopup();
  }

  @Override
  public void onAttachOrDetach(AttachEvent event) {
    if (!event.isAttached())
      hidePopup();
  }
}