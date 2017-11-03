/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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

import java.util.ArrayList;
import java.util.List;

/**
 * A convenience superclass which allows registered {@link DataChangeListener}s to be
 * automatically removed when the widget is detached from the DOM (and added back) if it's re-attached later.
 *
 * @author Alex
 */
public class CompositeWithDataChangeListeners extends Composite {

  private static class ListenerRegistration<T> {
    private ListenerSet<T> listenerSet;
    private DataChangeListener<T> listener;

    private ListenerRegistration(ListenerSet<T> listenerSet, DataChangeListener<T> listener) {
      this.listenerSet = listenerSet;
      this.listener = listener;
    }

    private void activate() {
      listenerSet.add(listener);
    }

    private void deactivate() {
      listenerSet.remove(listener);
    }
  }

  private final List<ListenerRegistration> listenerRegistrations = new ArrayList<ListenerRegistration>();

  public <T> void registerDataChangeListener(ListenerSet<T> listenerSet, final DataChangeListener<T> listener) {
    ListenerRegistration<T> reg = new ListenerRegistration<T>(listenerSet, listener);
    reg.activate();
    listenerRegistrations.add(reg);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    for (ListenerRegistration reg : listenerRegistrations) {
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
    for (ListenerRegistration reg : listenerRegistrations) {
      reg.deactivate();
    }
  }
}
