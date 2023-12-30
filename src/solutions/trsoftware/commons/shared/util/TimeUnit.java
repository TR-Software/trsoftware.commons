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

package solutions.trsoftware.commons.shared.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import solutions.trsoftware.commons.shared.util.compare.RichComparable;
import solutions.trsoftware.commons.shared.util.text.SharedNumberFormat;

/**
 * A GWT-friendly, simplified, version of {@link java.util.concurrent.TimeUnit}.
 *
 * Notes:
 * <ul>
 *   <li>
 *     starting with Java 1.8, there's a better implementation of this concept available in the standard library:
 *     {@link java.time.temporal.ChronoUnit}, but that's not currently available in GWT (as of GWT 2.8.2)
 *   </li>
 *   <li>
 *     units greater than {@link #WEEKS} are not well-defined (their values depend on the calendar being used),
 *     so we define their durations based on the approximation of 365.2425 days per year
 *     (same as {@link java.time.temporal.ChronoUnit}); for an in-depth explanation of this topic see the linked article
 *   </li>
 * </ul>
 *
 * @see java.util.concurrent.TimeUnit
 * @see java.time.temporal.ChronoUnit
 * @see <a href="https://www.washingtonpost.com/news/speaking-of-science/wp/2017/02/24/think-you-know-how-many-days-are-in-a-year-think-again/">"Think you know how many days are in a year? Think again."</a>
 * @author Alex
 */
public enum TimeUnit implements RichComparable<TimeUnit> {
  /**
   * Unit that represents the concept of a nanosecond, the smallest supported unit of time.
   * For the ISO calendar system, it is equal to the 1,000,000,000th part of the second unit.
   * @see java.util.concurrent.TimeUnit#NANOSECONDS
   * @see java.time.temporal.ChronoUnit#NANOS
   */
  NANOSECONDS(1e-6, "ns"),
  /**
   * Unit that represents the concept of a microsecond.
   * For the ISO calendar system, it is equal to the 1,000,000th part of the second unit.
   * @see java.util.concurrent.TimeUnit#MICROSECONDS
   * @see java.time.temporal.ChronoUnit#MICROS
   */
  MICROSECONDS(1e-3, "μs"),
  /**
   * Unit that represents the concept of a millisecond.
   * For the ISO calendar system, it is equal to the 1000th part of the second unit.
   * @see java.util.concurrent.TimeUnit#MILLISECONDS
   * @see java.time.temporal.ChronoUnit#MILLIS
   */
  MILLISECONDS(1, "ms"),
  /**
   * Unit that represents the concept of a second.
   * For the ISO calendar system, it is equal to the second in the SI system
   * of units, except around a leap-second.
   * @see java.util.concurrent.TimeUnit#SECONDS
   * @see java.time.temporal.ChronoUnit#SECONDS
   */
  SECONDS(MILLISECONDS.millis * 1000, "s"),
  /**
   * Unit that represents the concept of a minute.
   * For the ISO calendar system, it is equal to 60 seconds.
   * @see java.util.concurrent.TimeUnit#MINUTES
   * @see java.time.temporal.ChronoUnit#MINUTES
   */
  MINUTES(SECONDS.millis * 60, "m"),
  /**
   * Unit that represents the concept of an hour.
   * For the ISO calendar system, it is equal to 60 minutes.
   * @see java.util.concurrent.TimeUnit#HOURS
   * @see java.time.temporal.ChronoUnit#HOURS
   */
  HOURS(MINUTES.millis * 60, "h"),
  /**
   * Unit that represents the concept of a day.
   * For the ISO calendar system, it is the standard day from midnight to midnight.
   * The estimated duration of a day is {@code 24 Hours}.
   * @see java.util.concurrent.TimeUnit#DAYS
   * @see java.time.temporal.ChronoUnit#DAYS
   */
  DAYS(HOURS.millis * 24, "d"),
  /**
   * Unit that represents the concept of a week.
   * For the ISO calendar system, it is equal to 7 days.
   * @see java.time.temporal.ChronoUnit#WEEKS
   */
  WEEKS(DAYS.millis * 7, "w"),
  /**
   * Unit that represents the concept of a month.
   * For the ISO calendar system, the length of the month varies by month-of-year.
   * The estimated duration of a month is one twelfth of {@code 365.2425 Days}.
   * @see java.time.temporal.ChronoUnit#MONTHS
   */
  MONTHS(SECONDS.toMillis(2629746 /* 31556952 / 12 */), "M"),
  /**
   * Unit that represents the concept of a year.
   * For the ISO calendar system, it is equal to 12 months.
   * The estimated duration of a year is {@code 365.2425 Days}.
   * @see java.time.temporal.ChronoUnit#YEARS
   * @see <a href="https://www.washingtonpost.com/news/speaking-of-science/wp/2017/02/24/think-you-know-how-many-days-are-in-a-year-think-again/">"Think you know how many days are in a year? Think again."</a>
   */
  YEARS(SECONDS.toMillis(31556952), "y");

