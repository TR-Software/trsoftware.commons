package solutions.trsoftware.commons.server.io;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;

/**
 * A {@link Reader} that concatenates multiple readers.
 *
 *
 */
public class MultiReader extends Reader {
  private final Iterator<? extends Reader> it;
  private Reader current;

  public MultiReader(Iterator<? extends Reader> readers) throws IOException {
    this.it = readers;
    advance();
  }

  public MultiReader(Collection<? extends Reader> readers) throws IOException {
    this.it = readers.iterator();
    advance();
  }

  /**
   * Closes the current reader and opens the next one, if any.
   */
  private void advance() throws IOException {
    close();
    if (it.hasNext()) {
      current = it.next();
    }
  }

  @Override public int read(char cbuf[], int off, int len) throws IOException {
    if (current == null) {
      return -1;
    }
    int result = current.read(cbuf, off, len);
    if (result == -1) {
      advance();
      return read(cbuf, off, len);
    }
    return result;
  }

  @Override public long skip(long n) throws IOException {
    assert n >= 0;
    if (n > 0) {
      while (current != null) {
        long result = current.skip(n);
        if (result > 0) {
          return result;
        }
        advance();
      }
    }
    return 0;
  }

  @Override public boolean ready() throws IOException {
    return (current != null) && current.ready();
  }

  @Override public void close() throws IOException {
    if (current != null) {
      try {
        current.close();
      } finally {
        current = null;
      }
    }
  }
}