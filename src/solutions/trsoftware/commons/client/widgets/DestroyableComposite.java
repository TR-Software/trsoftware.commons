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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.web.bindery.event.shared.HandlerRegistration;
import solutions.trsoftware.commons.client.event.DataChangeListener;
import solutions.trsoftware.commons.client.event.DestroyableRegistersDataChangeListeners;
import solutions.trsoftware.commons.client.event.ListenerSet;
import solutions.trsoftware.commons.client.util.Destroyer;

/**
 * A superclass that mixes in both {@link Composite} and {@link DestroyableRegistersDataChangeListeners}.
 *
 * @author Alex
 */
public class DestroyableComposite extends Composite implements DestroyableRegistersDataChangeListeners {

  // TODO(2/17/2016): this can be implemented simply with a handler for AttachEvent

  Destroyer destroyer = new Destroyer();

  /** Cleans up everything created by this view */
  public void destroy() {
    destroyer.destroy();
  }

  public void addCleanupAction(Command command) {
    destroyer.addCleanupAction(command);
  }

  /**
   * This method is called immediately before a widget will be detached from the
   * browser's document.
   */
  protected void onUnload() {
    super.onUnload();
    destroy();
  }

  public <T> void registerDataChangeListener(ListenerSet<T> listenerSet, final DataChangeListener<T> listener) {
    listenerSet.add(listener);
    addCleanupAction(new Command() {
      public void execute() {
        listener.removeFromListenerSet();
      }
    });
  }

  /**
   * Adds a "cleanup" action that will remove the given handler registration when this widget becomes detached.
   */
  public void addHandlerRegistration(HandlerRegistration handlerRegistration) {
    addCleanupAction(new Command() {
      @Override
      public void execute() {
        handlerRegistration.removeHandler();
      }
    });
  }
}