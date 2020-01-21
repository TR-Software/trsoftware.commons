/*
 * Copyright 2018 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.server.io;

import java.io.*;

/**
 * Thread that reads from a given input stream or reader line-by-line (using a {@link BufferedReader}), and
 * terminates when the end of the input stream is reached.
 * <p>
 * Subclasses must implement {@link #processLine(String)} to do something with the lines.
 *
 * @author Alex
 * @since Mar 8, 2011
 */
public abstract class BufferedReaderThread extends Thread {
  private BufferedReader br;

  public BufferedReaderThread(Reader in) {
    br = new BufferedReader(in);
  }

  public BufferedReaderThread(InputStream in) {
    this(new InputStreamReader(in));
  }

  /**
   * @param in the input to be read
   * @param name name for this thread, which will be passed to {@link Thread#Thread(String)}
   */
  public BufferedReaderThread(Reader in, String name) {
    super(name);
    br = new BufferedReader(in);
  }

  /**
   * @param in the input to be read
   * @param name name for this thread, which will be passed to {@link Thread#Thread(String)}
   */
  public BufferedReaderThread(InputStream in, String name) {
    // TODO(10/12/2019): allow passing a specific charset to InputStreamReader?
    this(new InputStreamReader(in), name);
  }

  /**
   * Will be called for each line in the input.
   * @return {@code false} to stop reading and terminate thread, otherwise {@code true}.
   */
  protected abstract boolean processLine(String line);

  /** Subclasses may override to handle {@link IOException}s thrown during reading */
  protected void handleException(IOException ex) {
    ex.printStackTrace();
    throw new RuntimeException(ex);
  }

  /** Subclasses may override */
  protected void doneReading() {
    try {
      br.close();
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void run() {
    while (true) {
      try {
        String line = br.readLine();
        if (line == null || !processLine(line))
          break;
      }
      catch (IOException e) {
        handleException(e);
        break;
      }
    }
    doneReading();
  }
}
