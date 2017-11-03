package solutions.trsoftware.commons.client.event;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

/**
 * For AJAX applications that make heavy use of keyboard input, the browser's
 * mapping of the backspace key to the Back (one page back in history) action
 * is an annoyance, because the user can type it accidentally when not in
 * a text field.  This native event preview handler disables it.
 *
 * Note: all browsers have Backspace mapped to Back (inlcuding IE, Safari, FF, Opera),
 * at least on Windows.
 *
 * Oct 1, 2009
 *
 * @author Alex
 */
public class BackspaceBlocker implements Event.NativePreviewHandler {

  protected BackspaceBlocker() {
    // should only be instantiable via GWT.create
  }

  /**
   * Whether the given even needs to be canceled (i.e. it's the Backspace
   * keypress event).
   * @return true if the event should be allowed, false if it should be canceled.
   */
  public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
    if (isKeyDownEvent(event)) {
      NativeEvent nativeEvent = event.getNativeEvent();
      if (nativeEvent.getKeyCode() == KeyCodes.KEY_BACKSPACE) {
        // return Window.confirm("Proceed with backspace?" + " key code: " + keyCode + " target: " + event.getTarget().getTagName());
        String targetTag = nativeEvent.getEventTarget().<Element>cast().getTagName();
        if (!"input".equalsIgnoreCase(targetTag) && !"textarea".equalsIgnoreCase(targetTag)) {
          event.cancel(); // suppress the backspace when it's not over an input field or text area
        }
      }
    }
    // pass all other events through undeterred
  }

  protected boolean isKeyDownEvent(Event.NativePreviewEvent event) {
    return event.getTypeInt() == Event.ONKEYDOWN;
  }
}
