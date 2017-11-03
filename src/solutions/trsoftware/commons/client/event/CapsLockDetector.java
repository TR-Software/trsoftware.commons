package solutions.trsoftware.commons.client.event;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;

/**
 * Determines whether the "Caps Lock" key is on every time a {@link KeyPressEvent} with a capital letter is received.
 * Invokes {@link #onCapsLockStatus(boolean)} each time such a keystroke is received.
 */
public abstract class CapsLockDetector implements KeyPressHandler {

  public void onKeyPress(KeyPressEvent event) {
    char charCode = event.getCharCode();
    // can assume caps lock is on if we get a capital letter but the shift key isn't pressed
    boolean capsLockOn = Character.isUpperCase(charCode) && !event.isShiftKeyDown();
    onCapsLockStatus(capsLockOn);
  }

  /**
   * Allows subclass to take an action depending on the status of the user's "Caps Lock" key.
   * @param on whether the "Caps Lock" key is on.
   */
  protected abstract void onCapsLockStatus(boolean on);
}
