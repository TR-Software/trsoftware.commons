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

import solutions.trsoftware.commons.shared.util.text.SharedNumberFormat;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

/**
 * Represents a duration of time expressed in a particular {@link TimeUnit}. Instances are immutable and thread-safe.
 *
 * All methods inherited from {@link Number} return the number of milliseconds represented by this {@link TimeValue}.
 * The {@link #compareTo(TimeValue)} method also compares the number of milliseconds.
 * However, the {@link #equals(Object)} and {@link #hashCode()} methods consider both the {@link #value} and {@link #unit}
 * (<b>not</b> the millis value).
 *
 * @author Alex
 * @since 11/10/2017
 */
public class TimeValue extends Number implements Comparable<TimeValue> {

  private double value;

  private TimeUnit unit;

  private static transient ThreadLocal<SharedNumberFormat> threadLocalNumberFormat = ThreadLocal.withInitial(
      () -> new SharedNumberFormat(3)
  );

  public TimeValue(double value, TimeUnit unit) {
    requireNonNull(unit, "unit");
    this.value = value;
    this.unit = unit;
  }

  private TimeValue() { } // default constructor to support Serializable

  public double getValue() {
    return value;
  }

  public TimeUnit getUnit() {
    return unit;
  }

  public TimeValue to(TimeUnit newUnit) {
    return new TimeValue(unit.to(newUnit, value), newUnit);
  }

  public double toMillis() {
    return unit.toMillis(value);
  }

  public static TimeValue ofMillis(double millis) {
    return new TimeValue(millis, TimeUnit.MILLISECONDS);
  }

  /**
   * This method should only be used serverside, to avoid the performance penalty of GWT's {@code long} emulation.
   * @return {@link #toMillis()} as a {@code long}
   */
  public long toLongMillis() {
    return (long)toMillis();
  }

  /**
   * @return a new instance that represents the sum of {@code this} and the given argument.  The result
   * will have the same {@link #unit} as {@code this}.
   */
  public TimeValue add(TimeValue value) {
    return add(value.toMillis());
  }
  
  /**
   * @return a new instance that represents the difference of {@code this} and the given argument.  The result
   * will have the same {@link #unit} as {@code this}.
   */
  public TimeValue subtract(TimeValue value) {
    return add(-value.toMillis());
  }
  
  /**
   * @param value the addend, expressed in milliseconds
   * @return a new instance that represents the sum of {@code this} and the given number of milliseconds.  The result
   * will have the same {@link #unit} as {@code this}.
   */
  public TimeValue add(double value) {
    return new TimeValue(unit.fromMillis(this.toMillis() + value), unit);
  }
  

  /**
   * @return {@code true} iff the difference between {@code startTimeMillis} and {@code currentTimeMillis} is greater than
   * the duration represented by this instance.
   */
  public boolean isElapsed(double startTimeMillis, double currentTimeMillis) {
    return TimeUtils.isElapsed(toMillis(), startTimeMillis, currentTimeMillis);
  }

  /**
   * NOTE: it's preferable to use {@link #isElapsed(double, double)} in compiled GWT code to avoid the performance penalty of GWT's {@code long} emulation.
   * @return {@code true} iff the difference between {@code startTimeMillis} and {@code currentTimeMillis} is greater than
   * the duration represented by this instance.
   */
  public boolean isElapsed(long startTimeMillis, long currentTimeMillis) {
    return TimeUtils.isElapsed(toLongMillis(), startTimeMillis, currentTimeMillis);
  }

  @Override
  public String toString() {
    return toString(value, unit);
  }

  public String toString(SharedNumberFormat format) {
    return toString(value, unit, format);
  }

  public String toString(int maxFractionDigits) {
    return toString(value, unit, new SharedNumberFormat(maxFractionDigits));
  }

  public static String toString(double value, TimeUnit unit) {
    return toString(value, unit, defaultNumberFormat());
  }

  @Nonnull
  private static String toString(double value, TimeUnit unit, SharedNumberFormat format) {
    return format.format(value) + " " + unit.getPrettyName(value);
  }

  private static SharedNumberFormat defaultNumberFormat() {
    return threadLocalNumberFormat.get();
  }

  /**
   * @return {@code true} iff the given object is an instance of {@link TimeValue} with the same {@link #value}
   * and {@link #unit}. To check whether two instances represent the same length of time, compare the results of
   * {@link #toMillis()} instead.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TimeValue timeValue = (TimeValue)o;

    if (Double.compare(timeValue.value, value) != 0) return false;
    return unit == timeValue.unit;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(value);
    result = (int)(temp ^ (temp >>> 32));
    result = 31 * result + unit.hashCode();
    return result;
  }

  @Override
  public int compareTo(TimeValue o) {
    return Double.compare(toMillis(), o.toMillis());
  }

  /**
   * @return the number of milliseconds represented by this {@link TimeValue}
   */
  @Override
  public int intValue() {
    return (int)toMillis();
  }

  /**
   * @return the number of milliseconds represented by this {@link TimeValue}
   */
  @Override
  public long longValue() {
    return toLongMillis();
  }

  /**
   * @return the number of milliseconds represented by this {@link TimeValue}
   */
  @Override
  public float floatValue() {
    return (float)toMillis();
  }

  /**
   * @return the number of milliseconds represented by this {@link TimeValue}
   */
  @Override
  public double doubleValue() {
    return toMillis();
  }
}
