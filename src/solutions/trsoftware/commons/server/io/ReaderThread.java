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
 * Thread that reads from a given input stream or reader character-by-character,
 * terminating when the end of the input stream is reached.
 *
 * Subclasses should override the method processChar to do something with the
 * characters.
 *
 * Mar 8, 2011
 *
 * @author Alex
 */
public abstract class ReaderThread extends Thread {
  private Reader reader;

  public ReaderThread(Reader in) {
    reader = in;
  }

  public ReaderThread(InputStream in) {
    this(new InputStreamReader(in));
  }

  public ReaderThread(Reader in, String name) {
    super(name);
    reader = new BufferedReader(in);
  }

  public ReaderThread(InputStream in, String name) {
    this(new InputStreamReader(in), name);
  }

  /**
   * Will be called for each char in the input.
   * @return false to stop reading and terminate thread, otherwise true.
   */
  protected abstract boolean processChar(int character);

  /** Sublcasses may override to hand IOExceptions thrown during reading */
  protected void handleException(IOException ex) {
    ex.printStackTrace();
    throw new RuntimeException(ex);
  }

  /** Sublcasses may override */
  protected void doneReading() {
    try {
      reader.close();
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
        int next = reader.read();
        if (next == -1 || !processChar(next))
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