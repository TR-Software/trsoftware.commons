/*
 * Copyright 2018 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

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
 * <p>
 * NOTE(1/31/2019): this native mapping of Backspace to "Back" still affects many of the major browsers on Windows,
 * including IE, FF, and Opera (but not Chrome).
 *
 * @author Alex - Oct 1, 2009
 */
public class BackspaceBlocker implements Event.NativePreviewHandler {

  protected BackspaceBlocker() {
    // should only be instantiable via GWT.create
  }

  /**
   * Cancels the given event if it's the key press event for {@code Backspace} on any element other than {@code textarea} or {@code input}.
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
