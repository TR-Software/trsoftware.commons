package solutions.trsoftware.commons.client.logging;

import com.google.gwt.core.client.GWT;
import solutions.trsoftware.commons.client.jso.JsConsole;

/**
 * Abstracts away window.console object by providing implementations for a subset
 * a subset of the methods provided by the various browser implementations
 * of window.console (log, time, timeEnd, etc.) by either delegating to the
 * native window.console object or by emulating the methods not supported by
 * the current browser instance.
 *
 * NOTE: most of the Javadoc comments in this class were copied from the Firebug console
 *
 * @see JsConsole
 * @see <a href="http://getfirebug.com/wiki/index.php/Console_API">Firebug Console Reference</a>
 * @see <a href="https://developers.google.com/chrome-developer-tools/docs/console-api">Chrome Dev Tools Console Reference</a>
 *
 * @since Mar 26, 2013
 * @author Alex
 */
public interface Console {

  Console instance = GWT.create(Console.class);

  void log(Object arg);

  void warn(Object arg);

  void error(Object arg);

  void group(Object arg);

  void groupCollapsed(Object arg);

  void groupEnd();

  void markTimeline(Object arg);

  void timeStamp(Object arg);

  void time(String title);

  void timeEnd(String title);

  void profile(String title);

  void profileEnd(String title);

  void trace(Object arg);

  void count();
}
