package solutions.trsoftware.commons.server.io;

import junit.framework.TestCase;

/**
 * Mar 29, 2011
 *
 * @author Alex
 */
public class StringPrintStreamTest extends TestCase {


  public void testPrintingToString() throws Exception {
    StringPrintStream[] instances = new StringPrintStream[]{
        // test all the different constructor permutations
        new StringPrintStream(),
        new StringPrintStream(2),
        new StringPrintStream(2, true),
        new StringPrintStream(2, true, "UTF-8"),
    };
    for (StringPrintStream s : instances) {
      assertEquals("", s.toString());
      s.printf("foo %d\nbar", 1);
      assertEquals("foo 1\nbar", s.toString());
    }
  }
}