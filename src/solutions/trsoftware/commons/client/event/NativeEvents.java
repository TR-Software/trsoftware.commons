package solutions.trsoftware.commons.client.event;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Widget;

/**
 * Supports directly attaching JS event listeners to JSOs (like DOM elements).
 *
 * GWT's {@link com.google.gwt.dom.client.DOMImpl} doesn't define all the native events that we might want to listen to,
 * so this class allows us to support those.  So if, for example, we tried to call
 * {@link com.google.gwt.user.client.ui.Widget#addBitlessDomHandler} with an event type that GWT doesn't recognize,
 * we'd get a "Trying to sink unknown event type" JS exception.
 *
 * NOTE: This class provides the same functionality as {@link elemental.js.dom.JsElementalMixinBase#addEventListener(String, elemental.events.EventListener)},
 * which is part of GWT's experimental new "Elemental" package, but Elemental only works with SuperDevMode
 * (will produce a GWT compiler error when running under the regular DevMode, see http://stackoverflow.com/questions/17428265/adding-elemental-to-gwt ).
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/EventTarget/addEventListener">Mozilla API Reference</a>
 * @see <a href="https://msdn.microsoft.com/en-us/library/jj853328(v=vs.85).aspx">MSDN Reference</a>
 *
 * @author Alex, 4/5/2015
 */
public class NativeEvents {

  // TODO: test this class with other browsers (so far only been tested with Chrome)

  private NativeEvents() {  // this class only provides static methods, so it's not to be instantiated
  }

  private static class Registration implements HandlerRegistration {
    protected JavaScriptObject removerFcn;

    protected Registration() {
    }

    protected Registration(JavaScriptObject removerFcn) {
      this.removerFcn = removerFcn;
    }

    @Override
    public native void removeHandler() /*-{
      this.@solutions.trsoftware.commons.client.event.NativeEvents.Registration::removerFcn.call(this);
    }-*/;
  }

  /**
   * Utility class that combines {@link HandlerRegistration} and {@link EventListener} to avoid creating
   * two separate objects for those functions.
   */
  public static abstract class ListenerRegistration extends Registration implements EventListener {
    /**
     * Adds a native event listener to the given DOM element.  This is useful for supporting new event types that aren't
     * handled by GWT.
     *
     * NOTE: The caller must remember to call {@link #removeHandler()} to avoid leaking memory.
     *
     * @param element The listener will be added to this DOM element.  This arg is loosely-typed, we don't check
     * that it's actually a DOM node that implements the DOM <a href="https://developer.mozilla.org/en-US/docs/Web/API/EventTarget">EventTarget</a>
     * interface.
     * @param eventName The name of the event, (e.g. "click", or "visibilitychange")
     * @param useCapture If true, useCapture indicates that the user wishes to initiate capture. After initiating capture,
     * all events of the specified type will be dispatched to the registered listener before being dispatched to any
     * EventTarget beneath it in the DOM tree. Events which are bubbling upward through the tree will not trigger a
     * listener designated to use capture. In other words, if false, the event will be handled in the "bubble" phase (not
     * the "preview" phase)
     */
    public ListenerRegistration(JavaScriptObject element, String eventName, boolean useCapture) {
      removerFcn = addNativeEventListenerImpl(element, eventName, useCapture, this);
    }
  }

  /**
   * Utility class used in {@link #addDomHandler(Widget, JavaScriptObject, EventHandler, DomEvent.Type)}.
   */
  private static class WidgetRegistration<H extends EventHandler> extends Registration {
    private final HandlerRegistration widgetRegistration;

    public WidgetRegistration(JavaScriptObject element, DomEvent.Type eventType, boolean useCapture, final Widget receiver, H handler) {
      widgetRegistration = receiver.addHandler(handler, eventType);
      removerFcn = addNativeEventListenerImpl(element, eventType.getName(), useCapture, receiver);
    }

    @Override
    public void removeHandler() {
      try {
        super.removeHandler();
      } finally {
        widgetRegistration.removeHandler();
      }
    }
  }

