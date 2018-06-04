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
