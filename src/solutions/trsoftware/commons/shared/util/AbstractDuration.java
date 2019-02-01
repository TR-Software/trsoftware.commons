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

import solutions.trsoftware.commons.bridge.BridgeTypeFactory;
import solutions.trsoftware.commons.shared.util.text.DurationFormat;

import static solutions.trsoftware.commons.shared.util.StringUtils.notEmpty;

/**
 * Similar to GWT's {@link com.google.gwt.core.client.Duration} (a utility class for measuring elapsed time).
 * Our version is implemented in pure Java (without {@code native} JS methods), so it can be used on both client and
 * server.  We've also added a few utility methods, mainly for displaying durations and converting time units.
 * <p>
 * "Shared" code (that runs on both client and server) can call {@link BridgeTypeFactory#newDuration} to obtain
 * an appropriate instance for its environment.
 *
 * @author Alex, Apr 29, 2011
 * @see com.google.gwt.core.client.Duration
 * @see solutions.trsoftware.commons.client.util.Duration
 * @see solutions.trsoftware.commons.server.util.Duration
 * @see BridgeTypeFactory#newDuration()
 * @see BridgeTypeFactory#newDuration(String)
 * @see BridgeTypeFactory#newDuration(String, String)
 */
public abstract class AbstractDuration implements Duration {
  /** Optional action name for pretty printing the duration */
  protected String name;
  /** Optional action verb for pretty printing the duration */
  protected String verb;

  /**
   * Creates a new instance whose start time is now, with an optional name and action verb.
   * <p>
   * The {@link #toString()} method will return something like {@code "{name} {verb} {duration}"}
   * @param name action name for pretty printing (optional)
   * @param verb action verb for pretty printing (optional)
   */
  public AbstractDuration(String name, String verb) {
    this.name = name;
    this.verb = verb;
  }

  public String getName() {
    return name;
  }

  /** @return reference to itself for method chaining */
  public AbstractDuration setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * @return  the time elapsed since this object was created, expressed in the given unit.
   */
  @Override
  public double elapsed(TimeUnit timeUnit) {
    return timeUnit.fromMillis(elapsedMillis()) ;
  }

  /** @return true if more than the given time value has elapsed */
  @Override
  public boolean exceeds(double value, TimeUnit timeUnit) {
    return elapsedMillis() > timeUnit.toMillis(value);
  }

  @Override
  public String toString() {
    double millis = elapsedMillis();
    StringBuilder out = new StringBuilder();
    if (notEmpty(name) && notEmpty(verb)) {
      out.append(name).append(' ').append(verb).append(' ');
    }
    DurationFormat.getDefaultInstance(true).format(millis, out);
    return out.toString();
  }

  /**
   * Computes processing speed.
   * @param nOperations the number of operations carried out since this Duration was instantiated
   * @param timeUnit the time unit for the speed computation (e.g. per second, per minute, per hour, etc.)
   * @return the processing speed as {@code nOperations} per {@code timeUnit}
   */
  @Override
  public double computeSpeed(int nOperations, TimeUnit timeUnit) {
    return (double)nOperations / elapsed(timeUnit);
  }
}
