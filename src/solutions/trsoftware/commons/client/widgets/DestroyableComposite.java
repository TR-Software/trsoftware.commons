package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
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
}