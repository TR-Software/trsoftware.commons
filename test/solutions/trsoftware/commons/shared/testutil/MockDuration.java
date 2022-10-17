/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.shared.testutil;

import com.google.common.annotations.VisibleForTesting;
import solutions.trsoftware.commons.shared.util.AbstractDuration;

/**
 * Can be used by unit tests to specify the time returned by each invocation of {@link #elapsedMillis()}.
 *
 * @author Alex
 * @since 11/11/2021
 */
public class MockDuration extends AbstractDuration {

  @VisibleForTesting
  public double elapsedMillis;


  public MockDuration() {
  }

  public MockDuration(String name) {
    super(name);
  }

  public MockDuration(String name, String verb) {
    super(name, verb);
  }

  @Override
  public double elapsedMillis() {
    return elapsedMillis;
  }

  /**
   * Sets {@link #elapsedMillis} to the given value.
   *
   * @return the current value of {@link #elapsedMillis}, after this update was applied.
   */
  public double set(double elapsedMillis) {
    return this.elapsedMillis = elapsedMillis;
  }

  public void reset() {
    elapsedMillis = 0;
  }

  /**
   * Adds the given increment to {@link #elapsedMillis}.
   *
   * @return the current value of {@link #elapsedMillis}, after this update was applied.
   */
  public double incrementAndGet(double increment) {
    return this.elapsedMillis += increment;
  }
}
