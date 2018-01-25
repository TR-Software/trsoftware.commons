package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * @author Alex
 * @since 11/17/2017
 */
public interface HasFocusTarget extends IsWidget {
  /**
   * @return the child widget that should be focused when this widget is displayed.
   */
  FocusWidget getFocusTarget();
}
