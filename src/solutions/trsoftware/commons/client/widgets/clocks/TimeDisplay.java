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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import solutions.trsoftware.commons.client.bundle.CommonsClientBundleFactory;
import solutions.trsoftware.commons.client.util.time.CountdownTimer;
import solutions.trsoftware.commons.shared.util.StringUtils;

import static solutions.trsoftware.commons.client.widgets.WidgetDecorator.setVisibilityHidden;
import static solutions.trsoftware.commons.client.widgets.Widgets.flowPanel;
import static solutions.trsoftware.commons.client.widgets.Widgets.inlineLabel;

/**
 * Combines several labels in a simple panel: 1 or 2 optional captions (before and after) and
 * {@link #lblTime} in the middle, the latter of which shows a time value of minutes:seconds.
 * Encapsulates the logic to render the time value into a string.
 * The time value is also encapsulated, and updated explicitly by calling {@link #setTime} or implicitly
 * by {@link #setCountdownTimer(CountdownTimer)}, which registers an event change listener on the given timer.
 *
 * @author Alex
 */
public class TimeDisplay extends Composite implements CountdownTimer.TickEvent.Handler, CountdownTimer.StartedEvent.Handler {

  protected final FlowPanel pnlMain;
  protected final Label lblTime;
  private Label lblCaptionBefore;
  private Label lblCaptionAfter;

  /** The minutes value currently being displayed */
  private int minutes;
  /** The seconds value currently being displayed */
  private int seconds;

  private boolean displayLeadingZero = false;

  public TimeDisplay() {
    initWidget(
        pnlMain = flowPanel(
            lblTime = inlineLabel("", CommonsClientBundleFactory.INSTANCE.getCss().time())
        )
    );
    setStyleName(CommonsClientBundleFactory.INSTANCE.getCss().timeDisplay());
    setVisibilityHidden(this, true); // start invisible until the time value is set
  }

  public TimeDisplay(double valueMillis, boolean displayLeadingZero) {
    this();
    setDisplayLeadingZero(displayLeadingZero);
    setTime(valueMillis); // this call should happen after setDisplayLeadingZero for the setting to take effect prior to rendering
  }

  /**
   * Registers an time change listener on the given timer to call {@link #setTime(double)} automatically.
   * @return self, for chaining.
   */
  public TimeDisplay setCountdownTimer(CountdownTimer countdownTimer) {
    countdownTimer.addStartedHandler(this);
    countdownTimer.addTickHandler(this);
    return this;
  }

  @Override
  public void onCountdownStarted(CountdownTimer.StartedEvent event) {
    setTime(event.getTimeRemaining());
  }

  @Override
  public void onTick(CountdownTimer.TickEvent event) {
    setTime(event.getTimeRemaining());
  }

  public void setTime(double millis) {
    if (millis < 0)  // don't show negative values
      millis = 0;
    int totalSeconds = Math.round((float)(millis / 1000d));  // cast to float to avoid GWT's long emulation
    int newMinutes = totalSeconds / 60;
    int newSeconds = totalSeconds % 60;

    updateTimeDisplay(newMinutes, newSeconds);
  }

  public TimeDisplay setCaption(String captionTextLeft) {
    return setCaption(captionTextLeft, null);
  }

  public TimeDisplay setCaption(String captionTextLeft, String captionTextRight) {
    if (captionTextLeft != null) {
      if (!captionTextLeft.endsWith(" "))
        captionTextLeft += " ";  // add a blank space to pad the caption from the time widget
      lblCaptionBefore = createOrUpdateCaptionLabel(lblCaptionBefore, captionTextLeft, 0);
    }
    if (captionTextRight != null) {
      if (!captionTextRight.startsWith(" "))
        captionTextRight = " " + captionTextRight;  // insert a blank space to pad the caption from the time widget
      lblCaptionAfter = createOrUpdateCaptionLabel(lblCaptionAfter, captionTextRight, pnlMain.getWidgetCount());
    }
    return this;
  }

  private Label createOrUpdateCaptionLabel(Label lblCaption, String text, int beforeIndex) {
    if (lblCaption == null)
      pnlMain.insert(lblCaption = inlineLabel(text, CommonsClientBundleFactory.INSTANCE.getCss().timeDisplayCaption()), beforeIndex);
    else
      lblCaption.setText(text);
    return lblCaption;
  }

  public TimeDisplay setHint(String hint) {
    lblTime.setTitle(hint);
    return this;
  }

  public TimeDisplay setDisplayLeadingZero(boolean displayLeadingZero) {
    this.displayLeadingZero = displayLeadingZero;
    return this;
  }

  private void updateTimeDisplay(int newMinutes, int newSeconds) {
    if (newMinutes != minutes || newSeconds != seconds || StringUtils.isBlank(lblTime.getText())) {
      // update the display text only if it's changed or was never displayed before
      String timeString = ":";
      if (newSeconds < 10)
        timeString += "0";
      timeString += newSeconds;
      if (displayLeadingZero || newMinutes > 0)
        timeString = newMinutes + timeString;
      setVisibilityHidden(this, false); // this widget stays invisible until the time value is set for the first time
      lblTime.setText(timeString);
    }
    minutes = newMinutes;
    seconds = newSeconds;
  }
}
