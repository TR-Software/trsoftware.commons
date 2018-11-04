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

package solutions.trsoftware.commons.client.util;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import solutions.trsoftware.commons.shared.util.TimeUnit;

import static com.google.gwt.core.client.Duration.currentTimeMillis;

/**
 * Date: May 5, 2008 Time: 8:45:00 PM
 *
 * @author Alex
 */
public class TitleBarFlasher {

  /** Default milliseconds between flashes */
  private static final int DEFAULT_FLASH_DELAY = 1000;  // milliseconds between flashes

  /** Backs up the default page title in case it needs to be restored later */
  public static String DEFAULT_WINDOW_TITLE = Window.getTitle();

  /** To make sure that only one instance is flashing at any given time */
  private static TitleBarFlasher activeInstance;

  /** Milliseconds between flashes */
  private int flashDelay = DEFAULT_FLASH_DELAY;

  /** This will constitute a "flash off" */
  private String flashOffTitle = DEFAULT_WINDOW_TITLE;

  /** This will constitute a "flash on" */
  private String flashOnTitle = "";

  private double flashEndTime;

  private boolean flashOn = false;

  private Timer flashingTimer = new Timer() {
    public void run() {
      if (currentTimeMillis() > flashEndTime)
        stopFlashing();
      else if (flashOn)
        Window.setTitle(flashOnTitle);
      else
        Window.setTitle(flashOffTitle);
      flashOn = !flashOn;
    }
  };

  public TitleBarFlasher() {
  }

  /**
   * This constructor can be used to provide a custom delay between flashes.
   *
   * @param flashDelayMillis milliseconds between flashes
   */
  public TitleBarFlasher(int flashDelayMillis) {
    this.flashDelay = flashDelayMillis;
  }

  /**
   * @return the default title bar text
   */
  public String getFlashOffTitle() {
    return flashOffTitle;
  }

  /**
   * Sets the default title bar text.
   * @param flashOffTitle the default title bar text
   * @return this instance, for method chaining
   */
  public TitleBarFlasher setFlashOffTitle(String flashOffTitle) {
    this.flashOffTitle = flashOffTitle;
    return this;
  }

  /**
   * @return the alternate title bar text
   */
  public String getFlashOnTitle() {
    return flashOnTitle;
  }

  /**
   * Sets the default title bar text.
   * @param flashOnTitle the default title bar text
   * @return this instance, for method chaining
   */
  public TitleBarFlasher setFlashOnTitle(String flashOnTitle) {
    this.flashOnTitle = flashOnTitle;
    return this;
  }

  /**
   * Flashes the {@link #flashOnTitle} until {@link #stopFlashing()} is called
   * (actually the flashing will be cancelled after 1 month, but that shouldn't matter in any practical usage scenario).
   * @return this instance, for method chaining
   */
  public TitleBarFlasher startFlashing() {
    return startFlashing(flashOnTitle);
  }

  /**
   * Flash the given text until {@link #stopFlashing()} is called
   * (actually the flashing will be cancelled after 1 month, but that shouldn't matter in any practical usage scenario).
   * @return this instance, for method chaining
   */
  public TitleBarFlasher startFlashing(String textDuringFlash) {
    return startFlashing(textDuringFlash, (int)TimeUnit.MONTHS.toMillis(1));
  }

  /**
   * Flash the given text for the given duration of time
   * @return this instance, for method chaining
   */
  public TitleBarFlasher startFlashing(String textDuringFlash, int durationMillis) {
    // make sure that no other instance is currently active
    if (activeInstance != null)
      activeInstance.stopFlashing();
    activeInstance = this;
    flashOnTitle = textDuringFlash;
    flashEndTime = currentTimeMillis() + durationMillis;
    flashingTimer.scheduleRepeating(flashDelay);
    return this;
  }

  public void stopFlashing() {
    flashingTimer.cancel();
    Window.setTitle(flashOffTitle);
    flashEndTime = 0;
    activeInstance = null;
  }
}
