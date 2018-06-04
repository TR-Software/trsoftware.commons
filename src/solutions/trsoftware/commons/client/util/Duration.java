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

import static com.google.gwt.core.client.Duration.currentTimeMillis;

/**
 * Adapts GWT's Duration class (a utility class for measuring elapsed time)
 * for compatibility with the solutions.trsoftware.commons.client.bridge.util.Duration
 * interface (so the same interface can be used on both the client and server).
 */
public class Duration extends AbstractDuration {

  private double start;

  /**
   * Creates a new Duration whose start time is now.
   */
  public Duration() {
    this("", "");
  }

  /**
   * Creates a new Duration whose start time is now, with a name.
   * The toString method will return "{name} took {duration} {timeUnit}"
   */
  public Duration(String name) {
    this(name, "took");
  }


  /**
   * Creates a new Duration whose start time is now, with a name and action.
   * The toString method will return "{name} {action} {duration} {timeUnit}"
   */
  public Duration(String name, String action) {
    super(action, name);
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