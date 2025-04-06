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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;

/**
 * An {@link Anchor} that can be in one of two states: "on" or "off".
 *
 * @author Alex
 */
public class ToggleAnchor extends Composite {
  private final Anchor anchor;
  /** The current state of the toggle */
  private boolean on;

  private final String onMessage;
  private final String offMessage;
  private ToggleHandler handler;

  /**
   * @param startOn Upon creation, calls handleToggleEvent with this argument.
   */
  public ToggleAnchor(String onMessage, String offMessage, boolean startOn) {
    this.onMessage = onMessage;
    this.offMessage = offMessage;
    on = startOn;
    anchor = Widgets.anchor(on ? onMessage : offMessage,
        event -> toggle(!on));
    initWidget(anchor);
  }

  /**
   * @return the current state of the toggle
   */
  public boolean isOn() {
    return on;
  }

  /**
   * Sets the toggle state of this widget without triggering the toggle handler.
   *
   * @return {@code true} if the state was changed
   */
  public boolean setState(boolean toggleOn) {
    boolean changed = on != toggleOn;
    on = toggleOn;
    anchor.setText(on ? onMessage : offMessage);
    return changed;
  }

  private void toggle(boolean toggleOn) {
    // TODO(3/29/2025): maybe don't need this method (replace usages with setState, or setValue(toggleOn, true) and move handleToggleEvent there)
    setState(toggleOn);
    handleToggleEvent(toggleOn);
  }

  /* TODO(3/29/2025): maybe extend HasValue<Boolean> (like in ImageToggleButton),
       and replace handleToggleEvent/ToggleHandler with the ValueChangeEvent/ValueChangeHandler
       mechanism of HasValue.setValue(T, boolean)
   */

  /**
   * Subclasses should either override this legacy method to handle changes in this widget's toggle state
   * or specify a handler with {@link #onToggle(ToggleHandler)}.
   *
   * @param toggleOn
   */
  protected void handleToggleEvent(boolean toggleOn) {
    if (handler != null)
      handler.handleToggleEvent(toggleOn);
  }

  /**
   * Assigns a toggle handler that can be used instead of overriding {@link #handleToggleEvent(boolean)}.
   */
  public ToggleAnchor onToggle(ToggleHandler handler) {
    this.handler = handler;
    return this;
  }

  public interface ToggleHandler {
    void handleToggleEvent(boolean toggleOn);
  }
}
