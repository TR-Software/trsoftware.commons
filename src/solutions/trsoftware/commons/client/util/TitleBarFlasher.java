package solutions.trsoftware.commons.client.util;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

/**
 * Date: May 5, 2008 Time: 8:45:00 PM
 *
 * @author Alex
 */
public class TitleBarFlasher {

  private static final int FLASH_DELAY = 1000;  // milliseconds between flashes

  /** This will constitute a "flash off" */
  private static String defaultTitleText = Window.getTitle();

  /** This will constitute a "flash on" */
  private static String alternateTitleText = "";

  private static long flashEndTime;

  private static boolean flashOn = false;

  private static Timer flashingTimer = new Timer() {
    public void run() {
      if (System.currentTimeMillis() > flashEndTime) {
        stopFlashing();
        return;
      }
      if (flashOn)
        Window.setTitle(alternateTitleText);
      else
        Window.setTitle(defaultTitleText);
      flashOn = !flashOn;
    }
  };

  public static String getDefaultTitleText() {
    return defaultTitleText;
  }

  public static void setDefaultTitleText(String defaultTitleText) {
    TitleBarFlasher.defaultTitleText = defaultTitleText;
  }

  /** Flash the given text for the given duration of time */
  public static void startFlashing(String textDuringFlash, int duration) {
    stopFlashing();
    alternateTitleText = textDuringFlash;
    flashEndTime = System.currentTimeMillis() + duration;
    flashingTimer.scheduleRepeating(FLASH_DELAY);
  }

  public static void stopFlashing() {
    flashingTimer.cancel();
    Window.setTitle(defaultTitleText);
    flashEndTime = 0;
    alternateTitleText = "";
  }
}
