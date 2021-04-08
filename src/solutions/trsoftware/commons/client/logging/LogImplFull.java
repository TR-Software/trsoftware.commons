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

package solutions.trsoftware.commons.client.logging;

/**
 * Prints out real logging information, intended for deferred binding in
 * a debugging permutation of the app.  Delegates to the Console instance,
 * which logs the output to window.console (or to a UI element if window.console
 * is missing).
 *
 * @author Alex
 */
public class LogImplFull extends LogImpl {

  @Override
  protected boolean isLoggingEnabled() {
    return true;
  }

  @Override
  public void log(String msg) {
    Console.instance.log(msg);
  }

}
