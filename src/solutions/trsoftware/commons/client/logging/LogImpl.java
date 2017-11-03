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
