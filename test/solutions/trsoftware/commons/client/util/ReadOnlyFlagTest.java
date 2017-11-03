package solutions.trsoftware.commons.client.util;

import junit.framework.TestCase;

/**
 * Sep 14, 2009
 *
 * @author Alex
 */
public class ReadOnlyFlagTest extends TestCase {

  public void testReadOnlyFlag() throws Exception {
    ReadOnlyFlag flag = new ReadOnlyFlag();
    assertFalse(flag.isSet());
    assertTrue(flag.set());
    assertTrue(flag.isSet());
    assertFalse(flag.set());  // cannot be set again
    assertTrue(flag.isSet());  // already set
  }
}