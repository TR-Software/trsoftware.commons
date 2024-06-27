/*
 * Copyright 2023 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util.time;

import com.google.common.annotations.Beta;

/**
 * A more-flexible version of {@link com.google.common.base.Stopwatch}: allows manually setting the elapsed
 * duration and doesn't needlessly throw {@link IllegalStateException}s.
 *
 * @author Alex
 * @since 5/16/2023
 */
@Beta
public class Stopwatch {

  // TODO: impl this




  // TODO: move the following Guava Stopwatch methods to a util class:
  /**
   * Invokes {@link com.google.common.base.Stopwatch#stop()} if the given instance is actually running
   * (otherwise {@link com.google.common.base.Stopwatch#stop()} would throw an {@link IllegalStateException}).
   *
   * @return {@code true} iff the stopwatch state changed as a result of this call
   */
  public static boolean pauseStopwatch(com.google.common.base.Stopwatch stopwatch) {
    if (stopwatch != null && stopwatch.isRunning()) {
      stopwatch.stop();
      return true;
    }
    return false;
  }

  /**
   * Invokes {@link com.google.common.base.Stopwatch#start()} if the given instance is not already running
   * (otherwise {@link com.google.common.base.Stopwatch#start()} would throw an {@link IllegalStateException}).
   *
   * @return {@code true} iff the stopwatch state changed as a result of this call
   */
  public static boolean resumeStopwatch(com.google.common.base.Stopwatch stopwatch) {
    if (stopwatch != null && !stopwatch.isRunning()) {
      stopwatch.start();
      return true;
    }
    return false;
  }




}
