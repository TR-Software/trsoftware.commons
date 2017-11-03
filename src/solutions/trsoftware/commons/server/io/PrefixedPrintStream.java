package solutions.trsoftware.commons.server.io;

import java.io.PrintStream;

/**
 * Wraps a {@link PrintStream} to print a prefix on every invocation of
 * {@link PrintStream#println(String)} and {@link PrintStream#printf(String, Object...)}.
 *
 * @author Alex
 */
public class PrefixedPrintStream extends PrintStream {

  private String prefix;
  private PrintStream delegate;

  public PrefixedPrintStream(String prefix, PrintStream delegate) {
    super(delegate);
    this.prefix = prefix;
    this.delegate = delegate;
  }

  public PrintStream getDelegate() {
    return delegate;
  }

  // override methods to add the prefix


  @Override
  public void println(String x) {
    super.println(prefix + x);
  }

  @Override
  public PrintStream printf(String format, Object... args) {
    return super.printf(prefix + format, args);
  }

}
