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

package solutions.trsoftware.commons.client.widgets.clocks;

import com.google.gwt.user.client.ui.Label;
import solutions.trsoftware.commons.client.widgets.FlashingWidget;

/**
 * Wraps a countdown time display in a flashing widget, which starts flashing when the time value drops below the given
 * threshold, and stops flashing when the time value reaches 0.
 *
 * @author Alex
 */
public class FlashingTimeDisplay extends TimeDisplay {

  private FlashingWidget<Label> flashingDisplay;

  /**
   * When the time drops below this threshold, some sort of warning will be
   * given to the user (e.g. flashing in red)
   */
  private double warningTimeMillis = 0;

  /**
   * When the time drops below the given threshold, some sort of warning will be
   * given to the user (e.g. flashing in red).
   *
   * @param warningTimeMillis Set this to a negative value to not have a warning flasher.
   */
  public FlashingTimeDisplay(double warningTimeMillis) {
    // wrap the display label with a composite FlashingWidget
    int pos = pnlMain.getWidgetIndex(lblTime);  // we have to get the position before creating FlashingWidget, which removes lblTime from pnlMain
    pnlMain.insert(flashingDisplay = new FlashingWidget<Label>(lblTime, 500), pos);
    this.warningTimeMillis = warningTimeMillis;
  }

  public void setTime(double millis) {
    super.setTime(millis);
    flashingDisplay.setFlashing(millis > 0 && millis < warningTimeMillis);
  }

}