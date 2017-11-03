package solutions.trsoftware.commons.shared.util;

/**
 * Formats a time delta (in millis) as {@code "[MINUTES]:[SECONDS]"}
 *
 * @author Alex, 8/3/2017
 */
public class TimeFormatter {

  private static int MILLIS_IN_MINUTE = 60000;

  private boolean showFractionOfSecond;

  public TimeFormatter(boolean showFractionOfSecond) {
    this.showFractionOfSecond = showFractionOfSecond;
  }

  public String format(int millis) {
    int minutes = millis / MILLIS_IN_MINUTE;
    millis -= (minutes * MILLIS_IN_MINUTE);
    float secondsFractional = millis / 1000f;
    int seconds;
    if (!showFractionOfSecond)
      seconds = Math.round(secondsFractional);
    else
      seconds = (int)secondsFractional;
    millis -= (seconds * 1000);
    String ret = "";
    if (minutes < 10)
      ret += '0';  // leading zero
    ret += minutes + ":";
    if (seconds < 10)
      ret += '0';  // leading zero
    ret += seconds;
    if (showFractionOfSecond)
      ret += "." + millis;
    return ret;
  }

}
