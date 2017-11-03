package solutions.trsoftware.commons.server.io;

import java.io.*;

/**
 * Thread that reads from a given input stream or reader using BufferedReader
 * line-by-line, terminating when the end of the input stream is reached.
 * Subclasses should override the method processLine to do something with the lines.
 *
 * Mar 8, 2011
 *
 * @author Alex
 */
public abstract class BufferedReaderThread extends Thread {
  private BufferedReader br;

  public BufferedReaderThread(Reader in) {
    br = new BufferedReader(in);
  }

  public BufferedReaderThread(InputStream in) {
    this(new InputStreamReader(in));
  }

  public BufferedReaderThread(Reader in, String name) {
    super(name);
    br = new BufferedReader(in);
  }

  public BufferedReaderThread(InputStream in, String name) {
    this(new InputStreamReader(in), name);
  }

  /**
   * Will be called for each line in the input.
   * @return false to stop reading and terminate thread, otherwise true.
   */
  protected abstract boolean processLine(String line);

  /** Sublcasses may override to hand IOExceptions thrown during reading */
  protected void handleException(IOException ex) {
    ex.printStackTrace();
    throw new RuntimeException(ex);
  }

  /** Sublcasses may override */
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
