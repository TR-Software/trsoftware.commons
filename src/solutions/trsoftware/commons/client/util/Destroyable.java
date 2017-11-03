package solutions.trsoftware.commons.client.util;

import com.google.gwt.user.client.Command;

/**
 * An interface for all UI widgets, data models, etc, that create state
 * which must be torn down before this object is thrown away (e.g. unregistering
 * listeners)
 *
 * @author Alex
 */
public interface Destroyable {
  /** Cleans up everything created by this view */
  void destroy();

  void addCleanupAction(Command command);
}
