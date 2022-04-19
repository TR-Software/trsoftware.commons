/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.core.shared.GwtIncompatible;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.Date;

import static solutions.trsoftware.commons.shared.util.TimeUnit.*;

/**
 * @since Apr 29, 2008
 * @author Alex
 *
 * @see TimeUnit
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
    return timeIntervalToString(millis, "second", "seconds", "minute", "minutes", "hour", "hours", "day", "days", "month", "months", "year", "years") + " ago";
  }

  /**
   * Generates a human-readable relative time value like "15 minutes ago"
   * from the given millisecond time difference value.
   */
  public static String generateRelativeTimeElapsedString(double millis, String second, String seconds, String minute, String minutes, String hour, String hours, String day, String days, String month, String months, String year, String years) {
    return timeIntervalToString(millis, second, seconds, minute, minutes, hour, hours, day, days, month, months, year, years) + " ago";
  }

  /**
   * Generates a human-readable relative time interval value like "15 minutes" from the given milliseconds value.
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
   *
   * @see #secondsToInstant(double)
   */
  public static long secondsToMillisLong(double seconds) {
    // TODO(11/26/2020): why Math.round? it's probably more accurate to just cast to long (and avoid the value being rounded up to the next millis)
    return Math.round(secondsToMillis(seconds));
  }

  /**
   * A bridge between the long millisecond time representation in Java and
   * the UTC double fractional seconds representation used in Python and other
   * languages.
   *
   * @see #secondsToInstant(double)
   */
  public static double secondsToMillis(double seconds) {
    return SECONDS.to(MILLISECONDS, seconds);
  }

  /**
   * @return the system time as a {@code double} (to avoid slow {@code long} emulation in GWT)
   */
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

  /**
   * @return {@code true} iff the difference between {@code startTime} and {@code currentTime} is greater than {@code duration}
   */
  public static boolean isElapsed(double duration, double startTime, double currentTime) {
    return (currentTime - startTime) > duration;
  }

  /**
   * WARNING: it's preferable to use {@link #isElapsed(double, double, double)} in client-side GWT code to avoid the performance penalty of GWT's {@code long} emulation.
   * @return {@code true} iff the difference between {@code startTime} and {@code currentTime} is greater than {@code duration}
   */
  public static boolean isElapsed(long duration, long startTime, long currentTime) {
    return (currentTime - startTime) > duration;
  }

  /**
   * Rounds down the given time value to the nearest increment of the given duration.
   * <p>
   * This method takes the idea behind the {@code truncatedTo(TemporalUnit)} methods provided by various
   * {@link java.time} API classes, and applies it to raw epoch millis values.
   *
   * <h3>Examples:</h3>
   * Given a {@code unitDurationMillis} equal to 900000ms (15 minutes), this method will produce the following results:
   * <table border=1>
   *   <tr>
   *     <th>argument</th>
   *     <th>result</th>
   *   </tr>
   *   <tr>
   *     <td>{@code 1569578905651} (<i>2019-09-27T10:08:25.651Z</i>)</td>
   *     <td>{@code 1569578400000} (<i>2019-09-27T10:00:00Z</i>)</td>
   *   </tr>
   *   <tr>
   *     <td>{@code 1569579319512} (<i>2019-09-27T10:15:19.512Z</i>)</td>
   *     <td>{@code 1569579300000} (<i>2019-09-27T10:15:00Z</i>)</td>
   *   </tr>
   *   <tr>
   *     <td>{@code 1569580442849} (<i>2019-09-27T10:34:02.849Z</i>)</td>
   *     <td>{@code 1569580200000} (<i>2019-09-27T10:30:00Z</i>)</td>
   *   </tr>
   *   <tr>
   *     <td>{@code 1569581980047} (<i>2019-09-27T10:59:40.047Z</i>)</td>
   *     <td>{@code 1569581100000} (<i>2019-09-27T10:45:00Z</i>)</td>
   *   </tr>
   * </table>
   * <em>NOTE:</em> the above results are identical to those produced by {@link Instant#truncatedTo(TemporalUnit)}
   * and {@link LocalDateTime#truncatedTo(TemporalUnit)} (given a {@link TemporalUnit} that represents 15 minutes).
   *
   * @param timeMillis the time to truncate (round down)
   * @param unitDurationMillis the modulus for the rounding
   * @return the highest value {@code T} such that {@code T <= timeMillis} and {@code T % unitDurationMillis == 0}
   *
   * @see Instant#truncatedTo(TemporalUnit)
   * @see LocalDateTime#truncatedTo(TemporalUnit)
   */
  public static long truncateTime(long timeMillis, long unitDurationMillis) {
    // TODO: test this with negative values (might need to use floorMod here)
    return timeMillis - (timeMillis % unitDurationMillis);
  }

  /**
   * Converts a floating-point epoch seconds value to an {@link Instant} with {@link Instant#ofEpochSecond(long, long)}.
   *
   * @param seconds the floating-point "Unix time" value to convert
   * @return the corresponding {@link Instant}
   */
  @GwtIncompatible
  public static Instant secondsToInstant(double seconds) {
    long whole = (long)seconds;
    long nanos = (long)TimeUnit.SECONDS.to(TimeUnit.NANOSECONDS, seconds - whole);
    return Instant.ofEpochSecond(whole, nanos);
  }
}
