package solutions.trsoftware.commons.server.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a way to make sure that Readers and Streams are closed. 
 * Mar 5, 2010
 *
 * @author Alex
 */
public class CloseablePool implements Closeable {
  private List<Closeable> pool = new ArrayList<Closeable>();
  /** All exceptions that were thrown on closing will be saved in this list */
  private List<IOException> exceptions;

  /**
   * Makes sure that close() is invoked on all members (by suppressing, that
   * is, only printing but not throwing any exceptions).
   * The exceptions can be later retrieved by calling getExceptions.
   *
   * @return true if all closed successfully, false if exceptions encountered
   * @throws IOException the first exception encountered while closing the members
   */
  public void close() throws IOException {
    for (Closeable closeable : pool) {
      try {
        closeable.close();
      }
      catch (IOException e) {
        if (exceptions == null)
          exceptions = new ArrayList<IOException>();
        e.printStackTrace();
        exceptions.add(e);
      }
    }
    if (exceptions != null)
      throw exceptions.get(0);
  }

  /**
   * Adds a new member to the pool.
   * @return The same instance for method chaining.
   */
  public <T extends Closeable> T add(T closeable) {
    pool.add(closeable);
    return closeable;
  }
  
}
