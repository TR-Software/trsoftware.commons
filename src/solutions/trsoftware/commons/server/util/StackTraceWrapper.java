package solutions.trsoftware.commons.server.util;

/**
 * Utility class that can be used for obtaining or printing a stack trace without
 * implying that any actual exception or error has occurred.
 * <p>
 * Should be instantiated at the call site whose stack trace is desired, and can be passed around the application,
 * but <em>is not intended to actually be thrown</em> (this class extends {@link Throwable} only by necessity,
 * simply to allow obtaining obtaining a stack trace)
 *
 * <h3>Examples:</h3>
 * <pre>
 *   new StackTraceRecord(message).printStackTrace(System.out);
 *   new StackTraceRecord(message).getStackTrace();
 * </pre>
 *
 */
public class StackTraceWrapper extends Throwable {

  // TODO: use this in RuntimeUtils instead of Exception

  public StackTraceWrapper() { }

  public StackTraceWrapper(String message) {
    super(message);
  }

  public StackTraceWrapper(String message, Throwable cause) {
    super(message, cause);
  }

  public StackTraceWrapper(Throwable cause) {
    super(cause);
  }
}
