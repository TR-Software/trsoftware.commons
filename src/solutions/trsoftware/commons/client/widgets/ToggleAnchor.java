package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;

/**
 * An Anchor that can be in one of two states. 
 *
 * @author Alex
 */
public abstract class ToggleAnchor extends Composite {
  private Anchor lnk;
  /** The current state of the toggle */
  private boolean on;

  private final String onMessage;
  private final String offMessage;

  /**
   * @param startOn Upon creation, calls handleToggleEvent with this argument.
   */
  public ToggleAnchor(String onMessage, String offMessage, boolean startOn) {
    this.onMessage = onMessage;
    this.offMessage = offMessage;
    lnk = new Anchor(true);
    lnk.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        toggle(!on);
      }
    });
    toggle(startOn);
    initWidget(lnk);
  }

  private void toggle(boolean toggleOn) {
    on = toggleOn;
    lnk.setText(on ? onMessage : offMessage);
    handleToggleEvent(on);
  }

  public abstract void handleToggleEvent(boolean toggleOn);

}
