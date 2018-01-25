package solutions.trsoftware.commons.server.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An {@link OutputStream} that doesn't do anything with the written bytes - it simply skips over them.
 * This is useful in situations where an input stream must be read but we don't care about the individual bytes,
 * which might be the case when using a {@link java.io.FilterInputStream} to process data.
 * Example: computing checksums of files using {@link java.security.DigestInputStream}.
 *
 * @author Alex
 * @since 11/11/2017
 */
public class NullOutputStream extends OutputStream {

  @Override
  public void write(int b) throws IOException {
    // intentionally empty
  }
}
