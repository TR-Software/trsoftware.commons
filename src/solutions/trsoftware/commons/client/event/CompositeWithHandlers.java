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

package solutions.trsoftware.commons.client.event;

import com.google.gwt.user.client.ui.Composite;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static solutions.trsoftware.commons.shared.util.CollectionUtils.tryForEach;

/**
 * A convenience superclass which facilitates managing handler registrations on a global event bus in a way that
 * doesn't leak memory when the widget is removed from the DOM.
 * <p>
 * Handler registrations are deferred until the widget becomes attached to the DOM ({@link #onLoad}),
 * and the registered handlers are automatically removed when the widget becomes detached ({@link #onUnload}).
 * This process repeats on every attach/detach cycle (i.e. if the widget ever becomes attached again,
 * the same handlers will be automatically registered again and removed when it becomes detached).
 *
 * @see #registerEventHandlerOnLoad(Event.Type, Object)
 * @see MultiHandlerRegistration
 * @author Alex
 */
public abstract class CompositeWithHandlers extends Composite {

  private final List<DeferredRegistration> deferredRegistrations = new ArrayList<>();

  /**
   * The given handler will be {@linkplain EventBus#addHandler(Event.Type, Object) added}
   * to the {@linkplain Events#BUS global event bus} in {@link #onLoad()} and
   * {@linkplain HandlerRegistration#removeHandler() removed} in {@link #onUnload()}.
   * <p>
   * This add/remove cycle will repeat ad-infinitum, or until interrupted by calling {@link HandlerRegistration#removeHandler()}
   * on the object returned by this method.
   *
   * @param <H> the handler type
   * @param eventType the event type
   * @param handler the handler instance
   * @return memento that can be used to stop the add/remove cycle for the given handler
   */
  protected <H> Remover registerEventHandlerOnLoad(Event.Type<H> eventType, H handler) {
    return registerEventHandlerOnLoad(() -> Events.BUS.addHandler(eventType, handler));
  }

  /**
   * Similar to {@link #registerEventHandlerOnLoad(Event.Type, Object)}, but allows using a custom event bus
   * instead of {@link Events#BUS}.
   * <p>
   * The given function should perform the equivalent of {@link EventBus#addHandler(Event.Type, Object)}.
   *
   * @param registrar adds the desired handler to the desired event bus
   * @return memento that can be used to stop the add/remove cycle for the given handler
   */
  protected Remover registerEventHandlerOnLoad(Supplier<HandlerRegistration> registrar) {
    return addDeferredRegistration(DeferredRegistration.fromSupplier(registrar));
  }

  private Remover addDeferredRegistration(DeferredRegistration deferredRegistration) {
    deferredRegistrations.add(deferredRegistration);
    return () -> deferredRegistrations.remove(deferredRegistration);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    tryForEach(deferredRegistrations, DeferredRegistration::addHandler);
  }

  /**
   * This is a great place to clean up (e.g. remove model listeners)
   * and avoid memory leaks when the view is thrown away.
   */
  @Override
  protected void onUnload() {
    super.onUnload();
    tryForEach(deferredRegistrations, DeferredRegistration::removeHandler);
  }


  private abstract static class DeferredRegistration {
    private HandlerRegistration handlerRegistration;

    /**
     * Internal method invoked from {@link #onLoad()}: adds the encapsulated handler to the event bus.
     */
    final void addHandler() {
      handlerRegistration = doAddHandler();
    }

    /**
     * Internal method invoked from {@link #onUnload()}: removes the encapsulated handler from the event bus.
     */
    final void removeHandler() {
      if (handlerRegistration != null) {
        handlerRegistration.removeHandler();
        handlerRegistration = null;
      }
    }

    /**
     * Adds the desired handler to the event bus.  This method can be overridden to use custom event bus logic.
     *
     * @return the object returned by {@link EventBus#addHandler(Event.Type, Object)}
     */
    abstract HandlerRegistration doAddHandler();

    static DeferredRegistration fromSupplier(Supplier<HandlerRegistration> registrar) {
      return new DeferredRegistration() {
        @Override
        HandlerRegistration doAddHandler() {
          return registrar.get();
        }
      };
    }
  }


  /**
   * A memento that can be used to stop the add/remove cycle for a handler added with {@link #registerEventHandlerOnLoad}
   */
  interface Remover {
    /**
     * Stops the add/remove cycle for the given handler
     */
    boolean remove();
  }

}