  /**
   * Adds a native event listener to the given DOM element.  This is useful for supporting new event types that aren't
   * handled by GWT.
   *
   * NOTE: The caller must remember to remove the handler explicitly to avoid leaking memory.
   *
   * @param element The listener will be added to this DOM element.  This arg is loosely-typed, we don't check
   * that it's actually a DOM node that implements the DOM <a href="https://developer.mozilla.org/en-US/docs/Web/API/EventTarget">EventTarget</a>
   * interface.
   * @param eventName The name of the event, (e.g. "click", or "visibilitychange")
   * @param useCapture If true, useCapture indicates that the user wishes to initiate capture. After initiating capture,
   * all events of the specified type will be dispatched to the registered listener before being dispatched to any
   * EventTarget beneath it in the DOM tree. Events which are bubbling upward through the tree will not trigger a
   * listener designated to use capture. In other words, if false, the event will be handled in the "bubble" phase (not
   * the "preview" phase)
   * @param listener Will be called by the added listener function.
   * @return A memento that can remove the listener added by this method.
   */
  public static HandlerRegistration addNativeEventListener(JavaScriptObject element, String eventName, boolean useCapture, EventListener listener) {
    return new Registration(addNativeEventListenerImpl(element, eventName, useCapture, listener));
  }

  /**
   * Convenience method that can be used when a {@link Widget} wants to listen for events on a different
   * element (not itself).  For example, if it wants to listen for a {@link BlurEvent} on {@code document}, this
   * method allows it to use its own event dispatch mechanism (e.g. {@link HandlerManager}, etc.)
   * This method is similar to {@link Widget#addDomHandler(EventHandler, DomEvent.Type)}, without making the
   * widget "sink" this event on itself.
   *
   * @param element the source for the event
   * @param handler the handler
   * @param type the event key
   * @return {@link HandlerRegistration} used to remove the handler
   */
  public static <H extends EventHandler> HandlerRegistration addDomHandler(final Widget receiver, JavaScriptObject element, H handler, DomEvent.Type<H> type) {
    return new WidgetRegistration<H>(element, type, false, receiver, handler);
  }

  /**
   * Adds a native event listener to the given DOM element.  This is useful for supporting new event types that aren't
   * handled by GWT.
   *
   * @param element The listener will be added to this DOM element.
   * @param eventName The name of the event, (e.g. "click", or "visibilitychange")
   * @param useCapture If true, useCapture indicates that the user wishes to initiate capture. After initiating capture,
   * all events of the specified type will be dispatched to the registered listener before being dispatched to any
   * EventTarget beneath it in the DOM tree. Events which are bubbling upward through the tree will not trigger a
   * listener designated to use capture. In other words, if false, the event will be handled in the "bubble" phase (not
   * the "preview" phase)
   * @param listener Will be called by the added listener function.
   * @return A JavaScript 0-arg function that can be called to remove the listener added by this method.
   */
  private static native JavaScriptObject addNativeEventListenerImpl(JavaScriptObject element, String eventName, boolean useCapture, EventListener listener) /*-{
    // 1) we set up a native wrapper function to add to the element
    // NOTE: we're using a wrapper function as an event handler because there's no cross-browser way to get a "bound method" object
    // On newer browsers (IE9+) we could have used Function.prototype.bind, e.g.:
    // var handlerFcn =  $entry(@com.google.gwt.user.client.EventListener::onBrowserEvent(Lcom/google/gwt/user/client/Event;)).bind(listener);
    var handlerFcn = $entry(function (evt) {
      listener.@com.google.gwt.user.client.EventListener::onBrowserEvent(Lcom/google/gwt/user/client/Event;)(evt);
    })
    // 2) we attach our listener function in a cross-browser way (idea borrowed from http://javascriptrules.com/2009/07/22/cross-browser-event-listener-with-design-patterns/ )
    if (element.addEventListener) {
      // All standards-compliant browsers
      element.addEventListener(eventName, handlerFcn, useCapture);
      return function() {
        element.removeEventListener(eventName, handlerFcn, useCapture);
      };
    }
    else {
      // Non-standards-compliant browsers
      eventName = 'on' + eventName;
      if (element.attachEvent) {
        // Internet Explorer before IE9
        // NOTE: these functions don't support the useCapture arg; we could probably hack it by calling setCapture on the element (see https://msdn.microsoft.com/en-us/library/ms536742(v=vs.85).aspx), but it doesn't seem worth it
        element.attachEvent(eventName, handlerFcn);
        return function() {
          element.detachEvent(eventName, handlerFcn);
        };
      }
      else {
        // last resort (very old browsers, pre-DOM2)
        // TODO: check that the element contains these fields?  Maybe the problem is that the JSO is not a Node that supports events, in which case we probably want to throw an exception
        // TODO: must create a backup of the original handler, if any (see com.google.gwt.user.client.impl.WindowImpl.initWindowResizeHandler)
        element[eventName] = handlerFcn;
        return function() {
          if (element[eventName] == handlerFcn)
            element[eventName] = null;
        };
      }
    }
  }-*/;

}
