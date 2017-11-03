package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.event.CapsLockDetector;

import static solutions.trsoftware.commons.client.widgets.Widgets.flowPanel;
import static solutions.trsoftware.commons.client.widgets.Widgets.html;

/**
 * Uses a {@link CapsLockDetector} to show {@link #lblCapsLockWarning} whenever the contained {@link PasswordTextBox}
 * receives a keystroke while the "Caps Lock" key is on.  Also provides a convenient {@link #passwordInputWidget}
 * that contains both {@link #txtPassword} and {@link #lblCapsLockWarning}.
 *
 * @author Alex, 10/17/2017
 */
public class FormWithUsernameAndPassword extends Composite {

  protected PasswordTextBox txtPassword = new PasswordTextBox();
  protected Label lblCapsLockWarning = html("Your <em>Caps Lock</em> key is on", "fieldErrorMsg");
  protected final FlowPanel passwordInputWidget = flowPanel(txtPassword, lblCapsLockWarning);
  protected TextBox txtUsername = new TextBox();

  public FormWithUsernameAndPassword() {
    // show a "Caps Lock" warning when entering password
    txtPassword.addKeyPressHandler(new CapsLockDetector() {
      @Override
      protected void onCapsLockStatus(boolean on) {
        lblCapsLockWarning.setVisible(on);
      }
    });
    lblCapsLockWarning.setVisible(false);
  }

  /**
   * @return the widget that should be focused when this form is displayed.
   */
  public FocusWidget getFocusTarget() {
    return txtUsername;
  }

}
