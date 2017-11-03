package solutions.trsoftware.commons.client.logging;

import com.google.gwt.core.shared.GWT;

/**
 * Prints all logging information to GWT's console in hosted mode.
 * Delegates to the LogImpl in web mode.
 */
public class Log {

  private static final LogImpl impl = GWT.create(LogImpl.class);

  /**
   * Whether the write() method will have any effect.  This field can be used 
   * in a conditional statement to allow the compiler to eliminate the logging
   * statements in client code when logging is disabled.
   */
  public static final boolean ENABLED = !GWT.isScript() || impl.isLoggingEnabled();

  public static void write(String msg) {
    impl.log(msg);
    if (!GWT.isScript())
      GWT.log(msg, null);
  }

  /**
   * Same as write(msg), but additional actions may be taken for these
   * kinds of messages in the future
   * (e.g. posting them to the server to be logged).
   *
   * @param msg
   */
  public static void error(String msg, Throwable ex) {
    impl.error(msg, ex);
    if (!GWT.isScript())
      GWT.log(msg, ex);
  }

}
