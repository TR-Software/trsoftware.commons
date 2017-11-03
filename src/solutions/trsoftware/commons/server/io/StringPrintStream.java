package solutions.trsoftware.commons.server.io;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * A print stream (like System.out) that writes to a string buffer.
 *
 * @author Alex
 */
public class StringPrintStream extends PrintStream {

  public StringPrintStream() {
    super(new ByteArrayOutputStream(128));
  }

  public StringPrintStream(int bufferSize) {
    super(new ByteArrayOutputStream(bufferSize));
  }

  public StringPrintStream(int bufferSize, boolean autoFlush) {
    super(new ByteArrayOutputStream(bufferSize), autoFlush);
  }

  public StringPrintStream(int bufferSize,  boolean autoFlush, String encoding) throws UnsupportedEncodingException {
    super(new ByteArrayOutputStream(bufferSize), autoFlush, encoding);
  }

  @Override
  public String toString() {
    return getBuffer().toString();
  }

  public int size() {
    return getBuffer().size();
  }

  private ByteArrayOutputStream getBuffer() {
    return (ByteArrayOutputStream)out;
  }
}
