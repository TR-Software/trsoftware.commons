/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.client.event;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.event.input.InputEvent;

/**
 * Supports directly attaching JS event listeners to non-{@link Element} DOM Nodes (such as {@code Document} and {@code Window}).
 * <p>
 * <strong>Caution:</strong>
 * Using this class for attaching attaching arbitrary event listeners to {@linkplain Widget widgets} (e.g. for events
 * that don't have built-in GWT support) is not recommended due to potential memory leaks
 * (see the <a href="http://www.gwtproject.org/articles/dom_events_memory_leaks_and_you.html">"DOM Memory Leaks"</a> article,
 * which explains the reasoning behind {@link Widget#sinkEvents(int)} and {@link Widget#onBrowserEvent(Event)}).
 * <p>
 * For actual {@link Widget} instances it's better to define a custom {@link DomEvent} subclass and use
 * {@link Widget#addDomHandler} in order to handle events that don't already have native GWT implementations.
 * See {@link InputEvent} as an example.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/EventTarget/addEventListener">Mozilla API Reference</a>
 * @see <a href="https://msdn.microsoft.com/en-us/library/jj853328(v=vs.85).aspx">MSDN Reference</a>
 * @see elemental.js.dom.JsElementalMixinBase#addEventListener
 * @see <a href="http://www.gwtproject.org/articles/dom_events_memory_leaks_and_you.html">"DOM Memory Leaks" by Joel Webber</a>
 *
 * @author Alex, 4/5/2015
 */
public class NativeEvents {

