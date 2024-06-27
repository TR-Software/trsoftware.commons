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

import solutions.trsoftware.commons.shared.util.StringUtils;

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

  /**
   * Prints the given message surrounded by dashed lines of {@code '-'} chars of equal length
   *
   * @param msg the header text
   * @return the resulting width of the header lines in the output
   */
  public static int printSectionHeader(String msg) {
    int width = msg != null ? msg.length() : 0;
    printSectionHeader(msg, width);
    return width;
  }

  /**
   * Prints the given message surrounded by dashed lines of {@code '-'} chars of equal length
   * @param msg the header text
   * @param width the length of the dashed lines
   * @see #printSectionHeader(String)
   */
  public static void printSectionHeader(String msg, int width) {
    if (StringUtils.notBlank(msg)) {
      String hr = StringUtils.repeat('-', width);
      System.out.println(hr);
      System.out.println(msg);
      System.out.println(hr);
    }
  }
}
