package solutions.trsoftware.commons.client.event;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.user.client.Event;

/**
 * @author Alex
 * @since Jul 26, 2013
 */
public class BackspaceBlockerFirefoxImpl extends BackspaceBlocker {

  @Override
  protected boolean isKeyDownEvent(Event.NativePreviewEvent event) {
    // Some users are getting an error in firefox here: Logged JS Error (Firefox): "JavaScriptException: (Error) : Permission denied to access property 'type'" @ BackspaceBlocker.onPreviewNativeEvent(DOMImpl.java:164)
    // since I can't understand what's causing it; will just trap the exception and ignore the event
    try {
      return super.isKeyDownEvent(event);
    }
    catch (JavaScriptException ex) {
      return false;
    }
  }

}
