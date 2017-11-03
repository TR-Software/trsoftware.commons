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

import solutions.trsoftware.commons.client.jso.JsConsole;

/**
 * This class is intended to be used (via deferred binding) when we don't
 * wish to log any debugging information to window.console.  Only the error(Object)
 * method will print output to the console (and only if window.console exists).
 *
 * @author Alex
 * @since Mar 26, 2013
 */
public class ConsoleMinimalImpl implements Console {

  private JsConsole jsConsole = JsConsole.get();

  /** This is the only method actually implemented by ConsoleBlankImpl */
  public void error(Object arg) {
    if (jsConsole != null)
      if (jsConsole.implementsError())
        jsConsole.error(arg);
      else
        jsConsole.log(arg);
  }

  public void log(Object arg) {
    // intentionally blank
  }

  public void warn(Object arg) {
    // intentionally blank
  }

  public void group(Object arg) {
    // intentionally blank
  }

  public void groupCollapsed(Object arg) {
    // intentionally blank
  }

  public void groupEnd() {
    // intentionally blank
  }

  public void markTimeline(Object arg) {
    // intentionally blank
  }

  public void timeStamp(Object arg) {
    // intentionally blank
  }

  public void time(String title) {
    // intentionally blank
  }

  public void timeEnd(String title) {
    // intentionally blank
  }

  public void profile(String title) {
    // intentionally blank
  }

  public void profileEnd(String title) {
    // intentionally blank
  }

  public void trace(Object arg) {
    // intentionally blank
  }

  public void count() {
    // intentionally blank
  }
}
