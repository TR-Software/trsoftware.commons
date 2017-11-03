package solutions.trsoftware.commons.client.util;
/**
 *
 * Date: Oct 17, 2008
 * Time: 3:45:23 PM
 * @author Alex
 */

import junit.framework.TestCase;

public class ReadOnlyReferenceTest extends TestCase {

  public void testReadOnlyReference() throws Exception {
    {
      ReadOnlyReference<String> r = new ReadOnlyReference<String>("foo");
      assertEquals("foo", r.get());
      try {
        r.set("bar");  // can't overwrite existing value
        fail("IllegalStateException expected");
      }
      catch (Exception e) {
        assertTrue(e instanceof IllegalStateException);
      }
    }
    {
      ReadOnlyReference<String> r = new ReadOnlyReference<String>();
      assertEquals(null, r.get());
      r.set("foo");
      assertEquals("foo", r.get());
      try {
        r.set("bar");  // can't overwrite existing value
        fail("IllegalStateException expected");
      }
      catch (Exception e) {
        assertTrue(e instanceof IllegalStateException);
      }
    }
  }
}