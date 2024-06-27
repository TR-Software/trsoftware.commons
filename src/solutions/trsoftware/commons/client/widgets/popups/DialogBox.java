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

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.bundle.CommonsClientBundleFactory;
import solutions.trsoftware.commons.client.event.MultiHandlerRegistration;
import solutions.trsoftware.commons.client.event.NativeEvents;
import solutions.trsoftware.commons.client.event.PageVisibility;
import solutions.trsoftware.commons.client.jso.JsWindow;
import solutions.trsoftware.commons.client.widgets.CompositeHasAllMouseHandlers;
import solutions.trsoftware.commons.client.widgets.Widgets;

/**
 * A form of popup that has a caption area at the top which can be dragged by the user.
 * This is a modified version of {@link com.google.gwt.user.client.ui.DialogBox} with the following improvements:
 * <ul>
 *   <li>adds event handling for PageVisibility and window.onblur
 *   (since we can't detect "mouseup" events after the browser loses focus, we need to end the dragging when this happens)</li>
 *   <li>extends our EnhancedPopup class instead of GWT's DecoratedPopupPanel (which adds lots of unnecessary table cells
 *   to implement border styling; this functionality is now available in pure CSS)</li>
 *   <li>overrides setPopupPosition to constrain our dialog popup to stay within the visible window area
 *   (otherwise it could be confusing if the dialog is modal and the user doesn't see it on the screen)</li>
 * </ul>
 */
@SuppressWarnings("deprecation")
public class DialogBox extends EnhancedPopup {

  /**
   * Set of characteristic interfaces supported by the {@link DialogBox}
   * caption.
   *
   */
  public interface Caption extends HasAllMouseHandlers, IsWidget {
    String DEFAULT_STYLE = CommonsClientBundleFactory.INSTANCE.getCss().Caption();
  }

  /**
   * Default implementation of {@link Caption} that simply extends {@link HTML}.
   * Will be used if header isn't specified.
   */
  public static class HtmlCaption extends HTML implements Caption {
    public HtmlCaption() {
      setStyleName(Caption.DEFAULT_STYLE);
    }
  }

  /**
   * Can be used to implement captions that are more complex.
   */
  public abstract static class CompositeCaption extends CompositeHasAllMouseHandlers implements Caption {
    @Override
    protected void initWidget(Widget widget) {
      super.initWidget(widget);
      setStyleName(Caption.DEFAULT_STYLE);
    }
  }

  public static class CaptionWithIcon extends CompositeCaption {
    public CaptionWithIcon(AbstractImagePrototype icon, String text) {
      this(icon.createImage(), text);
    }

    public CaptionWithIcon(Image icon, String text) {
      icon.setStyleName("icon");
      initWidget(Widgets.flowPanel(icon, Widgets.inlineLabel(text, "text")));
      addStyleName(CommonsClientBundleFactory.INSTANCE.getCss().CaptionWithIcon());
    }
  }

  /** Handles all the events needed to implement the Drag & Drop functionality */
  private class DraggingEventHandler implements MouseDownHandler, MouseMoveHandler, MouseUpHandler, PageVisibility.ChangeHandler, BlurHandler {

    public void onMouseDown(MouseDownEvent event) {
      beginDragging(event);
    }

    public void onMouseMove(MouseMoveEvent event) {
      continueDragging(event);
    }

    public void onMouseUp(MouseUpEvent event) {
      endDragging();
    }

    @Override
    public void onVisibilityChange(PageVisibility.ChangeEvent event) {
      // we can't detect "mouseup" events after the browser loses focus, so we need to end the dragging when this happens
      if (event.isHidden())
        endDragging();
    }

    @Override
    public void onBlur(BlurEvent event) {
      // we can't detect "mouseup" events after the browser loses focus, so we need to end the dragging when this happens
      endDragging();
    }
  }

  private class DragState {
    private int startX, startY;

    /**
     * @param startX The x-coordinate when started dragging
     * @param startY The y-coordinate when started dragging
     */
    private DragState(int startX, int startY) {
      this.startX = startX;
      this.startY = startY;
    }

