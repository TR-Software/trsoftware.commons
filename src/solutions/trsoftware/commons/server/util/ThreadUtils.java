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

import solutions.trsoftware.commons.client.util.SchedulerUtils;

import java.util.concurrent.Callable;

/**
 * @author Alex, 1/7/14
 */
public final class ThreadUtils {

  private static final int DEFAULT_SLEEP_DURATION = 5;

  /** This class contains only {@code static} methods, and hence shouldn't be instantiated */
  private ThreadUtils() {
  }

  /** Sleeps the current thread for the given duration, ignoring all {@link InterruptedException}s */
  public static void sleepUnchecked(long millis) {
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException e) {
    }
  }

  /**
   * Waits until the given condition evaluates to true or the timeout has elapsed using a wait loop which checks
   * the given condition, and calls {@link Thread#sleep(long)} between those checks.
   * @param endCondition the condition to check
   * @param timeoutMs total millis to wait before returning {@code false}
   * @param sleepMs millis to sleep between successive checks of the condition
   * @return {@code true} iff the condition was met before the timeout elapsed.
   *
   * @throws Exception if either the given {@link Callable} or {@link Thread#sleep(long)} threw an exception
   *
   * @see SchedulerUtils#checkAndWait
   */
  public static boolean waitFor(Callable<Boolean> endCondition, long timeoutMs, long sleepMs) throws Exception {
    long startTime = System.currentTimeMillis();
    while (!endCondition.call()) {
      Thread.sleep(sleepMs);
      if (System.currentTimeMillis() > startTime + timeoutMs) {
        return false;
      }
    }
    return true;
  }

  /**
   * Waits until the given condition evaluates to true or the timeout has elapsed using a wait loop which checks
   * the given condition, sleeping for {@value DEFAULT_SLEEP_DURATION} millis between those checks.
   * @param endCondition the condition to check
   * @param timeoutMs total millis to wait before returning {@code false}
   * @return {@code true} iff the condition was met before the timeout elapsed.
   *
   * @throws Exception if either the given {@link Callable} or {@link Thread#sleep(long)} threw an exception
   */
  public static boolean waitFor(Callable<Boolean> endCondition, long timeoutMs) throws Exception {
    return waitFor(endCondition, timeoutMs, DEFAULT_SLEEP_DURATION);
  }

}
