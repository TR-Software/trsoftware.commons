/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.commons.client.event;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.TextBox;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Allows cycling through the commands entered into a textbox using the up/down arrow keys, similar to a command-line console.
 *
 * @author Alex
 * @since 4/15/2021
 */
public class CommandBuffer implements KeyDownHandler {

  private final TextBox textBox;

  private final ArrayList<String> commandHistory = new ArrayList<>();
  private ListIterator<String> commandIterator;

  /**
   * Binds a new instance to the given textbox, attaching key-down handlers to it for the command history cycling behavior.
   */
  public CommandBuffer(TextBox textBox) {
    this.textBox = textBox;
    textBox.addKeyDownHandler(this);
  }

  @Override
  public void onKeyDown(KeyDownEvent event) {
    switch (event.getNativeKeyCode()) {
      case KeyCodes.KEY_UP:
        if (!commandHistory.isEmpty()) {
          if (commandIterator == null) {
            // will be iterating over a temporary view of the command history,
            // which includes the current un-entered input as the last element
            ArrayList<String> values = new ArrayList<>(commandHistory);
            values.add(textBox.getText());
            // however, we start the iteration on the first command, not the current textbox value
            commandIterator = values.listIterator(commandHistory.size());
          }
          if (commandIterator.hasPrevious())
            textBox.setText(commandIterator.previous());
        }
        break;
      case KeyCodes.KEY_DOWN:
        if (commandIterator != null)
          if (commandIterator.hasNext())
            textBox.setText(commandIterator.next());
          else
            // stop the iteration
            commandIterator = null;
        break;
      case KeyCodes.KEY_ENTER:
        commandHistory.add(textBox.getText());
        commandIterator = null;
        break;
    }
  }

  public TextBox getTextBox() {
    return textBox;
  }

  public ArrayList<String> getCommandHistory() {
    return commandHistory;
  }

  /**
   * Removes all commands from this buffer, and resets its iteration state.
   */
  public void clear() {
    commandHistory.clear();
    commandIterator = null;
  }
}
