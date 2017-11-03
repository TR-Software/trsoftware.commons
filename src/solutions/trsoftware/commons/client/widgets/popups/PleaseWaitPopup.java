package solutions.trsoftware.commons.client.widgets.popups;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import solutions.trsoftware.commons.client.widgets.LoadingMessage2;

/**
 * @author Alex, 10/18/2017
 */
public class PleaseWaitPopup extends PopupDialog {
  public PleaseWaitPopup(String message, AbstractImagePrototype icon) {
    super(true, icon, "Please Wait", null,
        new LoadingMessage2(message + "...", LoadingMessage2.SpinnerPosition.RIGHT),
        null);
  }
}
