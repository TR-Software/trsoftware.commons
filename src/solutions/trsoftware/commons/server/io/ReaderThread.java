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