    private void updatePosition(int newX, int newY) {
      setPopupPosition(newX - startX, newY - startY);
    }

  }

  /**
   * The default style name.
   */
  private static final String DEFAULT_STYLENAME = CommonsClientBundleFactory.INSTANCE.getCss().DialogBox();
  private FlowPanel pnlMain = new FlowPanel();
  private PopupCloserButton btnClose;
  private Caption caption;
  private SimplePanel content = new SimplePanel();
  private DragState dragState;

  /**
   * Registrations for the handlers we registered for events on DOM elements outside this widget,
   * which should be removed when this widget is hidden, to avoid leaking memory.
   */
  private MultiHandlerRegistration handlerRegistrations;
  private DraggingEventHandler eventHandler = new DraggingEventHandler();

  /**
   * Creates an empty dialog box. It should not be shown until its child widget
   * has been added using {@link #add(Widget)}.
   */
  public DialogBox() {
    this(false);
  }

  /**
   * Creates an empty dialog box specifying its "auto-hide" property. It should
   * not be shown until its child widget has been added using
   * {@link #add(Widget)}.
   *
   * @param autoHide <code>true</code> if the dialog should be automatically
   *          hidden when the user clicks outside of it
   */
  public DialogBox(boolean autoHide) {
    this(autoHide, true);
  }

  /**
   * Creates an empty dialog box specifying its {@link Caption}. It should not
   * be shown until its child widget has been added using {@link #add(Widget)}.
   *
   * @param captionWidget the widget that is the DialogBox's header.
   */
  public DialogBox(Caption captionWidget) {
    this(false, true, captionWidget);
  }

  /**
   * Creates an empty dialog box specifying its "auto-hide" and "modal"
   * properties. It should not be shown until its child widget has been added
   * using {@link #add(Widget)}.
   *
   * @param autoHide <code>true</code> if the dialog should be automatically
   *          hidden when the user clicks outside of it
   * @param modal <code>true</code> if keyboard and mouse events for widgets not
   *          contained by the dialog should be ignored
   */
  public DialogBox(boolean autoHide, boolean modal) {
    this(autoHide, modal, new HtmlCaption());
  }

  /**
   *
   * Creates an empty dialog box specifying its "auto-hide", "modal" properties
   * and an implementation a custom {@link Caption}. It should not be shown
   * until its child widget has been added using {@link #add(Widget)}.
   *
   * @param autoHide <code>true</code> if the dialog should be automatically
   *          hidden when the user clicks outside of it
   * @param modal <code>true</code> if keyboard and mouse events for widgets not
   *          contained by the dialog should be ignored
   * @param captionWidget the widget that is the DialogBox's header.
   */
  public DialogBox(boolean autoHide, boolean modal, Caption captionWidget) {
    super(autoHide, modal);

    assert captionWidget != null : "The caption must not be null";
    captionWidget.asWidget().removeFromParent();
    caption = captionWidget;
    pnlMain.add(captionWidget);
    pnlMain.add(content);

    super.setWidget(pnlMain);
    content.addStyleName(CommonsClientBundleFactory.INSTANCE.getCss().dialogContent());
    setStyleName(DEFAULT_STYLENAME);

    // register for the events we need on the widget itself (these don't need to be removed for the lifetime of the widget)
    // the rest of the events we need will be registered in the show() method.
    addDomHandler(eventHandler, MouseDownEvent.getType());
    addDomHandler(eventHandler, MouseUpEvent.getType());
    addDomHandler(eventHandler, MouseMoveEvent.getType());
  }

  /**
   * Adds a simple "x" button (for hiding the dialog) to its top right corner.
   * This button is styled in CSS using the {@code .xButton} style name.
   */
  public void insertCloserButton(PopupCloserButton closerButton) {
    if (btnClose == null && isCloserControlsEnabled()) // make sure we don't already have it
      pnlMain.insert(btnClose = closerButton, 0);
  }

  /**
   * Adds a simple "x" button (for hiding the dialog) to its top right corner.
   * This button is styled in CSS using the {@code .xButton} style name.
   */
  protected void insertCloserButton() {
    insertCloserButton(new PopupCloserButton(this));
  }