  /** The number of milliseconds in this time unit */
  public final double millis;

  /**
   * The <a href="https://en.wikipedia.org/wiki/International_System_of_Units#Prefixes">SI abbreviation</a>
   * for this unit if it's 1 second or less, otherwise a conventional abbreviation resembling (but not necessarily
   * compatible with) the format specifiers used by used by {@link java.text.SimpleDateFormat},
   * {@link com.google.gwt.i18n.client.DateTimeFormat}, or {@link java.time.Duration#toString()}
   */
  public final String abbreviation;

  TimeUnit(double millis, String abbreviation) {
    this.millis = millis;
    this.abbreviation = abbreviation;
  }

  /**
   * Convert the given time duration in the given unit to this unit.
   * @param sourceUnit the unit of the <tt>sourceDuration</tt> argument
   * @param sourceDuration the time duration in the given <tt>sourceUnit</tt>
   * @return the converted duration in this unit, which could be a fraction.
   */
  public double from(TimeUnit sourceUnit, double sourceDuration) {
    return convert(sourceDuration, sourceUnit);
  }

  /**
   * Convert the given time duration in the given unit to this unit.
   * <p>
   * <em>Note:</em> this method is exactly the same as {@link #from(TimeUnit, double)} with the parameters reversed,
   * to match the API of
   * {@link java.util.concurrent.TimeUnit#convert(long, java.util.concurrent.TimeUnit) java.util.concurrent.TimeUnit}
   *
   * @param sourceUnit the unit of the <tt>sourceDuration</tt> argument
   * @param sourceDuration the time duration in the given <tt>sourceUnit</tt>
   * @return the converted duration in this unit, which could be a fraction.
   */
  public double convert(double sourceDuration, TimeUnit sourceUnit) {
    if (sourceUnit == this)
      return sourceDuration;  // if it's the same unit, return the same value without doing any lossy arithmetic
    return (sourceUnit.millis * sourceDuration) / millis;
  }

  /**
   * Convert the give time duration in this unit to the given unit.
   * @param targetUnit the unit of the desired result<tt>sourceDuration</tt> argument
   * @param duration the time duration in this unit.
   * @return the converted duration in <tt>targetUnit</tt>, which could be a fraction.
   */
  public double to(TimeUnit targetUnit, double duration) {
    return targetUnit.from(this, duration);
  }

  // the following methods provide shorthand notation for the most common conversions
  public double toMillis(double duration) {
    return to(MILLISECONDS, duration);
  }

  public double fromMillis(double duration) {
    return from(MILLISECONDS, duration);
  }

  public double toNanos(double duration) {
    return to(NANOSECONDS, duration);
  }

  public double fromNanos(double duration) {
    return from(NANOSECONDS, duration);
  }

  /**
   * @return The name of this unit in lowercase, in its singular or plural form, depending on whether the given
   * duration is equal to 1.
   */
  public String getPrettyName(double duration) {
    String ret = name().toLowerCase();
    if (duration == 1)
      ret = ret.substring(0, ret.length()-1);  // we want the singular form of the name, so strip the trailing "s"
    return ret;
  }

