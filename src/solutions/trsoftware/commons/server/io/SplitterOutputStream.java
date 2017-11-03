package solutions.trsoftware.commons.server.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Serves a similar purpose to the Unix tee utility.
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
   * Closes the underlying streams, but never closes System.out or System.err,
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