  /**
   * Controls the visibility of the button that was created by {@link #insertCloserButton(PopupCloserButton)}
   */
  public void setCloserButtonVisible(boolean visible) {
    if (btnClose != null)
      btnClose.setVisible(visible);
  }

  @Override
  public void setCloserControlsEnabled(boolean enabled) {
    setCloserButtonVisible(enabled);
    super.setCloserControlsEnabled(enabled);
  }

  private boolean dragging() {
    return dragState != null;
  }

  // We override the following methods to make our content panel impersonate our SimplePanel widget (since we've inserted the caption as a child)
  @Override
  public void clear() {
    content.clear();
  }

  @Override
  public Widget getWidget() {
    return content.getWidget();
  }

  @Override
  public boolean remove(Widget w) {
    return content.remove(w);
  }

  @Override
  public void setWidget(Widget w) {
    content.setWidget(w);
    super.setWidget(pnlMain);  // this call is needed because we can't call PopupPanel.maybeUpdateSize() directly (it's package-private)
  }

  /**
   * Provides access to the dialog's caption.
   *
   * @return the logical caption for this dialog box
   */
  public Caption getCaption() {
    return caption;
  }


  @Override
  protected void onLoad() {
    super.onLoad();
    if (handlerRegistrations == null) {
      handlerRegistrations = new MultiHandlerRegistration(
          PageVisibility.addVisibilityChangeHandler(eventHandler),
          NativeEvents.addDomHandler(this, JsWindow.get(), eventHandler, BlurEvent.getType())
      );
    }
  }

  @Override
  protected void onUnload() {
    if (handlerRegistrations != null) {
      handlerRegistrations.removeHandler();
      handlerRegistrations = null;
    }
    super.onUnload();
  }

  /**
   * We override this method to constrain our dialog popup to stay within the visible window area (otherwise it could
   * be confusing if the dialog is modal and the user doesn't see it on the screen).
   */
  @Override
  public void setPopupPosition(int left, int top) {
    // constrain our popup to stay within the visible window area
    int offsetWidth = getOffsetWidth();
    int offsetHeight = getOffsetHeight();
    Document doc = Document.get();
    if (offsetWidth > 0 && offsetHeight > 0) {
      // we apply the constraint only if the popup has already been attached (offsetWidth/Height > 0), otherwise we want
      // to allow the rendering engine to do the popup's layout normally
      left = Math.max(doc.getScrollLeft(),
          Math.min(left, doc.getClientWidth() + doc.getScrollLeft() - offsetWidth));
      top = Math.max(doc.getScrollTop(),
          Math.min(top, doc.getClientHeight() + doc.getScrollTop() - offsetHeight));
    }
    super.setPopupPosition(left, top);
  }

  @Override
  public void onBrowserEvent(Event event) {
    // If we're not dragging, suppress all mouse events that don't occur in the caption wrapper
    switch (event.getTypeInt()) {
      case Event.ONMOUSEDOWN:
      case Event.ONMOUSEUP:
      case Event.ONMOUSEMOVE:
        if (!dragging() && !isCaptionEvent(event))
          return;
    }
    super.onBrowserEvent(event);
  }


  /*
   NOTE: there's now a native browser Drag & Drop API (see https://developer.mozilla.org/en-US/docs/Web/API/HTML_Drag_and_Drop_API
   and com.google.gwt.event.dom.client.DragEvent), but it's pretty complex, and seems to be geared for dragging items
   and dropping them onto "drop targets", whereas in this widget we just want to enable dragging it to reposition it,
   not to drop it onto a target, so we're not using that Drag & Drop API.
   */


  /**
   * Called on mouse down in the caption area, begins the dragging loop by
   * turning on event capture.
   *
   * @see DOM#setCapture
   * @see #continueDragging
   * @param event the mouse down event that triggered dragging
   */
  protected void beginDragging(MouseDownEvent event) {
    if (DOM.getCaptureElement() == null) {
      /*
       * Need to check to make sure that we aren't already capturing an element
       * otherwise events will not fire as expected. If this check isn't here,
       * any class which extends custom button will not fire its click event for
       * example.
       */
      dragState = new DragState(event.getX(), event.getY());
      DOM.setCapture(getElement());
    }
  }

