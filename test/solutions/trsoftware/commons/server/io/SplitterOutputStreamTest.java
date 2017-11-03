package solutions.trsoftware.commons.server.io;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import junit.framework.TestCase;

import java.io.PrintStream;

/**
 * Jan 7, 2009
 *
 * @author Alex
 */
public class SplitterOutputStreamTest extends TestCase {


  public void testWriteToOneStream() throws Exception {
    ByteOutputStream stream1 = new ByteOutputStream();

    SplitterOutputStream splitter = new SplitterOutputStream(stream1);
    PrintStream ps = new PrintStream(splitter);
    ps.println("Testing");

    // make sure the data is passed to the underlying stream
    assertEquals("Testing" + System.getProperty("line.separator"), stream1.toString());
  }

  public void testWriteToMultipleStreams() throws Exception {
    ByteOutputStream stream1 = new ByteOutputStream();
    ByteOutputStream stream2 = new ByteOutputStream();

    SplitterOutputStream splitter = new SplitterOutputStream(stream1, stream2);
    PrintStream ps = new PrintStream(splitter);
    ps.println("Testing");

    // make sure the data is passed to the underlying stream
    assertEquals("Testing" + System.getProperty("line.separator"), stream1.toString());
    assertEquals("Testing" + System.getProperty("line.separator"), stream2.toString());
    assertNotSame(stream1.toString(), stream2.toString());
  }
}