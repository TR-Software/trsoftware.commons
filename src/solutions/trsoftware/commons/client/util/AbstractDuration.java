/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.util;

import solutions.trsoftware.commons.client.bridge.util.Duration;
import solutions.trsoftware.commons.shared.util.TimeUnit;

/**
 * Apr 29, 2011
 *
 * @author Alex
 */
public abstract class AbstractDuration implements Duration {
  /** Optional name for pretty printing the duration */
  protected String action;
  /** Optional action for pretty printing the duration */
  protected String name;

  public AbstractDuration(String action, String name) {
    this.action = action;
    this.name = name;
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
   * Returns the time elapsed since this object was created, expressed in the given unit.
   */
  @Override
  public double elapsed(TimeUnit timeUnit) {
    return timeUnit.fromMillis(elapsedMillis()) ;
  }

  /** @return true if more than the given time value has elapsed */
  @Override
  public boolean exceeds(long value, TimeUnit timeUnit) {
    return elapsedMillis() > timeUnit.toMillis(value);
  }

  @Override
  public String toString() {
    int elapsed = (int)elapsedMillis();
    return name + " " + action  + " " + elapsed + " ms.";
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
