package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a {@code public} {@link #isInitialized()} method because {@link Composite#getWidget()} has {@code protected} access.
 * @author Alex
 * @since 3/6/2018
 */
public class SmartComposite extends Composite {

  /**
   * @return {@code true} iff this {@link Composite} has a widget (i.e. {@link #initWidget(Widget)} has already been called)
   */
  public boolean isInitialized() {
    return super.getWidget() != null;
  }
}
