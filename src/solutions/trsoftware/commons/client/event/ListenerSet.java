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

import com.google.gwt.core.client.GWT;
import solutions.trsoftware.commons.client.exceptions.CommonsUncaughtExceptionHandler;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * @author Alex
 * @since Nov 21, 2007
 * @deprecated use {@link Events#BUS} instead.
 */
public class ListenerSet<T> extends LinkedHashSet<DataChangeListener<T>> {

  public void fireChange(final DataChangeEvent<T> event) {
    // make a copy to avoid a ConcurrentModificationException if listeners try to remove themselves
    // don't worry, this seems like a fast operation in JS
    ArrayList<DataChangeListener<T>> copyOfListeners = new ArrayList<DataChangeListener<T>>(this);

    // make sure that unchecked exceptions while executing a listener don't affect the execution of other listeners
    for (final DataChangeListener<T> listener : copyOfListeners) {
      try {
        listener.onChange(event);
      }
      catch (Throwable e) {
        // we don't let an exception interrupt this loop, but since this would
        // prevent it from percolating up to the TyperacerUncaughtExceptionHandler,
        // we invoke the TyperacerUncaughtExceptionHandler here manually
        ((CommonsUncaughtExceptionHandler)GWT.getUncaughtExceptionHandler()).handleException(e, false);
      }
    }
  }

  @Override
  public boolean add(DataChangeListener<T> changeListener) {
    boolean result = super.add(changeListener);
    changeListener.addedToSet(this);
    return result;
  }

  @Override
  public boolean remove(Object o) {
    boolean removed = super.remove(o);
    if (removed && o instanceof DataChangeListener)
      ((DataChangeListener)o).removedFromSet(this);
    return removed;
  }
}
