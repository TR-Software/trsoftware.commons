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
 * A GWT-compatible, simplified, version of {@link java.util.concurrent.TimeUnit}
 *
 * @author Alex
 */
public enum TimeUnit {
  MILLISECONDS(1),
  SECONDS(MILLISECONDS.millis * 1000),
  MINUTES(SECONDS.millis * 60),
  HOURS(MINUTES.millis * 60),
  DAYS(HOURS.millis * 24),
  MONTHS(DAYS.millis * 365 / 12),  // 30.4 days in a month, on average
  YEARS(MONTHS.millis * 12),
  NANOSECONDS(1e-6);

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
