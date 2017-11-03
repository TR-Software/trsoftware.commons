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

package solutions.trsoftware.commons.client.logging;

import solutions.trsoftware.commons.client.util.MessageFormatter;

/**
 * Defines a stub logging implementation, which by default
 * supresses all client-side logging information, except errors, but can be replaced
 * with a real implementation using deferred binding.
 *
 * @author Alex
 */
public class LogImpl {
  
  protected boolean isLoggingEnabled() {
    return false;
  }

  public void log(String msg) {
    // do nothing (overridden by subclasses)
  }

  /** An error always needs to be logged, regardless of the "debug" flag in the URL */
  public final void error(String msg, Throwable ex) {
    Console.instance.error(msg + ": " + MessageFormatter.exceptionTypeToString(ex) + ": " + ex.getMessage());
  }

}
