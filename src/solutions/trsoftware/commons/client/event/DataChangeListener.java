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

/**
 * Date: Nov 14, 2007
 * Time: 9:52:44 PM
 *
 * @author Alex
 */
public abstract class DataChangeListener<T> {

  /**
   * References to the listener set that holds this listener - enforces the
   * restriction of each listener only belonging to one listener set.
   */
  private ListenerSet containingSet;

  public abstract void onChange(DataChangeEvent<T> event);

  void addedToSet(ListenerSet listenerSet) {
    if (containingSet != null && containingSet != listenerSet)
      throw new IllegalStateException("Listener already belongs to another ListenerSet.");
    containingSet = listenerSet;
  }

  void removedFromSet(ListenerSet listenerSet) {
    containingSet = null;
  }

  public ListenerSet getListenerSet() {
    return containingSet;
  }

  public void removeFromListenerSet() {
    if (containingSet != null)
      containingSet.remove(this);
  }
}
