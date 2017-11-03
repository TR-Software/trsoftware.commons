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

package solutions.trsoftware.commons.client.util;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.shared.GWT;

import java.util.Date;

import static solutions.trsoftware.commons.client.util.TimeUnit.*;

/**
 * Date: Apr 29, 2008 Time: 10:36:55 PM
 *
 * @author Alex
 */
public class TimeUtils {
  // NOTE: this code was written before we introduced the TimeUnit class, but it's not worth rewriting this until we internationalize the entire app (and have pluralization available in GWT 1.6+)
  private static final int ONE_SECOND = (int)SECONDS.millis;
  private static final int ONE_MINUTE = (int)MINUTES.millis;
  private static final int ONE_HOUR = (int)HOURS.millis;
  private static final int ONE_DAY = (int)DAYS.millis;
  private static final double ONE_MONTH = MONTHS.millis;
  private static final double ONE_YEAR = YEARS.millis;

  /**
   * Generates a human-readable relative time value like "15 minutes ago"
   * from the given millisecond time difference value.
   */
  public static String generateRelativeTimeElapsedString(double millis) {
    return generateRelativeTimeElapsedString(millis, "second", "seconds", "minute", "minutes", "hour", "hours", "day", "days", "month", "months", "year", "years");
  }

  /**
   * Generates a human-readable relative time value like "15 minutes ago"
   * from the given millisecond time difference value.
   */
  public static String generateRelativeTimeElapsedString(double millis, String second, String seconds, String minute, String minutes, String hour, String hours, String day, String days, String month, String months, String year, String years) {
    return timeIntervalToString(millis, second, seconds, minute, minutes, hour, hours, day, days, month, months, year, years) + " ago";
  }

  /**
   * Generates a human-readable relative time value like "15 minutes ago"
   * from the given millisecond time difference value.
   */
  public static String timeIntervalToString(double millis) {
    return timeIntervalToString(millis, "second", "seconds", "minute", "minutes", "hour", "hours", "day", "days", "month", "months", "year", "years");
  }

  /**
   * Generates a human-readable relative time value like "15 minutes ago"
   * from the given millisecond time difference value.
   */
  public static String timeIntervalToString(double millis, String second, String seconds, String minute, String minutes, String hour, String hours, String day, String days, String month, String months, String year, String years) {
    double number;
    String unit;
    // NOTE: this code was written before we introduced the TimeUnit class, but it's not worth rewriting this until we internationalize the entire app (and have pluralization available in GWT 1.6+)
    if (millis < (10*ONE_SECOND) && second.length() > 2) {
      return "moments";
    }
    else if (millis < (2*ONE_SECOND)) {
      number = 1; unit = second;  // keeping number separate from label for easier i18n later
    }
    else if (millis < ONE_MINUTE) {
      number = (millis / ONE_SECOND); unit = seconds;
    }
    else if (millis < (2*ONE_MINUTE)) {
      number = 1; unit = minute;
    }
    else if (millis < ONE_HOUR) {
      number = (millis / ONE_MINUTE); unit = minutes;
    }
    else if (millis < (2*ONE_HOUR)) {
      number = 1; unit = hour;
    }
    else if (millis < ONE_DAY) {
      number = (millis / ONE_HOUR); unit = hours;
    }
    else if (millis < (2*ONE_DAY)) {
      number = 1; unit = day;
    }
    else if (millis < 2*ONE_MONTH) {
      number = (millis / ONE_DAY); unit = days;
    }
//    else if (millis < (2*ONE_MONTH)) {
//      number = 1; unit = month;
//    }
    else if (millis < ONE_YEAR) {
      number = (millis / ONE_MONTH); unit = months;
    }
    else if (millis < (2*ONE_YEAR)) {
      number = 1; unit = year;
    }
    else {
      number = (millis / ONE_YEAR); unit = years;
    }

    return "" + (int)number + " " + unit;
  }


  /**
   * A bridge between the long millisecond time representation in Java and
   * the UTC double fractional seconds representation used in Python and other 
   * languages.
   */
  public static double millisToSeconds(long millis) {
    return millisToSeconds((double)millis);
  }

  /**
   * A bridge between the double millisecond time representation in GWT/JS and
   * the UTC double fractional seconds representation used in Python and other
   * languages.
   */
  public static double millisToSeconds(double millis) {
    return MILLISECONDS.to(SECONDS, millis);
  }

  /**
   * A bridge between the long millisecond time representation in Java and
   * the UTC double fractional seconds representation used in Python and other
   * languages.
   */
  public static long secondsToMillisLong(double seconds) {
    return Math.round(secondsToMillis(seconds));
  }

  /**
   * A bridge between the long millisecond time representation in Java and
   * the UTC double fractional seconds representation used in Python and other
   * languages.
   */
  public static double secondsToMillis(double seconds) {
    return SECONDS.to(MILLISECONDS, seconds);
  }

  /** Returns the time as a double to avoid slow GWT long emulation */
  public static double currentTimeMillis() {
    if (GWT.isClient())
      return Duration.currentTimeMillis();
    else
      return System.currentTimeMillis();
  }

  public static int getCalendarYear(double millis) {
    return dateFromMillis(millis).getYear() + 1900;
  }

  public static Date dateFromMillis(double millis) {
    // TODO: GWT needs a way to create a Date object without casting to long
    return new Date((long)millis);
  }

  /**
   * @param seconds the same value as {@link Date#getTime()} but expressed in seconds instead of millis.
   */
  public static Date dateFromSeconds(double seconds) {
    return dateFromMillis(secondsToMillis(seconds));
  }

  /**
   * @return the value of {@link Date#getTime()} expressed in seconds instead of millis.
   */
  public static double dateToSeconds(Date date) {
    return millisToSeconds(date.getTime());
  }

}
