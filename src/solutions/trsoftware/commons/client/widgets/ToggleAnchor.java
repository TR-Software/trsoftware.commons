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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;

/**
 * An Anchor that can be in one of two states. 
 *
 * @author Alex
 */
public abstract class ToggleAnchor extends Composite {
  private Anchor lnk;
  /** The current state of the toggle */
  private boolean on;

  private final String onMessage;
  private final String offMessage;

  /**
   * @param startOn Upon creation, calls handleToggleEvent with this argument.
   */
  public ToggleAnchor(String onMessage, String offMessage, boolean startOn) {
    this.onMessage = onMessage;
    this.offMessage = offMessage;
    lnk = new Anchor(true);
    lnk.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        toggle(!on);
      }
    });
    toggle(startOn);
    initWidget(lnk);
  }

  private void toggle(boolean toggleOn) {
    on = toggleOn;
    lnk.setText(on ? onMessage : offMessage);
    handleToggleEvent(on);
  }

  public abstract void handleToggleEvent(boolean toggleOn);

}
