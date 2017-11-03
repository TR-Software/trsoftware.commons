package solutions.trsoftware.commons.client.bridge;

import solutions.trsoftware.commons.client.bridge.util.RandomGen;
import solutions.trsoftware.commons.server.bridge.util.RandomGenJavaImpl;

/**
 * Date: Nov 28, 2008 Time: 6:11:18 PM
 *
 * @author Alex
 */
public class RandomGenJavaTest extends RandomGenTestCase {

  public void testCorrectInstanceUsed() throws Exception {
    // this test is running in Java, not GWT
    assertTrue(RandomGen.getInstance() instanceof RandomGenJavaImpl);
  }

}