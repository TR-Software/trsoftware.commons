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

package solutions.trsoftware.commons.shared.text;

import solutions.trsoftware.commons.shared.util.MathUtils;
import solutions.trsoftware.commons.shared.util.compare.RichComparable;
import solutions.trsoftware.commons.shared.util.text.SharedNumberFormat;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Encapsulates a typing speed, expressed in either {@link Unit#WPM WPM} or {@link Unit#CPM CPM},
 * and provides operations to calculate it and convert between the units.
 *
 * @author Alex, 10/30/2017
 */
public class TypingSpeed extends Number implements RichComparable<TypingSpeed> {

  public enum Unit {
    /** Characters per minute */
    CPM {
      @Override
      public double to(Unit toFormat, double value, Language language) {
        return (toFormat == this) ? value : cpmToWpm(value, language);
      }

      /**
       * @param language ignored, could pass {@code null}
       */
      @Override
      public double timeMillis(int charsTyped, double speed, Language language) {
        return cpmToTime(charsTyped, speed);
      }

      /**
       * @param language ignored, could pass {@code null}
       */
      @Override
      public double calcSpeed(int charsTyped, double timeMillis, Language language) {
        return calcCpm(charsTyped, timeMillis);
      }
    },
    /** Words per minute */
    WPM {
      @Override
      public double to(Unit toFormat, double value, Language language) {
        return (toFormat == this) ? value : wpmToCpm(value, language);
      }

      @Override
      public double timeMillis(int charsTyped, double speed, Language language) {
        return wpmToTime(charsTyped, speed, language);
      }

      @Override
      public double calcSpeed(int charsTyped, double timeMillis, Language language) {
        return calcWpm(charsTyped, timeMillis, language);
      }
    };

    /**
     * Converts a value expressed in this {@link Unit} to the given {@link Unit}.
     */
    public abstract double to(Unit toFormat, double value, Language language);

    /**
     * @return the time (in millis) required to type the given number of chars at the given speed expressed in this {@link Unit}.
     */
    public abstract double timeMillis(int charsTyped, double speed, Language language);

    /**
     * Calculates the typing speed expressed in this {@link Unit}
     */
    public abstract double calcSpeed(int charsTyped, double timeMillis, Language language);
  }

  /**
   * The precision (number of decimal fractional digits) to be used for
   * {@link #toString()}, {@link #equals(Object)}, {@link #hashCode()}, and {@link #compareTo(TypingSpeed)}
   */
  public static final int MAX_PRECISION = 8;

  private static transient SharedNumberFormat defaultFormatter;

  public static SharedNumberFormat getDefaultFormatter() {
    if (defaultFormatter == null)
      defaultFormatter = new SharedNumberFormat(MAX_PRECISION);
    return defaultFormatter;
  }

  /**
   * The typing speed in {@link Unit#CPM}. We internally represent all speeds as CPM so that their values are canonical
   * and instances are mutually comparable.
   */
  private double cpm;

  /** This field is needed to convert between the {@linkplain Unit units} */
  @Nonnull
  private Language language;

  /** Default constructor to support serialization */
  private TypingSpeed() {
  }

  /** Initializes instance from a computed value in the given {@link Unit} */
  public TypingSpeed(double value, Unit unit, Language language) {
    Objects.requireNonNull(language, "language");
    // TODO: throw exception if value too high (such that scaledCpm() > Long.MAX_VALUE)?
    this.cpm = unit.to(Unit.CPM, value, language);
    this.language = language;
  }

  /**
   * Computes the typing speed from the given parameters.
   * @param charsTyped the number of characters typed
   * @param timeMillis the time taken to type those chars
   */
  public TypingSpeed(int charsTyped, double timeMillis, Language language) {
    this(calcCpm(charsTyped, timeMillis), Unit.CPM, language);
  }

  public double getSpeed(Unit unit) {
    return Unit.CPM.to(unit, cpm, language);
  }

  @Override
  public int intValue() {
    return Math.round(floatValue());
  }

  @Override
  public long longValue() {
    return Math.round(cpm);
  }

  @Override
  public float floatValue() {
    return (float)cpm;
  }

  @Override
  public double doubleValue() {
    return cpm;
  }

  /**
   * @return The string representation of this typing speed in {@link Unit#WPM WPM}, rounded to a maximum of
   * {@link #MAX_PRECISION} decimal places.  Example: {@code "123.45 WPM"}
   */
  @Override
  public String toString() {
    Unit unit = Unit.WPM;
    return getDefaultFormatter().format(getSpeed(unit)) + " " + unit.name();
  }

  /**
   * Uses a {@linkplain #scaledCpm() scaled integer representation} of the CPM to avoid floating-point arithmetic precision pitfalls
   * (as described in {@link MathUtils#equal(double, double, double)})
   *
   * @return true if the given typing speed matches this one (within {@value #MAX_PRECISION} decimal places of precision),
   * and has the same {@link #language}.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TypingSpeed other = (TypingSpeed)o;
    return language == other.language && scaledCpm() == other.scaledCpm();
  }

  /**
   * Uses a {@linkplain #scaledCpm() scaled integer representation} of the CPM to avoid floating-point arithmetic precision pitfalls
   * (as described in {@link MathUtils#equal(double, double, double)})
   */
  @Override
  public int hashCode() {
    return language.hashCode() + 31 * Long.hashCode(scaledCpm());
  }

  /**
   * Compares the {@linkplain #scaledCpm() scaled integer representation} of the given typing speed to this one.
   */
  @Override
  public int compareTo(@Nonnull TypingSpeed o) {
    return Long.compare(scaledCpm(), o.scaledCpm());
  }

  /**
   * Returns an integer representation of the {@link #cpm CPM}, which can be used to implement {@link #equals(Object)},
   * {@link #hashCode()}, and {@link #compareTo(TypingSpeed)}, such that small differences in WPM/CPM conversion results
   * (due to floating point arithmetic) can be disregarded when comparing two instances of this class.
   *
   * @return <code>{@link #cpm} &times; 10<sup>{@value #MAX_PRECISION}</sup></code>
   */
  private long scaledCpm() {
    return Math.round(cpm * Math.pow(10, MAX_PRECISION));
  }

  /**
   * Calculates typing speed in "characters-per-minute"
   * @param charsTyped the number of characters typed
   * @param timeMillis the time taken to type those chars
   * @return the typing speed as "words-per-minute"
   */
  public static double calcCpm(int charsTyped, double timeMillis) {
    double minutes = timeMillis / 60000d;
    if (minutes == 0)
      return 0;  // avoid a divide by zero, causing a NaN value to be returned
    return (double)charsTyped / minutes;
  }

  /**
   * Calculates typing speed in "words-per-minute", using {@link Language#charsPerWord()} to define "word"
   * @param charsTyped the number of characters typed
   * @param timeMillis the time taken to type those chars
   * @param language used to define "word"
   * @return the typing speed as "words-per-minute"
   */
  public static double calcWpm(int charsTyped, double timeMillis, Language language) {
    return cpmToWpm(calcCpm(charsTyped, timeMillis), language);
  }

  /**
   * Converts the given value from {@link Unit#CPM} to {@link Unit#WPM}.
   */
  public static double cpmToWpm(double cpm, Language language) {
    return cpm / language.charsPerWord();
  }

  /**
   * Converts the given value from {@link Unit#WPM} to {@link Unit#CPM}.
   */
  public static double wpmToCpm(double wpm, Language language) {
    return wpm * language.charsPerWord();
  }

  /** @return The time, in millis, required to type the given number of characters at the given cpm */
  public static double cpmToTime(int charsTyped, double cpm) {
    if (cpm == 0)
      return 0;  // avoids a divide by zero
    return ((double)charsTyped / cpm) * 60000d;
  }

  /** @return The time, in millis, required to type the given number of characters at the given wpm */
  public static double wpmToTime(int charsTyped, double wpm, Language language) {
    return cpmToTime(charsTyped, wpmToCpm(wpm, language));
  }
}