  /**
   * Called on mouse move in the caption area, continues dragging if it was
   * started by {@link #beginDragging}.
   *
   * @see #beginDragging
   * @see #endDragging
   * @param event the mouse move event that continues dragging
   */
  protected void continueDragging(MouseMoveEvent event) {
    if (dragging()) {
      // the event (x,y) is measured from the caption's top left corner,
      // so we must add the caption's position to get the absolute coords of the mouse pointer
      int absX = event.getX() + getAbsoluteLeft();
      int absY = event.getY() + getAbsoluteTop();
      // If the mouse is off the screen to the left, right, or top, don't move the dialog box.
      // This would let users lose dialog boxes, which would be bad for modal popups.
      //    return getScrollLeftNative();
      if (absX < 0 || absY < 0 || absX >= Window.getClientWidth() + Window.getScrollLeft()) {
        if (!PageVisibility.isSupported()) {
          // we want to end dragging if we can't detect whether the window is no longer active (using the Page Visibility API),
          // otherwise, we would lose any mouseup events that happen after that
          endDragging();
        }
        return;
      }
      dragState.updatePosition(absX, absY);
    }
  }


  /**
   * Ends dragging by ending event capture. Called either on mouse up in the caption area or when the mouse cursor
   * leaves the browser window (because there's no way to detect if the mouse button has been released when the mouse
   * has left the browser window and the browser window is no longer active.
   *
   * @see DOM#releaseCapture
   * @see #beginDragging
   * @see #endDragging
   */
  protected void endDragging() {
    if (dragging()) {
      dragState = null;
      DOM.releaseCapture(getElement());
    }
  }

  /**
   * <b>Affected Elements:</b>
   * <ul>
   * <li>-caption = text at the top of the {@link DialogBox}.</li>
   * <li>-content = the container around the content.</li>
   * </ul>
   *
   * @see UIObject#onEnsureDebugId(String)
   */
  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
    caption.asWidget().ensureDebugId(baseID + "-caption");
    content.asWidget().ensureDebugId(baseID + "-content");
  }

  @Override
  protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
    // We need to preventDefault() on mouseDown events (outside of the
    // DialogBox content) to keep text from being selected when it
    // is dragged.
    NativeEvent nativeEvent = event.getNativeEvent();

    if (!event.isCanceled() && (event.getTypeInt() == Event.ONMOUSEDOWN) && isCaptionEvent(nativeEvent)) {
      nativeEvent.preventDefault();
    }

    super.onPreviewNativeEvent(event);
  }

  private boolean isCaptionEvent(NativeEvent event) {
    EventTarget target = event.getEventTarget();
    return Element.is(target) && caption.asWidget().getElement().isOrHasChild(Element.as(target));
  }

  /**
   * We override this method simply to preclude the possibility of ever enabling {@link ResizeAnimation},
   * because it contains a bug (actually more like a slight oversight on behalf of the GWT developers): it uses the
   * deprecated CSS <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/clip">clip</a> property to adjust the
   * size of the popup element (at the end of its run, it sets {@code clip: rect(auto auto auto auto);}),
   * which although it reads like it might have the effect of undoing the adjustments performed by the animation, its
   * actual effect in every major browser is to literally clip off anything that might be rendered outside the natural
   * boundaries of the popup element. Therefore, we can't use this animation if we want to have something like shadows
   * (defined with the CSS <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/box-shadow">box-shadow</a>
   * property), or the little "X" close button that we might want to include by calling
   * {@link DialogBox#insertCloserButton(PopupCloserButton)}, (which is rendered just outside the top right corner
   * of the popup (by setting negative values for {@code margin-right} and {@code margin-top}).
   * @deprecated TODO: report this GWT bug (and maybe submit a patch)
   */
  @Override
  public final void setAnimationEnabled(boolean enable) {
    System.err.println("WARNING: Avoid using DialogBox.setAnimationEnabled (see doc for explanation)");
  }
}