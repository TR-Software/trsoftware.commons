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
