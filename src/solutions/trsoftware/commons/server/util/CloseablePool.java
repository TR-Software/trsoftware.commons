/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

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
