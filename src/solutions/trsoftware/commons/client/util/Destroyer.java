package solutions.trsoftware.commons.client.util;

import com.google.gwt.user.client.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * A delegate that facilitates other classes implementing the Destroyable interface.
 * Provides "mix-in" logic.
 * 
 * @author Alex
 */
public class Destroyer implements Destroyable {

  /** The actions to be performed when this view goes away (removing listeners, etc) */
  private List<Command> cleanupActions;

  private boolean destroyed = false;

  /** Cleans up everything created by this view.  This logic will be executed only once. */
  public void destroy() {
    if (destroyed)
      return;
    destroyed = true;
    if (cleanupActions != null) {
      for (Command cleanupAction : cleanupActions) {
        cleanupAction.execute();
      }
    }
  }

  public void addCleanupAction(Command command) {
    if (cleanupActions == null)
      cleanupActions = new ArrayList<Command>();
    cleanupActions.add(command);
  }
}