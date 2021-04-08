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

package solutions.trsoftware.commons.shared.util.text;

import com.google.common.collect.ImmutableMap;
import solutions.trsoftware.commons.shared.util.TimeUnit;

import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

/**
 * Prints a time delta (in milliseconds) in a human-readable format similar to Python's {@code datetime.timedelta}.
 *
 * <h3>Examples</h3>
 * <ul>
 *   <li>{@code 05:30:00.123}</li>
 *   <li>{@code 1 day, 02:30:02}</li>
 *   <li>{@code 2 years, 3 days, 01:00:02.123}</li>
 * </ul>
 *
 * The number of decimal places and how 0-values are handled is configured via the various constructor parameters.
 *
 * <h3>Synchronization</h3>
 * Because this class uses {@link SharedNumberFormat} (which wraps {@link java.text.DecimalFormat}), the same threading
 * considerations apply: it is recommended to create separate format instances for each thread.
 * If multiple threads access a format concurrently, it must be synchronized externally.
 *
 * @see java.time.Duration#toString()
 * @see <a href="https://stackoverflow.com/q/266825/1965404">Other ways to format a duration in Java</a>
 * @author Alex, 8/3/2017
 */
public class DurationFormat {

  /**
   * Default instances of this class will use this value for the {@code minRequired} argument to the
   * {@link DurationFormat#DurationFormat(Component, int)} and {@link DurationFormat#DurationFormat(Component, int, int)}
   * constructors.
   */
  public static final Component DEFAULT_MIN_COMPONENT = Component.HOURS;


  /**
   * The most-significant component to be printed (even if its value is 0)
   */
  private final Component minRequired;
  private final EnumMap<Component, SharedNumberFormat> componentFormats = new EnumMap<>(Component.class);

  /**
   * Full constructor that allows specifying all the formatting parameters.
   * <h3>Examples:</h3>
   * <ul>
   *   <li>{@code new DurationFormat(Component.MINUTES, 3, 3).format(1.0)} &rarr; {@code "00:00.001"} (prints the full milliseconds value)</li>
   *   <li>{@code new DurationFormat(Component.MINUTES, 1, 3).format(500.0)} &rarr; {@code "00:00.5"}</li>
   *   <li>{@code new DurationFormat(Component.HOURS, 3, 3).format(500.0)} &rarr; {@code "00:00:00.500"}</li>
   *   <li>{@code new DurationFormat(Component.YEARS, 0, 0).format(1.0)} &rarr; {@code "0 years, 0 days, 00:00:00"}</li>
   * </ul>
   * NOTE: will use {@link RoundingMode#HALF_UP} rounding for seconds.
   *
   * @param minRequired the most-significant element to be printed even if its value is 0
   * @param minFractionDigits the minimum number of decimal places to print for seconds
   * @param maxFractionDigits the maximum number of decimal places to print for seconds
   */
  public DurationFormat(Component minRequired, int minFractionDigits, int maxFractionDigits) {
    this.minRequired = minRequired;
    initComponentFormats(minFractionDigits, maxFractionDigits);
  }

  /**
   * Shortcut for {@link #DurationFormat(Component, int, int)} to render seconds with a fixed number of decimal places.
   *
   * <h3>Examples:</h3>
   * <ul>
   *   <li>{@code new DurationFormat(Component.MINUTES, 3).format(1.0)} &rarr; {@code "00:00.001"} (prints the full milliseconds value)</li>
   *   <li>{@code new DurationFormat(Component.MINUTES, 1).format(500.0)} &rarr; {@code "00:00.5"}</li>
   *   <li>{@code new DurationFormat(Component.YEARS, 0).format(1.0)} &rarr; {@code "0 years, 0 days, 00:00:00"}</li>
   *   <li>{@code new DurationFormat(Component.HOURS, 2).format(500.0)} &rarr; {@code "00:00:00.50"}</li>
   * </ul>
   *
   * @param minRequired the most-significant element to be printed even if its value is 0
   * @param decimalPlaces the number of decimal places to print for seconds
   *   (will use {@link RoundingMode#HALF_UP} rounding)
   * @see #DurationFormat(Component, int, int)
   */
  public DurationFormat(Component minRequired, int decimalPlaces) {
    this(minRequired, decimalPlaces, 0);
  }

