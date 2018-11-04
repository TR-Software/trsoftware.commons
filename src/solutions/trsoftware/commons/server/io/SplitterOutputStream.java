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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Serves a similar purpose to the Unix {@code tee} utility.
 *
 * @author Alex
 */
public class SplitterOutputStream extends OutputStream {
  private OutputStream[] destinationStreams;

  public SplitterOutputStream(OutputStream ... destinationStreams) {
    this.destinationStreams = destinationStreams;
  }

  public void write(int b) throws IOException {
    for (OutputStream destinationStream : destinationStreams) {
      destinationStream.write(b);
    }
  }

  /**
   * Closes the underlying streams, but never closes {@link System#out} or {@link System#err},
   * (closing these streams could cause a debugging nightmare).
   */
  @Override
  public void close() throws IOException {
    for (OutputStream destinationStream : destinationStreams) {
      if (destinationStream != System.out && destinationStream != System.err)
        destinationStream.close();
      else
        destinationStream.flush();
    }
  }

  @Override
  public void flush() throws IOException {
    for (OutputStream destinationStream : destinationStreams) {
      destinationStream.flush();
    }
  }
}
