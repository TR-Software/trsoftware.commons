/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.commons.server.io;

import javax.annotation.Nonnull;
import java.io.*;

/**
 * Sends the same output to a number of underlying output streams.
 * <p>
 * Can be used to implement something similar to the Unix {@code tee} utility.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Tee_(command)">tee (shell command)</a>
 * @see #teeToFile(File)
 * @see #teeTo(OutputStream)
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

  /**
   * Factory method that constructs an instance that behaves like the Unix {@code tee} utility.
   *
   * @param outputFile the file to be opened for writing using {@link FileOutputStream#FileOutputStream(File)}.
   * <em><strong>NOTE:</strong> the written bytes will overwrite the file</em>; to have the bytes appended
   * use {@link #teeTo(OutputStream)} with an instance of {@link FileOutputStream} created with {@code append = true}.
   * @return an output stream that writes both to stdout and the given file
   * @see #teeTo(OutputStream)
   * @see <a href="https://en.wikipedia.org/wiki/Tee_(command)">tee (shell command)</a>
   */
  @Nonnull
  public static PrintStream teeToFile(File outputFile) throws FileNotFoundException {
    return teeTo(new FileOutputStream(outputFile));
  }

  /**
   * Factory method that constructs an instance that behaves like the Unix {@code tee} utility,
   * except allows writing to any output stream, not just a file.
   *
   * @return an output stream that writes both to stdout and the given output stream.
   * @see <a href="https://en.wikipedia.org/wiki/Tee_(command)">tee (shell command)</a>
   */
  @Nonnull
  public static PrintStream teeTo(OutputStream outStream) {
    return new PrintStream(
        new SplitterOutputStream(
            new NonCloseableOutputStream(System.out), // wrap stdout to prevent accidentally closing it
            outStream
        ),
        true);
  }
}