  /**
   * Shortcut for {@link #DurationFormat(Component, int)} setting {@link #minRequired} to {@link #DEFAULT_MIN_COMPONENT}.
   *
   * <h3>Examples:</h3>
   * <ul>
   *   <li>{@code new DurationFormat(3).format(1.0)} &rarr; {@code "00:00:00.001"} (prints the full milliseconds value)</li>
   *   <li>{@code new DurationFormat(0).format(1.0)} &rarr; {@code "00:00:00"} (milliseconds rounded to nearest second)</li>
   *   <li>{@code new DurationFormat(2).format(500.0)} &rarr; {@code "00:00:00.50"}</li>
   *   <li>{@code new DurationFormat(1).format(YEARS.toMillis(2) + 5_123)} &rarr; {@code "2 years, 00:00:05.1"}</li>
   *   <li>{@code new DurationFormat(0).format(YEARS.toMillis(2) + 5_123)} &rarr; {@code "2 years, 00:00:05"}</li>
   *   <li>{@code new DurationFormat(Component.HOURS, 2).format(500.0)} &rarr; {@code "00:00:00.50"}</li>
   * </ul>
   *
   * @param decimalPlaces the number of decimal places to print for seconds
   *   (will use {@link RoundingMode#HALF_UP} rounding)
   * @see #DurationFormat(Component, int, int)
   */
  public DurationFormat(int decimalPlaces) {
    this(DEFAULT_MIN_COMPONENT, decimalPlaces);
  }

  /**
   * Default constructor, setting {@link #minRequired} to {@link #DEFAULT_MIN_COMPONENT}
   * and using exactly 3 decimal places of precision for seconds (i.e. always printing the full milliseconds)
   *
   * <h3>Examples:</h3>
   * <ul>
   *   <li>{@code new DurationFormat().format(0.0)} &rarr; {@code "00:00:00.000"}</li>
   *   <li>{@code new DurationFormat().format(500.0)} &rarr; {@code "00:00:00.500"}</li>
   *   <li>{@code new DurationFormat().format(YEARS.toMillis(2) + 5_123)} &rarr; {@code "2 years, 00:05.123"}</li>
   * </ul>
   * NOTE: will use {@link RoundingMode#HALF_UP} rounding for milliseconds.
   *
   * @see #DurationFormat(Component, int, int)
   */
  public DurationFormat() {
    this(DEFAULT_MIN_COMPONENT, 3);
  }

  private void initComponentFormats(int minFractionDigits, int maxFractionDigits) {
    SharedNumberFormat fmt1 = new SharedNumberFormat("#");
    SharedNumberFormat fmt2 = new SharedNumberFormat("00");
    for (Component component : Component.values()) {
      switch (component) {
        case SECONDS:
          componentFormats.put(component, new SharedNumberFormat(2, minFractionDigits, maxFractionDigits, false));
          break;
        case MINUTES:
        case HOURS:
          componentFormats.put(component, fmt2);
          break;
        default:
          componentFormats.put(component, fmt1);
      }
    }
  }

  /**
   * @param millis the duration (in milliseconds);  NOTE: negative values not supported (will use the absolute value
   *   of the argument if it's negative)
   * @return the string representation of the given duration
   */
  public String format(double millis) {
    StringBuilder out = new StringBuilder();
    format(millis, out);
    return out.toString();
  }

  /**
   * Same as {@link #format(double)}, but appends the result to the given {@link StringBuilder}, rather than
   * constructing a new string.
   */
  public void format(double millis, StringBuilder buf) {
    millis = Math.abs(millis);  // our algorithm doesn't work for negative inputs
    Component[] components = Component.values();
    double[] values = new double[components.length];

    /*
    NOTE: this code is a bit complicated because we're supporting rendering seconds with arbitrary precision,
    which adds 2 extra passes to deal with propagating overflow to the left.
    For example: 59_999 millis could be rounded to "60.00", in which case we need to set seconds to "00.00" and
    propagate the 1 minute to the left, and so forth
     */
    for (int i = 0; i < components.length; i++) {
      Component component = components[i];
      TimeUnit timeUnit = component.timeUnit;
      double value = timeUnit.fromMillis(millis);
      if (i < components.length - 1) {
        value = Math.floor(value);  // round down all values except for the smallest time unit (which will contain the remainder)
        values[i] = value;
      } else {
        assert component == Component.SECONDS;
        String secondsStr = getNumberFormat(component).format(value);
        // the "seconds" component might overflow into "minutes" due to rounding (e.g. 59_999 millis could be rounded to "60.00")
        values[i] = Double.parseDouble(secondsStr); // use the rounded value for this component; the overflow will be propagated in the next loop
      }
      millis -= timeUnit.toMillis(value);
    }
    // now propagate any overflow carries from right to left
    for (int i = components.length - 1; i >= 0; i--) {
      Component component = components[i];
      TimeUnit thisUnit = component.timeUnit;
      if (i > 0) {
        // check for overflow into the previous component
        TimeUnit prevUnit = components[i - 1].timeUnit;
        double overflow = thisUnit.to(prevUnit, values[i]);
        if (overflow >= 1) {
          double units = Math.floor(overflow);
          values[i - 1] += units;
          // update own value to the remainder of the overflow
          values[i] -= prevUnit.to(thisUnit, units);
          assert values[i] >= 0;
        } else {
          break;  // done propagating all the overflows
        }
      }
    }
    // now render the components
    for (int i = 0; i < components.length; i++) {
      Component component = components[i];
      TimeUnit timeUnit = component.timeUnit;
      double value = values[i];
      if (value > 0 || isRequired(component)) {
        // emit this value
        SharedNumberFormat fmt = getNumberFormat(component);
        switch (component) {
          case YEARS:
          case DAYS:
            buf.append(fmt.format(value)).append(' ').append(timeUnit.getPrettyName(value)).append(", ");
            break;
          case HOURS:
          case MINUTES:
            buf.append(fmt.format(value)).append(':');
            break;
          case SECONDS:
            buf.append(fmt.format(value));
        }
      }
    }
  }

