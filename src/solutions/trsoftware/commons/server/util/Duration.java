package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.client.util.AbstractDuration;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Similar to GWT's Duration class (a utility class for measuring elapsed time).
 * Implemented in simple Java code so it can be used on both client and server.
 * NOTE: this is not very accurate because most operating systems return time
 * in tens of millis (when you call System.currentTimeMillis).  For better
 * precision use the server-side counterpart NanoDuration.
 */

public class Duration extends AbstractDuration {

  private long start;

  /**
   * Creates a new Duration whose start time is now.
   */
  public Duration() {
    this("");
  }

  /**
   * Creates a new Duration whose start time is now, with a name.
   * The toString method will return "{name} took {duration} {timeUnit}"
   */
  public Duration(String name) {
    this(name, "took");
  }


  /**
   * Creates a new Duration whose start time is now, with a name and action.
   * The toString method will return "{name} {action} {duration} {timeUnit}"
   */
  public Duration(String name, String action) {
    super(action, name);
    start = System.currentTimeMillis();
  }

  /**
   * @return the number of milliseconds that have elapsed since this object was created.
   */
  public double elapsedMillis() {
    return System.currentTimeMillis() - start;
  }

  private final static long TIME_ZONE_OFFSET = new CalDate().getDate().getTime();  // the "zero" date, in millis


  /**
   * If the duration is less than an hour, it will be printed as "MM:SS" or "MM:SS.millis" if printMillis is true,
   * and if greater than an hour, it will be printed as "HH:MM:SS" or "HH:MM:SS.millis" if printMillis is true.
   * If greater than 24 hours, the value will wrap around, and the overflow will not be displayed.
   */
  public String formatAsClockTime(boolean printMillis) {
    return formatAsClockTime((long)elapsedMillis(), printMillis);
  }

  /**
   * If the given duration is less than an hour, it will be printed as "MM:SS" or "MM:SS.millis" if printMillis is true,
   * and if greater than an hour, it will be printed as "HH:MM:SS" or "HH:MM:SS.millis" if printMillis is true.
   * If greater than 24 hours, the value will wrap around, and the overflow will not be displayed.
   */
  public static String formatAsClockTime(long millisDuration, boolean printMillis) {
    Date date = new Date(TIME_ZONE_OFFSET + millisDuration);
    if (TimeUnit.MILLISECONDS.toHours(millisDuration) >= 1) {
      if (printMillis)
        return String.format("%tH:%tM:%tS.%tL", date, date, date, date); // "HH:MM:SS.millis"
      else
        return String.format("%tH:%tM:%tS", date, date, date); // "HH:MM:SS.millis"
    }
    else {
      if (printMillis)
        return String.format("%tM:%tS.%tL", date, date, date); // "MM:SS.millis"
      else
        return String.format("%tM:%tS", date, date); // "MM:SS.millis"
    }
  }


}
