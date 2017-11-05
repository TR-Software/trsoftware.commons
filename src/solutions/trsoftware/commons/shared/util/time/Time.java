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

package solutions.trsoftware.commons.shared.util.time;

/**
 * @author Alex, 3/24/2015
 */
public abstract class Time {

  public abstract double currentTimeMillis();

  /**
   * @return the number of millis remaining until the given timestamp.
   */
  public final double getMillisUntil(double timestamp) {
    return timestamp - currentTimeMillis();
  }

  /**
   * @return the number of millis elapsed since the given timestamp.
   */
  public final double getMillisSince(double timestamp) {
    return currentTimeMillis() - timestamp ;
  }
}
