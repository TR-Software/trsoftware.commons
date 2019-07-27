package solutions.trsoftware.commons.server.io;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * @author Alex
 * @since 7/27/2019
 */
public class NonCloseableOutputStreamTest extends TestCase {

  public void testClose() throws Exception {
    TestStream wrappedStream = new TestStream();
    NonCloseableOutputStream nonCloseableOutputStream = new NonCloseableOutputStream(wrappedStream);
    assertFalse(wrappedStream.flushed);
    assertFalse(wrappedStream.closed);
    nonCloseableOutputStream.close();
    // the close method should just flush the delegate without actually closing it
    assertTrue(wrappedStream.flushed);
    assertFalse(wrappedStream.closed);
  }


  private static class TestStream extends NullOutputStream {
    private boolean flushed, closed;

    @Override
    public void flush() throws IOException {
      flushed = true;
    }

    @Override
    public void close() throws IOException {
      closed = true;
    }
  }
}