package solutions.trsoftware.commons.shared.event;

import java.util.EventListener;

/**
 * A generic change listener.
 *
 * @author Alex
 * @since 12/2/2017
 */
public interface ChangeListener extends EventListener {
  void onChange();
}