  private SharedNumberFormat getNumberFormat(Component component) {
    return componentFormats.get(component);
  }

  private boolean isRequired(Component component) {
    return component.ordinal() >= minRequired.ordinal();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('{');
    for (Component component : Component.values()) {
      char c = component.conversionChar;
      if (isRequired(component))
        sb.append(c);
      else
        sb.append(Character.toLowerCase(c));
      sb.append('(').append(getNumberFormat(component).getPattern()).append(')');
    }
    sb.append('}');
    return sb.toString();
  }

  private static final ThreadLocal<DurationFormat> defaultInstance = ThreadLocal.withInitial(DurationFormat::new);
  private static final ThreadLocal<DurationFormat> defaultInstanceNoMillis = ThreadLocal.withInitial(() -> new DurationFormat(0));

  /**
   * Factory method for obtaining a cached thread-local instance for the most-common configurations.
   *
   * @param printMillis if {@code true}, will return an instance created with the default constructor ({@link
   *     #DurationFormat() new DurationFormat()}), otherwise {@link #DurationFormat(int) new DurationFormat(0)}
   * @return a cached thread-local instance for the selected option
   */
  public static DurationFormat getDefaultInstance(boolean printMillis) {
    if (printMillis)
      return defaultInstance.get();
    else
      return defaultInstanceNoMillis.get();
  }

  /**
   * The elements in a string representation of a "duration".
   *
   * NOTE: these constants are listed in descending order of duration (from most-significant to least-significant),
   * which is important for the iteration order.
   */
  public enum Component {
    /**
     * Years, formatted as in integer with no leading zeros, followed by the word "years" 
     * (or "year" if the quantity is singular).
     */
    YEARS('Y'),
    /**
     * Days, formatted as in integer with no leading zeros, followed by the word "days" 
     * (or "day" if the quantity is singular).
     */
    DAYS('D'),
    /**
     * Hours, formatted as two digits with a leading zero as necessary, i.e. 00 - 23, followed by a colon (:)
     */
    HOURS('H'),
    /**
     * Minutes, formatted as two digits with a leading zero as necessary, i.e. 00 - 59, followed by a colon (:)
     */
    MINUTES('M'),
    /**
     * Seconds, formatted as two digits with a leading zero as necessary, i.e. 00 - 59, optionally with a fractional
     * part for milliseconds (the number of decimal places printed is configured by a parameter to one of the
     * constructors of {@link DurationFormat}).
     *
     * @see DurationFormat#DurationFormat(int) 
     * @see DurationFormat#DurationFormat(Component, int, int) 
     */
    SECONDS('S');

    /**
     * The corresponding "conversion suffix character" of the <i>Date/Time Conversions</i> implemented by
     * {@link java.util.Formatter} (e.g. {@code %tH} / {@code %tM} / {@code %tS}).
     * <p>
     * <strong>NOTE</strong>: some of the elements in {@link Component} do not correspond to any of the <i>Date/Time
     * Conversions</i> implemented by {@link java.util.Formatter} (e.g. {@link #DAYS} or {@link #YEARS}).
     */
    public final char conversionChar;

    /**
     * The corresponding {@link TimeUnit}
     */
    public final TimeUnit timeUnit;

    private static final transient Map<Character, Component> valuesByFormatChar;

    static {
      ImmutableMap.Builder<Character, Component> mapBuilder = ImmutableMap.builder();
      for (Component value : values()) {
        mapBuilder.put(value.conversionChar, value);
      }
      valuesByFormatChar = mapBuilder.build();
    }

    Component(char conversionChar) {
      this.conversionChar = conversionChar;
      this.timeUnit = TimeUnit.valueOf(name());
    }

    public static Component valueOf(char c) {
      return valuesByFormatChar.get(c);
    }
  }

}
