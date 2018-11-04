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

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides utility methods that make working with event handlers a bit less
 * cluttered.
 * 
 * @author Alex
 * @since Apr 3, 2013
 */
public class EventHandlers {

  /**
   * Adds handlers for the Enter and Escape keyboard keys to the given widget.
   * The Enter key is handled with a KeyPress event while Escape is
   * handled with a KeyDown event because WebKit browsers don't generate a KeyPress
   * for Escape.
   * @param onEnter The action to be executed when the Enter key is pressed.
   * @param onEscape The action to be executed when the Escape key is pressed.
   */
  public static <T extends Widget & HasKeyPressHandlers & HasKeyDownHandlers> void addEnterAndEscapeKeyHandlers(
      T widget, final Command onEnter, final Command onEscape) {
    if (onEnter != null)
      widget.addKeyDownHandler(new SpecificKeyDownHandler(KeyCodes.KEY_ENTER, onEnter));
    if (onEscape != null)
      widget.addKeyDownHandler(new SpecificKeyDownHandler(KeyCodes.KEY_ESCAPE, onEscape));
  }

  /**
   * Creates a {@link ClickHandler} from the given {@link Command}
   */
  public static ClickHandler clickHandler(final Command command) {
    return new ClickHandler() {
      public void onClick(ClickEvent event) {
        command.execute();
      }
    };
  }

}

