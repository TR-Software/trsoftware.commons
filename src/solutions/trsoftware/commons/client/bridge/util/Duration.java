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

package solutions.trsoftware.commons.client.bridge.util;

import solutions.trsoftware.commons.shared.util.TimeUnit;

/**
 * Apr 28, 2011
 *
 * @author Alex
 */
public interface Duration {

  /**
   * @return the number of milliseconds that have elapsed since this object was created.
   */
  double elapsedMillis();

  /**
   * @return the time elapsed since this object was created, expressed in the given unit.
   */
  double elapsed(TimeUnit timeUnit);

  /** @return true if more than the given time value has elapsed */
  boolean exceeds(long value, TimeUnit timeUnit);

  /**
   * Computes processing speed.
   * @param nOperations the number of operations carried out since this Duration was instantiated
   * @param timeUnit the time unit for the speed computation (e.g. per second, per minute, per hour, etc.)
   * @return the processing speed as {@code nOperations} per {@code timeUnit}
   */
  double computeSpeed(int nOperations, TimeUnit timeUnit);  // TODO(11/1/2016): use this new method everywhere Duration is used to compute processing speed

}
