/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

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

  /**
   * Will be invoked when a {@code keydown} event with the given key code is received.
   * Subclasses must override this method if they don't pass a {@link Command} to the constructor.
   */
  public void execute() {
    if (onKeyCodeMatch != null)
      onKeyCodeMatch.execute();
    else
      throw new UnsupportedOperationException();  // subclasses must override this method if they don't pass a Command to the constructor
  }
}
