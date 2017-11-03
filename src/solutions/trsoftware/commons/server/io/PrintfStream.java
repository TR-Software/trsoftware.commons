package solutions.trsoftware.commons.server.io;

import java.io.PrintStream;

/**
 * Wraps a {@link PrintStream} and exposes some convenience methods for it.
 *
 * @author Alex
 */
public class PrintfStream {

  PrintStream delegate;

  public PrintfStream(PrintStream delegate) {
    this.delegate = delegate;
  }

  public PrintStream getDelegate() {
    return delegate;
  }

  // the convenience methods

  /** Same as {@link PrintStream#printf(String, Object...)}, but additionally prints a newline at the end. */
  public PrintStream println(String format, Object... args) {
    return delegate.printf(format + "%n", args);
  }



}
