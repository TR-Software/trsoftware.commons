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

package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.shared.util.AbstractDuration;
import solutions.trsoftware.commons.shared.util.TimeUnit;
import solutions.trsoftware.commons.shared.util.text.DurationFormat;

/**
 * Server-side implementation of {@link solutions.trsoftware.commons.shared.util.Duration}
 * (a utility class for measuring elapsed time).
 * <p>
 * Uses {@link System#nanoTime()} to compute {@link #elapsedMillis()}. This provides better precision than {@link
 * System#currentTimeMillis()}, but is <em>not able to correctly measure durations greater than 292 years</em>
 * (2<sup>63</sup> nanoseconds) due to numerical overflow.
 * <p>
 * Implements {@link AutoCloseable} to allow automatically printing the duration at the end of a try-with-resources block
 */
public class Duration extends AbstractDuration implements AutoCloseable {

  private long start;

  /**
   * Creates a new instance whose start time is now.
   * <p>
   * The {@link #toString()} method will simply return the elapsed duration formatted with {@link DurationFormat}.
   */
  public Duration() {
    this("");
  }

  /**
   * Creates a new instance whose start time is now, with a name.
   * <p>
   * The {@link #toString()} method will return something like {@code "{name} took {duration}"}
   * @param name action name for pretty printing (optional)
   */
  public Duration(String name) {
    this(name, "took");
  }


  /**
   * Creates a new instance whose start time is now, with a name and action verb.
   * <p>
   * The {@link #toString()} method will return something like {@code "{name} {verb} {duration}"}
   * @param name action name for pretty printing (optional)
   * @param verb action verb for pretty printing (optional)
   */
  public Duration(String name, String verb) {
    super(name, verb);
    start = System.nanoTime();
  }

  /**
   * @return the number of milliseconds that have elapsed since this object was created.
   */
  public double elapsedMillis() {
    return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
  }


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
    return DurationFormat.getDefaultInstance(printMillis).format(millisDuration);
  }


  @Override
  public void close() {
    System.out.println(toString());
  }
}
