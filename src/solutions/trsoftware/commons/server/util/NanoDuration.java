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

package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.client.util.AbstractDuration;

/**
 * Serverside implementation of solutions.trsoftware.commons.client.bridge.util.Duration
 * which uses nanosecond precision which is only available in a JVM.
 */
public class NanoDuration extends AbstractDuration {

  public static final double NANOS_IN_MILLIS = 1000000;  // 1 million nanoseconds in 1 millisecond

  private long startNanos;

  /**
   * Creates a new Duration whose start time is now.
   */
  public NanoDuration() {
    this("", "");
  }

  /**
   * Creates a new Duration whose start time is now, with a name.
   * The toString method will return "{name} took {duration} {timeUnit}"
   */
  public NanoDuration(String name) {
    this(name, "took");
  }


  /**
   * Creates a new Duration whose start time is now, with a name and action.
   * The toString method will return "{name} {action} {duration} {timeUnit}"
   */
  public NanoDuration(String name, String action) {
    super(name, action);
    startNanos = System.nanoTime();
  }

  /**
   * Returns the number of milliseconds that have elapsed since this object was
   * created.
   */
  @Override
  public double elapsedMillis() {
    return (double)(System.nanoTime() - startNanos) / NANOS_IN_MILLIS;
  }

}