  /**
   * Returns a human-readable string representing the given duration expressed in this time unit,
   * with the output unit chosen automatically based on the magnitude of the duration, and 3 decimal places
   * of precision.
   * <p>
   * Equivalent to {@link #toString(double, int) toString}{@code (duration, 3)}
   * <h3>Examples:
   * <table>
   *   <tr>
   *     <th>Inputs</th>
   *     <th>Output</th>
   *   </tr>
   *   <tr><td>{@code YEARS.toString(0.9)}</td><td>"328.718 d"</td></tr>
   *   <tr><td>{@code MINUTES.toString(1)}</td><td>"1 m"</td></tr>
   *   <tr><td>{@code MINUTES.toString(0.9)}</td><td>"54 s"</td></tr>
   *   <tr><td>{@code SECONDS.toString(0.9)}</td><td>"900 ms"</td></tr>
   *   <tr><td>{@code MILLISECOND.toString(1)}</td><td>"1 ms"</td></tr>
   *   <tr><td>{@code MILLISECONDS.toString(0.9)}</td><td>"900 μs"</td></tr>
   *   <tr><td>{@code MICROSECONDS.toString(0.9)}</td><td>"900 ns"</td></tr>
   *   <tr><td>{@code NANOSECONDS.toString(1.1236)}</td><td>"1.124 ns"</td></tr>
   * </table>
   *
   * @param duration a duration expressed in this time unit
   * @see #toString(double, int)
   * @see #format(double, int)
   */
  public String toString(double duration) {
    return toString(duration, 3);
  }

  /**
   * Returns a human-readable string representing the given duration expressed in this time unit,
   * with the output unit chosen automatically based on the magnitude of the duration.
   * <h3>Examples:
   * <table>
   *   <tr>
   *     <th>Inputs</th>
   *     <th>Output</th>
   *   </tr>
   *   <tr><td>{@code YEARS.toString(1.1, 4)}</td><td>"401.7668 d"</td></tr>
   *   <tr><td>{@code YEARS.toString(0.9, 3)}</td><td>"328.718 d"</td></tr>
   *   <tr><td>{@code MONTHS.toString(0.9, 4)}</td><td>"27.3932 d"</td></tr>
   *   <tr><td>{@code MINUTES.toString(1, 4)}</td><td>"1 m"</td></tr>
   *   <tr><td>{@code MINUTES.toString(0.9, 4)}</td><td>"54 s"</td></tr>
   *   <tr><td>{@code SECONDS.toString(0.9, 4)}</td><td>"900 ms"</td></tr>
   *   <tr><td>{@code MILLISECOND.toString(1, 4)}</td><td>"1 ms"</td></tr>
   *   <tr><td>{@code MILLISECONDS.toString(0.9, 4)}</td><td>"900 μs"</td></tr>
   *   <tr><td>{@code MICROSECONDS.toString(1.1, 4)}</td><td>"1.1 μs"</td></tr>
   *   <tr><td>{@code MICROSECONDS.toString(0.9, 4)}</td><td>"900 ns"</td></tr>
   *   <tr><td>{@code NANOSECONDS.toString(1.136, 2)}</td><td>"1.14 ns"</td></tr>
   * </table>
   *
   * @param duration a duration expressed in this time unit
   * @param maxFractionDigits the maximum fractional digits to include in the output
   * @see #toString(double)
   * @see #format(double, int)
   */
  public String toString(double duration, int maxFractionDigits) {
    return format(toNanos(duration), maxFractionDigits);
  }

  // TODO: maybe move the following Stopwatch.toString copycat methods to DurationFormat, and provide juc.TimeUnit-compatible versions

