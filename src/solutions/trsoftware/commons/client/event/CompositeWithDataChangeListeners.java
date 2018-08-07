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

package solutions.trsoftware.commons.client.event;

import com.google.gwt.user.client.ui.Composite;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.HandlerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * A convenience superclass which allows registered {@link DataChangeListener}s to be
 * automatically removed when the widget is detached from the DOM (and added back) if it's re-attached later.
 *
 * @author Alex
 */
public class CompositeWithDataChangeListeners extends Composite {

  private interface DeferredRegistration {
    /**
     * Register the event handler.
     */
    void activate();

    /**
     * Unregister the event handler.
     */
    void deactivate();
  }

  private static class DeferredListenerRegistration<T> implements DeferredRegistration {
    private ListenerSet<T> listenerSet;
    private DataChangeListener<T> listener;

    private DeferredListenerRegistration(ListenerSet<T> listenerSet, DataChangeListener<T> listener) {
      this.listenerSet = listenerSet;
      this.listener = listener;
    }

    @Override
    public void activate() {
      listenerSet.add(listener);
    }

    @Override
    public void deactivate() {
      listenerSet.remove(listener);
    }
  }

  private static class DeferredEventHandlerRegistration<H> implements DeferredRegistration {
    private Event.Type<H> eventType;
    private H handler;
    private HandlerRegistration handlerRegistration;

    public DeferredEventHandlerRegistration(Event.Type<H> eventType, H handler) {
      this.eventType = eventType;
      this.handler = handler;
    }

    @Override
    public void activate() {
      handlerRegistration = Events.BUS.addHandler(eventType, handler);
    }

    @Override
    public void deactivate() {
      if (handlerRegistration != null)
        handlerRegistration.removeHandler();
    }
  }

  private final List<DeferredRegistration> deferredRegistrations = new ArrayList<>();


  public <T> void registerDataChangeListener(ListenerSet<T> listenerSet, final DataChangeListener<T> listener) {
    DeferredListenerRegistration<T> reg = new DeferredListenerRegistration<T>(listenerSet, listener);
    reg.activate();
    deferredRegistrations.add(reg);
  }

  public <H> boolean registerEventHandler(Event.Type<H> eventType, H handler) {
    return deferredRegistrations.add(new DeferredEventHandlerRegistration<H>(eventType, handler));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    for (DeferredRegistration reg : deferredRegistrations) {
      reg.activate();
    }
  }

  /**
   * This is a great place to clean up (e.g. remove model listeners)
   * and avoid memory leaks when the view is thrown away.
   */
  @Override
  protected void onUnload() {
    super.onUnload();
    for (DeferredRegistration reg : deferredRegistrations) {
      reg.deactivate();
    }
  }
}
