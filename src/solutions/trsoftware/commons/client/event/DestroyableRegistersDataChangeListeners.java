package solutions.trsoftware.commons.client.event;

import solutions.trsoftware.commons.client.util.Destroyable;

/**
 * Allows registered DataChangeListeners to be automatically removed when the
 * subclass is destroyed.
 *
 * Oct 31, 2009
 *
 * @author Alex
 */
public interface DestroyableRegistersDataChangeListeners extends Destroyable {
  <T> void registerDataChangeListener(ListenerSet<T> listenerSet, DataChangeListener<T> listener);
}
