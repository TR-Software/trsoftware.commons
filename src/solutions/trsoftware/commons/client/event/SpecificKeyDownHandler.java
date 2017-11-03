package solutions.trsoftware.commons.client.event;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Command;

/**
 * Executes the given command when handling a key down event for the specified
 * key code.  The command can be specified in two ways: passing
 * a Command object or overriding the execute method.
 */
public class SpecificKeyDownHandler implements KeyDownHandler, Command {
  private final int specifiedKeyCode;
  private final Command onKeyCodeMatch;

  /**
   * @param specifiedKeyCode should be one of the constants defined in {@link com.google.gwt.event.dom.client.KeyCodes}
   */
  public SpecificKeyDownHandler(int specifiedKeyCode) {
    this.specifiedKeyCode = specifiedKeyCode;
    this.onKeyCodeMatch = null;
  }

  /**
   * @param specifiedKeyCode should be one of the constants defined in {@link com.google.gwt.event.dom.client.KeyCodes}
   * @param onKeyCodeMatch command to execute when the a key down event with the specific key code is detected.
   */
  public SpecificKeyDownHandler(int specifiedKeyCode, Command onKeyCodeMatch) {
    this.specifiedKeyCode = specifiedKeyCode;
    this.onKeyCodeMatch = onKeyCodeMatch;
  }

  public final void onKeyDown(KeyDownEvent event) {
    if (event.getNativeKeyCode() == specifiedKeyCode)
      execute();
  }

  /** Sublcasses should override this method if they don't pass a Command to the contructor */
  public void execute() {
    onKeyCodeMatch.execute();
  }
}
