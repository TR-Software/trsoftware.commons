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

package solutions.trsoftware.commons.shared.testutil;

import java.util.function.BooleanSupplier;

/**
 * @author Alex, 10/23/2017
 * @see solutions.trsoftware.commons.shared.testutil.TestData
 */
public class TestUtils {

  /**
   * Busy waits until the condition evaluates to true or the timeout has
   * elapsed.
   *
   * @return true if the condition was met within timeoutMs, false otherwise.
   */
  public static boolean waitFor(BooleanSupplier endCondition, long timeoutMs) throws Exception {
    long startTime = System.currentTimeMillis();
    while (!endCondition.getAsBoolean()) {
      if (System.currentTimeMillis() > startTime + timeoutMs) {
        return false;
      }
    }
    return true;
  }

  /**
   * Busy waits for the given number of milliseconds.
   */
  public static void busyWait(long millis) {
    long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() < startTime + millis);
  }
}
