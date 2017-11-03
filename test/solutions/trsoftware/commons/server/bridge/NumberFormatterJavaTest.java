package solutions.trsoftware.commons.server.bridge;

import solutions.trsoftware.commons.client.bridge.NumberFormatterTestBridge;
import solutions.trsoftware.commons.server.bridge.text.NumberFormatterJavaImpl;
import junit.framework.TestCase;

/**
 * This class uses NumberFormatterTestBridge as a delegate (which provides a way
 * to call the same test methods from both Java and GWT test contexts).
 *
 * @author Alex
 */
public class NumberFormatterJavaTest extends TestCase {
  NumberFormatterTestBridge delegate = new NumberFormatterTestBridge(){};

  @Override
  public void setUp() throws Exception {
    super.setUp();
    delegate.setUp();
  }

  public void testCorrectInstanceUsed() throws Exception {
    delegate.testCorrectInstanceUsed(NumberFormatterJavaImpl.class);
  }

  public void testFormattingDeterministically() {
    delegate.testFormattingDeterministically();
  }
}