package solutions.trsoftware.commons.server.io;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Mar 8, 2011
 *
 * @author Alex
 */
public class BufferedReaderThreadTest extends TestCase {
  private InputStream input;
  private ArrayList<String> receivedLines;

  protected void setUp() throws Exception {
    super.setUp();
    input = new ByteArrayInputStream("Hey\nwhat's up\nyo".getBytes());
    receivedLines = new ArrayList<String>();
  }

  public void testReadingWithBufferedReaderThread() throws Exception {
    BufferedReaderThread readerThread = new BufferedReaderThread(input) {
      public boolean processLine(String line) {
        receivedLines.add(line);
        return true;
      }
    };
    assertTrue(receivedLines.isEmpty());
    readerThread.start();
    // wait for the reader thread to terminate
    readerThread.join(2000);
    // at this point, all the input should have been read
    assertEquals(3, receivedLines.size());
    assertEquals("Hey", receivedLines.get(0));
    assertEquals("what's up", receivedLines.get(1));
    assertEquals("yo", receivedLines.get(2));
  }
  
  public void testTerminatingBufferedReaderThreadBeforeEndOfInput() throws Exception {
    BufferedReaderThread readerThread = new BufferedReaderThread(input) {
      int count = 0;
      public boolean processLine(String line) {
        receivedLines.add(line);
        return ++count < 2;  // will read only the first two lines
      }
    };
    assertTrue(receivedLines.isEmpty());
    readerThread.start();
    // wait for the reader thread to terminate
    readerThread.join(2000);
    // at this point, the first two lines from the input should have been read
    assertEquals(2, receivedLines.size());
    assertEquals("Hey", receivedLines.get(0));
    assertEquals("what's up", receivedLines.get(1));
  }
}