  /**
   * Returns a human-readable string representing the given nanoseconds duration, with the output unit chosen
   * automatically based on the magnitude of the duration.
   * <h3>Examples:
   * <table>
   *   <tr>
   *     <th>Description</th>
   *     <th>Inputs</th>
   *     <th>Output</th>
   *   </tr>
   *   <tr><td><i>1 year</i></td><td>{@code format(3.1556952E16, 4)}</td><td>"365.2425 d"</td></tr>
   *   <tr><td><i>0.9 months</i></td><td>{@code format(2.3667714E15, 4)}</td><td>"27.3932 d"</td></tr>
   *   <tr><td><i>1 week</i></td><td>{@code format(6.048E14, 4)}</td><td>"7 d"</td></tr>
   *   <tr><td><i>0.9 days</i></td><td>{@code format(7.776E13, 4)}</td><td>"21.6 h"</td></tr>
   *   <tr><td><i>0.9 hours</i></td><td>{@code format(3.24E12, 4)}</td><td>"54 m"</td></tr>
   *   <tr><td><i>1 minute</i></td><td>{@code format(6.0E10, 4)}</td><td>"1 m"</td></tr>
   *   <tr><td><i>0.9 minutes</i></td><td>{@code format(5.4E10, 4)}</td><td>"54 s"</td></tr>
   *   <tr><td><i>1 second</i></td><td>{@code format(1.0E9, 4)}</td><td>"1 s"</td></tr>
   *   <tr><td><i>0.9 seconds</i></td><td>{@code format(9.0E8, 4)}</td><td>"900 ms"</td></tr>
   *   <tr><td><i>1 millisecond</i></td><td>{@code format(1000000.0, 4)}</td><td>"1 ms"</td></tr>
   *   <tr><td><i>1.1 milliseconds</i></td><td>{@code format(1100000.0000000002, 4)}</td><td>"1.1 ms"</td></tr>
   *   <tr><td><i>0.9 milliseconds</i></td><td>{@code format(900000.0000000001, 4)}</td><td>"900 μs"</td></tr>
   *   <tr><td><i>1 microsecond</i></td><td>{@code format(1000.0000000000001, 4)}</td><td>"1 μs"</td></tr>
   *   <tr><td><i>0.9 microseconds</i></td><td>{@code format(900.0000000000001, 4)}</td><td>"900 ns"</td></tr>
   *   <tr><td><i>1 nanosecond</i></td><td>{@code format(1.0, 4)}</td><td>"1 ns"</td></tr>
   *   <tr><td><i>0.9 nanoseconds</i></td><td>{@code format(0.9, 4)}</td><td>"0.9 ns"</td></tr>
   * </table>
   *
   * @param nanos the time value in nanoseconds
   * @param maxFractionDigits the maximum fractional digits to include in the output
   * @see #toString(double, int)
   * @see Stopwatch#toString()
   */
  public static String format(double nanos, int maxFractionDigits) {
    // Note: this code was borrowed from Guava's Stopwatch.toString
    TimeUnit unit = chooseUnit(nanos);
    double value = nanos / NANOSECONDS.convert(1, unit);
    SharedNumberFormat numberFormat = new SharedNumberFormat(maxFractionDigits);  // corresponds to Guava's Platform.formatCompact4Digits(value)
    return numberFormat.format(value) + " " + unit.abbreviation;
  }

  /**
   * Helper for {@link #format(double, int)}.  Chooses the best time unit to represent the given nanoseconds duration.
   */
  @VisibleForTesting
  static TimeUnit chooseUnit(double nanos) {
    // Note: this code was borrowed from Guava's Stopwatch.chooseUnit
    if (DAYS.convert(nanos, NANOSECONDS) >= 1) {
      return DAYS;
    }
    if (HOURS.convert(nanos, NANOSECONDS) >= 1) {
      return HOURS;
    }
    if (MINUTES.convert(nanos, NANOSECONDS) >= 1) {
      return MINUTES;
    }
    if (SECONDS.convert(nanos, NANOSECONDS) >= 1) {
      return SECONDS;
    }
    if (MILLISECONDS.convert(nanos, NANOSECONDS) >= 1) {
      return MILLISECONDS;
    }
    if (MICROSECONDS.convert(nanos, NANOSECONDS) >= 1) {
      return MICROSECONDS;
    }
    return NANOSECONDS;
  }

}
