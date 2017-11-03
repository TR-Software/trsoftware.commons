package solutions.trsoftware.commons.client.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;

import static solutions.trsoftware.commons.client.testutil.AssertUtils.assertThrows;

public class AssertTest extends TestCase {

  public void testAssertTrue() throws Exception {
    Assert.assertTrue(true);
    Assert.assertTrue(true, "Foo");
    AssertUtils.assertThrows(new AssertionError(Assert.DEFAULT_ERROR_MSG), new Runnable() {
      @Override
      public void run() {
        Assert.assertTrue(false);
      }
    });
    AssertUtils.assertThrows(new AssertionError("Foo"), new Runnable() {
      @Override
      public void run() {
        Assert.assertTrue(false, "Foo");
      }
    });
  }

  public void testAssertNotNull() throws Exception {
    for (Object arg : new Object[]{"asdf", new Object()}) {
      assertEquals(arg, Assert.assertNotNull(arg));
    }
    AssertUtils.assertThrows(new NullPointerException(), new Runnable() {
      @Override
      public void run() {
        Assert.assertNotNull(null);
      }
    });
    AssertUtils.assertThrows(new NullPointerException("Foo"), new Runnable() {
      @Override
      public void run() {
        Assert.assertNotNull(null, "Foo");
      }
    });
  }

  public void testFail() throws Exception {
    AssertUtils.assertThrows(new AssertionError("Foo"), new Runnable() {
      @Override
      public void run() {
        Assert.fail("Foo");
      }
    });
  }

  public void testAssertEquals() throws Exception {
    Assert.assertEquals("asdf", "asdf");
    Assert.assertEquals(null, null);
    checkEqualityFail("asdf", null);
    checkEqualityFail(null, "asdf");
    checkEqualityFail(new Object(), "asdf");
  }

  private static void checkEqualityFail(final Object expected, final Object actual) {
    AssertUtils.assertThrows(new AssertionError(Assert.DEFAULT_ERROR_MSG + ": " + "expected:<" + expected + "> but was:<" + actual + ">"), new Runnable() {
      @Override
      public void run() {
        Assert.assertEquals(expected, actual);
      }
    });
  }
}