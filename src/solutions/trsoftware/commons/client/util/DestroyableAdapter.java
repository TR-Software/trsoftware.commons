package solutions.trsoftware.commons.client.util;

import com.google.gwt.user.client.Command;

/**
 * Date: Oct 9, 2008 Time: 8:55:24 PM
 *
 * @author Alex
 */
public class DestroyableAdapter implements Destroyable {
  private Destroyer delegate = new Destroyer();

  public void destroy() {
    delegate.destroy();
  }

  public void addCleanupAction(Command command) {
    delegate.addCleanupAction(command);
  }
}