  // TODO: test this class with more browsers (so far only been tested with Chrome)

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
     * @param target The listener will be added to this DOM node.  This arg is loosely-typed, we don't check
     * that it's actually a DOM node that implements the
     * <a href="https://developer.mozilla.org/en-US/docs/Web/API/EventTarget">{@code EventTarget}</a> interface.
     * @param eventName The name of the event, (e.g. "click", or "visibilitychange")
     * @param useCapture If true, useCapture indicates that the user wishes to initiate capture. After initiating capture,
     * all events of the specified type will be dispatched to the registered listener before being dispatched to any
     * EventTarget beneath it in the DOM tree. Events which are bubbling upward through the tree will not trigger a
     * listener designated to use capture. In other words, if false, the event will be handled in the "bubble" phase (not
     * the "preview" phase)
     */
    public ListenerRegistration(JavaScriptObject target, String eventName, boolean useCapture) {
      removerFcn = addNativeEventListenerImpl(target, eventName, useCapture, this);
    }
  }

  /**
   * Utility class used by {@link #addDomHandler(Widget, JavaScriptObject, EventHandler, DomEvent.Type)}.
   */
  private static class WidgetRegistration<H extends EventHandler> extends Registration {
    private final HandlerRegistration widgetRegistration;

    WidgetRegistration(JavaScriptObject target, DomEvent.Type<H> eventType, boolean useCapture, final Widget receiver, H handler) {
      widgetRegistration = receiver.addHandler(handler, eventType);
      removerFcn = addNativeEventListenerImpl(target, eventType.getName(), useCapture, receiver);
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
   * Adds a native event listener to the given DOM node.
   * Can be used for attaching JS event listeners to non-{@link Element} DOM Nodes (such as {@code Document} and {@code Window})
   * or supporting new event types that aren't implemented by GWT.
   *
   * NOTE: The caller must remember to remove the handler explicitly to avoid leaking memory.
   *
   * @param target The listener will be added to this DOM node.  This arg is loosely-typed, we don't check
   * that it's actually a DOM node that implements the
   * <a href="https://developer.mozilla.org/en-US/docs/Web/API/EventTarget">{@code EventTarget}</a> interface.
   * @param eventName The name of the event, (e.g. "click", or "visibilitychange")
   * @param useCapture If true, useCapture indicates that the user wishes to initiate capture. After initiating capture,
   * all events of the specified type will be dispatched to the registered listener before being dispatched to any
   * EventTarget beneath it in the DOM tree. Events which are bubbling upward through the tree will not trigger a
   * listener designated to use capture. In other words, if false, the event will be handled in the "bubble" phase (not
   * the "preview" phase)
   * @param listener Will be called by the added listener function.
   * @return A memento that can remove the listener added by this method.
   */
  public static HandlerRegistration addNativeEventListener(JavaScriptObject target, String eventName, boolean useCapture, EventListener listener) {
    return new Registration(addNativeEventListenerImpl(target, eventName, useCapture, listener));
  }

  /**
   * Convenience method that can be used when a {@link Widget} wants to listen for events on a different
   * element (not itself).  For example, if it wants to listen for a {@link BlurEvent} on {@code document}, this
   * method allows it to use its own event dispatch mechanism (e.g. {@link HandlerManager}, etc.)
   * This method is similar to {@link Widget#addDomHandler(EventHandler, DomEvent.Type)}, without making the
   * widget "sink" this event on itself.
   *
   * @param target the source for the event
   * @param handler the handler
   * @param type the event key
   * @return {@link HandlerRegistration} used to remove the handler
   */
  public static <H extends EventHandler> HandlerRegistration addDomHandler(final Widget receiver, JavaScriptObject target, H handler, DomEvent.Type<H> type) {
    return new WidgetRegistration<H>(target, type, false, receiver, handler);
  }

  /**
   * Adds a native event listener to the given DOM node.
   * Can be used for attaching JS event listeners to non-{@link Element} DOM Nodes (such as {@code Document} and {@code Window})
   * or supporting new event types that aren't implemented by GWT.
   *
   * @param target The listener will be added to this DOM node.
   * @param eventName The name of the event, (e.g. "click", or "visibilitychange")
   * @param useCapture If true, useCapture indicates that the user wishes to initiate capture. After initiating capture,
   * all events of the specified type will be dispatched to the registered listener before being dispatched to any
   * EventTarget beneath it in the DOM tree. Events which are bubbling upward through the tree will not trigger a
   * listener designated to use capture. In other words, if false, the event will be handled in the "bubble" phase (not
   * the "preview" phase)
   * @param listener Will be called by the added listener function.
   * @return A JavaScript 0-arg function that can be called to remove the listener added by this method.
   */
  private static native JavaScriptObject addNativeEventListenerImpl(JavaScriptObject target, String eventName, boolean useCapture, EventListener listener) /*-{
    // 1) set up a native wrapper function to use as the event listener
    // NOTE: we're using a wrapper function as an event handler because there's no cross-browser way to get a "bound method" object
    // On newer browsers (IE9+) we could have used Function.prototype.bind, e.g.:
    // var handlerFcn =  $entry(@com.google.gwt.user.client.EventListener::onBrowserEvent(Lcom/google/gwt/user/client/Event;)).bind(listener);
    var handlerFcn = $entry(function (evt) {
      listener.@com.google.gwt.user.client.EventListener::onBrowserEvent(Lcom/google/gwt/user/client/Event;)(evt);
    })
    // 2) attach our listener function in a cross-browser way (idea borrowed from http://javascriptrules.com/2009/07/22/cross-browser-event-listener-with-design-patterns/ )
    if (target.addEventListener) {
      // All standards-compliant browsers
      target.addEventListener(eventName, handlerFcn, useCapture);
      return function() {
        target.removeEventListener(eventName, handlerFcn, useCapture);
      };
    }
    else {
      // Non-standards-compliant browsers
      eventName = 'on' + eventName;
      if (target.attachEvent) {
        // Internet Explorer before IE9
        // NOTE: these functions don't support the useCapture arg; we could probably hack it by calling setCapture on the element (see https://msdn.microsoft.com/en-us/library/ms536742(v=vs.85).aspx), but it doesn't seem worth it
        target.attachEvent(eventName, handlerFcn);
        return function() {
          target.detachEvent(eventName, handlerFcn);
        };
      }
      else {
        // last resort (very old browsers, pre-DOM2)
        // TODO: check that the element contains these fields?  Maybe the problem is that the JSO is not a Node that supports events, in which case we probably want to throw an exception
        // TODO: must create a backup of the original handler, if any (see com.google.gwt.user.client.impl.WindowImpl.initWindowResizeHandler)
        target[eventName] = handlerFcn;
        return function() {
          if (target[eventName] === handlerFcn)
            target[eventName] = null;
        };
      }
    }
  }-*/;

}
