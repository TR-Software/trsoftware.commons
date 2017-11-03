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
