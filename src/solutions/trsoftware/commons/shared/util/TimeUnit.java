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

package solutions.trsoftware.commons.shared.util;

/**
 * A GWT-compatible, simplified, version of {@link java.util.concurrent.TimeUnit}.
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
 * @see java.time.temporal.ChronoUnit
 * @see <a href="https://www.washingtonpost.com/news/speaking-of-science/wp/2017/02/24/think-you-know-how-many-days-are-in-a-year-think-again/">"Think you know how many days are in a year? Think again."</a>
 * @author Alex
 */
public enum TimeUnit {
  /**
   * Unit that represents the concept of a nanosecond, the smallest supported unit of time.
   * For the ISO calendar system, it is equal to the 1,000,000,000th part of the second unit.
   * @see java.time.temporal.ChronoUnit#NANOS
   */
  NANOSECONDS(1e-6),
  /**
   * Unit that represents the concept of a millisecond.
   * For the ISO calendar system, it is equal to the 1000th part of the second unit.
   * @see java.time.temporal.ChronoUnit#MILLIS
   */
  MILLISECONDS(1),
  /**
   * Unit that represents the concept of a second.
   * For the ISO calendar system, it is equal to the second in the SI system
   * of units, except around a leap-second.
   * @see java.time.temporal.ChronoUnit#SECONDS
   */
  SECONDS(MILLISECONDS.millis * 1000),
  /**
   * Unit that represents the concept of a minute.
   * For the ISO calendar system, it is equal to 60 seconds.
   * @see java.time.temporal.ChronoUnit#MINUTES
   */
  MINUTES(SECONDS.millis * 60),
  /**
   * Unit that represents the concept of an hour.
   * For the ISO calendar system, it is equal to 60 minutes.
   * @see java.time.temporal.ChronoUnit#HOURS
   */
  HOURS(MINUTES.millis * 60),
  /**
   * Unit that represents the concept of a day.
   * For the ISO calendar system, it is the standard day from midnight to midnight.
   * The estimated duration of a day is {@code 24 Hours}.
   * @see java.time.temporal.ChronoUnit#DAYS
   */
  DAYS(HOURS.millis * 24),
  /**
   * Unit that represents the concept of a week.
   * For the ISO calendar system, it is equal to 7 days.
   * @see java.time.temporal.ChronoUnit#WEEKS
   */
  WEEKS(DAYS.millis * 7),
  /**
   * Unit that represents the concept of a month.
   * For the ISO calendar system, the length of the month varies by month-of-year.
   * The estimated duration of a month is one twelfth of {@code 365.2425 Days}.
   * @see java.time.temporal.ChronoUnit#MONTHS
   */
  MONTHS(SECONDS.toMillis(31556952 / 12)),
  /**
   * Unit that represents the concept of a year.
   * For the ISO calendar system, it is equal to 12 months.
   * The estimated duration of a year is {@code 365.2425 Days}.
   * @see java.time.temporal.ChronoUnit#YEARS
   * @see <a href="https://www.washingtonpost.com/news/speaking-of-science/wp/2017/02/24/think-you-know-how-many-days-are-in-a-year-think-again/">"Think you know how many days are in a year? Think again."</a>
   */
  YEARS(SECONDS.toMillis(31556952));

  /** The number of milliseconds in this time unit */
  public final double millis;

  TimeUnit(double millis) {
    this.millis = millis;
  }

  /**
   * Convert the given time duration in the given unit to this
   * unit.
   * @param sourceUnit the unit of the <tt>sourceDuration</tt> argument
   * @param sourceDuration the time duration in the given <tt>sourceUnit</tt>
   * @return the converted duration in this unit, which could be a fraction.
   */
  public double from(TimeUnit sourceUnit, double sourceDuration) {
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
}
