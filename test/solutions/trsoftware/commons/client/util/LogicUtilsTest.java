package solutions.trsoftware.commons.client.util;

import static solutions.trsoftware.commons.client.util.LogicUtils.*;
import junit.framework.TestCase;

/**
 * Dec 10, 2008
 *
 * @author Alex
 */
public class LogicUtilsTest extends TestCase {

  public void test_bothNotNullAndEqual() throws Exception {
    assertTrue(bothNotNullAndEqual("foo", "foo"));
    assertFalse(bothNotNullAndEqual("foo", "bar"));
    assertFalse(bothNotNullAndEqual("foo", null));
    assertFalse(bothNotNullAndEqual(null, "foo"));
  }

  public void test_bothNotNullAndNotEqual() throws Exception {
    assertTrue(bothNotNullAndNotEqual("foo", "bar"));
    assertFalse(bothNotNullAndNotEqual("foo", "foo"));
    assertFalse(bothNotNullAndNotEqual("foo", null));
    assertFalse(bothNotNullAndNotEqual(null, "foo"));
  }

  public void test_bothNull() throws Exception {
    assertTrue(bothNull(null, null));
    assertFalse(bothNull("foo", "bar"));
    assertFalse(bothNull("foo", null));
    assertFalse(bothNull(null, "foo"));
  }

  public void test_bothNotNull() throws Exception {
    assertTrue(bothNotNull("foo", "foo"));
    assertTrue(bothNotNull("foo", "bar"));
    assertFalse(bothNotNull("foo", null));
    assertFalse(bothNotNull(null, "foo"));
  }

  public void test_eq() throws Exception {
    assertTrue(eq(null, null));
    assertTrue(eq("foo", "foo"));
    assertFalse(eq("foo", "bar"));
    assertFalse(eq("foo", null));
    assertFalse(eq(null, "foo"));
  }

}