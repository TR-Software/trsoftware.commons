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

import com.google.gwt.user.client.Command;
import solutions.trsoftware.commons.client.util.DestroyableAdapter;

/**
 * A convenience superclass which allows registered DataChangeListeners to be
 * automatically removed when the subclass is destroyed.
 *
 * @author Alex
 */
public class RegistersDataChangeListeners extends DestroyableAdapter implements DestroyableRegistersDataChangeListeners {

  public <T> void registerDataChangeListener(ListenerSet<T> listenerSet, final DataChangeListener<T> listener) {
    listenerSet.add(listener);
    addCleanupAction(new Command() {
      public void execute() {
        listener.removeFromListenerSet();
      }
    });
  }
}
