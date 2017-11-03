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

/**
 * @author Alex, 1/7/14
 */
public abstract class ThreadUtils {

  /** Sleeps the current thread for the given duration, ignoring all {@link InterruptedException}s */
  public static void sleepUnchecked(long millis) {
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException e) {
    }
  }

}
