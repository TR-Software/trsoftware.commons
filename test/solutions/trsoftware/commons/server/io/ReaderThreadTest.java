package solutions.trsoftware.commons.server.io;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Mar 12, 2011
 *
 * @author Alex
 */
public class ReaderThreadTest extends TestCase {
  private InputStream input;
  private ArrayList<Character> receivedChars;
  private String testString = "Hey\nwhat's up\nyo";

  protected void setUp() throws Exception {
    super.setUp();
    input = new ByteArrayInputStream(testString.getBytes());
    receivedChars = new ArrayList<Character>();
  }

  public void testReadingWithReaderThread() throws Exception {
    ReaderThread readerThread = new ReaderThread(input) {
      public boolean processChar(int character) {
        receivedChars.add((char)character);
        return true;
      }
    };
    assertTrue(receivedChars.isEmpty());
    readerThread.start();
    // wait for the reader thread to terminate
    readerThread.join(2000);
    // at this point, all the input should have been read
    assertEquals(testString.length(), receivedChars.size());
    StringBuilder receivedStr = new StringBuilder(receivedChars.size());
    for (Character c : receivedChars) {
      receivedStr.append(c);
    }
    assertEquals(testString, receivedStr.toString());
  }
  
  public void testTerminatingReaderThreadBeforeEndOfInput() throws Exception {
    ReaderThread readerThread = new ReaderThread(input) {
      int count = 0;
      public boolean processChar(int character) {
        receivedChars.add((char)character);
        return ++count < 2;  // will read only the first two chars
      }
    };
    assertTrue(receivedChars.isEmpty());
    readerThread.start();
    // wait for the reader thread to terminate
    readerThread.join(2000);
    // at this point, the first two lines from the input should have been read
    assertEquals(2, receivedChars.size());
    assertEquals('H', (char)receivedChars.get(0));
    assertEquals('e', (char)receivedChars.get(1));
  }

}