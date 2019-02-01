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

package solutions.trsoftware.commons.client.util;

import solutions.trsoftware.commons.shared.util.AbstractDuration;
import solutions.trsoftware.commons.shared.util.text.DurationFormat;

import static com.google.gwt.core.client.Duration.currentTimeMillis;

/**
 * Client-side implementation of {@link solutions.trsoftware.commons.shared.util.Duration}
 * (a utility class for measuring elapsed time).
 *
 * Uses {@link com.google.gwt.core.client.Duration#currentTimeMillis()} to compute {@link #elapsedMillis()}
 *
 * @see com.google.gwt.core.client.Duration
 * @see solutions.trsoftware.commons.server.util.Duration
 */
public class Duration extends AbstractDuration {

  private double start;

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
    start = currentTimeMillis();
  }

  /**
   * Returns the number of milliseconds that have elapsed since this object was
   * created.
   */
  public double elapsedMillis() {
    return currentTimeMillis() - start;
  }


}