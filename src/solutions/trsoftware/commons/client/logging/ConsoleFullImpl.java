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

import solutions.trsoftware.commons.client.jso.JsConsole;
import solutions.trsoftware.commons.client.util.Duration;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstracts away the {@code window.console} object by providing default implementations
 * of the methods that might not be supported by the various browser implementations
 * of {@code window.console}.
 *
 * @see JsConsole
 * @see <a href="http://getfirebug.com/wiki/index.php/Console_API">Firebug Console Reference</a>
 * @see <a href="https://developers.google.com/chrome-developer-tools/docs/console-api">Chrome Dev Tools Console Reference</a>
 *
 * @since Mar 26, 2013
 * @author Alex
 */
public class ConsoleFullImpl implements Console {

  // NOTE: most of the Javadoc comments in this class were copied from the Firebug console

  private JsConsole jsConsole = JsConsole.get();

  /** Supports emulation of console.time: a name to duration mapping for the operations being timed */
  private Map<String, Duration> timedOperations;

  // Singleton, not to be instantiated directly
  private ConsoleFullImpl() {
    if (jsConsole == null || !jsConsole.implementsTime()) {
      timedOperations = new HashMap<String, Duration>();
    }
  }

  /** Prints a message to the console */
  public void log(Object arg) {
    jsConsole.log(arg);
  }

  /** "Writes a message to the console with the visual "warning" icon and color coding and a hyperlink to the line where it was called." */
  public void warn(Object arg) {
    if (jsConsole.implementsWarn())
      jsConsole.warn(arg);
    else
      log("WARNING: " + arg.toString());
  }

  /** "Writes a message to the console with the visual "error" icon and color coding and a hyperlink to the line where it was called." */
  public void error(Object arg) {
    if (jsConsole.implementsError())
      jsConsole.error(arg);
    else
      log("ERROR: " + arg.toString());
  }

  /** "Writes a message to the console and opens a nested block to indent all future messages sent to the console. Call console.groupEnd() to close the block." */
  public void group(Object arg) {
    if (jsConsole.implementsGroup())
      jsConsole.group(arg);
    // do nothing if the browser doesn't support this method
  }

  public void groupCollapsed(Object arg) {
    if (jsConsole.implementsGroupCollapsed())
      jsConsole.groupCollapsed(arg);
    else
      group(arg);
  }

  public void groupEnd() {
    if (jsConsole.implementsGroupEnd())
      jsConsole.groupEnd();
    // do nothing if the browser doesn't support this method
  }

  /**
   * Some window.console implementations (like WebKit) support a markTimeline
   * method, by which an app can add an annotation to the Timeline section
   * of the browser's developer tools.  This is particularly useful for the
   * Speed Tracer chrome extension (see: https://developers.google.com/web-toolkit/speedtracer/logging-api )
   */
  public void markTimeline(Object arg) {
    if (jsConsole.implementsMarkTimeline())
      jsConsole.markTimeline(arg);
    // do nothing if the browser doesn't support this method
  }

  public void timeStamp(Object arg) {
    if (jsConsole.implementsTimeStamp())
      jsConsole.timeStamp(arg);
    // do nothing if the browser doesn't support this method
  }

  public void time(String title) {
    if (timedOperations != null)
      timedOperations.put(title, new Duration(title));
    else
      jsConsole.time(title);
  }

  public void timeEnd(String title) {
    if (timedOperations != null)
      log(timedOperations.remove(title));
    else
      jsConsole.timeEnd(title);
  }

  public void profile(String title) {
    if (jsConsole.implementsProfile())
      jsConsole.profile(title);
    // do nothing if the browser doesn't support this method
  }

  public void profileEnd(String title) {
    if (jsConsole.implementsProfileEnd())
      jsConsole.profileEnd(title);
    // do nothing if the browser doesn't support this method
  }

  public void trace(Object arg) {
    if (jsConsole.implementsTrace())
      jsConsole.trace(arg);
    // do nothing if the browser doesn't support this method
    // TODO: manually get the stack trace if arg is an exception
  }

  /** "Writes the number of times that the line of code where count was called was executed. The optional argument title will print a message in addition to the number of the count." */
  public void count() {
    if (jsConsole.implementsCount())
      jsConsole.count("");
    // do nothing if the browser doesn't support this method
  }
}
