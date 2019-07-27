package solutions.trsoftware.commons.server.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Wraps an underlying {@link OutputStream} to prevent closing it.
 * <p>
 * This is useful in situations where one might want to wrap streams that should never be closed,
 * such as {@link System#out} or {@link System#err}.
 *
 * @author Alex
 * @since 7/27/2019
 */
public class NonCloseableOutputStream extends FilterOutputStream {

  /**
   * @param out the underlying output stream that should never be closed.
   */
  public NonCloseableOutputStream(OutputStream out) {
    super(out);
  }

  /**
   * Flushes the wrapped stream but does not close it.
   */
  @Override
  public void close() throws IOException {
    flush();
  }

}
