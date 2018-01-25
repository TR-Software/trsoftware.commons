package solutions.trsoftware.commons.shared.event;

import java.util.EventListener;

/**
 * A generic change listener.
 *
 * @author Alex
 * @since 12/2/2017
 */
public interface SizeChangeListener extends EventListener {

  void onSizeChange(int oldSize, int newSize);
}
