package solutions.trsoftware.commons.client.event;

import com.google.gwt.core.client.GWT;
import solutions.trsoftware.commons.client.exceptions.CommonsUncaughtExceptionHandler;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Date: Nov 21, 2007 Time: 3:58:29 AM
 *
 * @author Alex
 */
public class ListenerSet<T> extends LinkedHashSet<DataChangeListener<T>> {

  public void fireChange(final DataChangeEvent<T> event) {
    // make a copy to avoid a ConcurrentModificationException if listeners try to remove themselves
    // don't worry, this seems like a fast operation in JS
    ArrayList<DataChangeListener<T>> copyOfListeners = new ArrayList<DataChangeListener<T>>(this.size());
    for (final DataChangeListener<T> listener : this)
      copyOfListeners.add(listener);

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
