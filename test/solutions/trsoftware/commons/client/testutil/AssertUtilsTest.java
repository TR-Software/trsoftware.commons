package solutions.trsoftware.commons.client.testutil;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Date: Nov 28, 2008 Time: 6:25:03 PM
 *
 * @author Alex
 */
public class AssertUtilsTest extends TestCase {

  public void testAssertAllEqual() throws Exception {
    AssertUtils.assertAllEqual(1);
    AssertUtils.assertAllEqual(1, 1);
    AssertUtils.assertAllEqual(1, 1, 1);

    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertAllEqual(1, 2);
      }
    });

    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertAllEqual(1, 1, 2);
      }
    });

    AssertUtils.assertAllEqual("a");
    AssertUtils.assertAllEqual("a", "a");
    AssertUtils.assertAllEqual("a", "a", "a");

    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertAllEqual("a", "b");
      }
    });

    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertAllEqual("a", "a", "b");
      }
    });

  }

  public void testAssertAllEqualAndNotNull() throws Exception {
    AssertUtils.assertAllEqualAndNotNull(1);
    AssertUtils.assertAllEqualAndNotNull(1, 1);
    AssertUtils.assertAllEqualAndNotNull(1, 1, 1);

    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertAllEqualAndNotNull(1, 2);
      }
    });

    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertAllEqualAndNotNull(1, 1, 2);
      }
    });

    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertAllEqualAndNotNull(null, 1, 2);
      }
    });

    AssertUtils.assertAllEqualAndNotNull("a");
    AssertUtils.assertAllEqualAndNotNull("a", "a");
    AssertUtils.assertAllEqualAndNotNull("a", "a", "a");

    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertAllEqualAndNotNull("a", "b");
      }
    });

    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertAllEqualAndNotNull("a", "a", "b");
      }
    });

    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertAllEqualAndNotNull("a", "a", null);
      }
    });
    
    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertAllEqualAndNotNull("a", null, "a");
      }
    });
  }

  public void testAssertThrows() throws Exception {
    // test an obvious case first
    AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
      public void run() {
        throw new IllegalArgumentException();
      }
    });

    {
      // now make sure that if a valid JUnit assertion arises in the given block,
      // that assertion is propagated instead of failing because the expected
      // exception isn't thrown
      Throwable caught = null;
      try {
        AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
          public void run() {
            fail("Calling fail()");
          }
        });
      }
      catch (Throwable ex) {
        caught = ex;
      }

      assertTrue(caught instanceof AssertionFailedError);
      assertEquals("Calling fail()", caught.getMessage());
    }
    {
      // now try it with a code block that throws a different type of exception from what we expect
      Throwable caught = null;
      try {
        AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
          public void run() {
            throw new UnsupportedOperationException("Testing with a UOE");
          }
        });
      }
      catch (Throwable ex) {
        caught = ex;
      }

      // the assertThrows method should have wrapped the UnsupportedOperationException inside an Error
      assertNotNull(caught);
      assertTrue(caught instanceof Error);
      Throwable cause = caught.getCause();
      assertNotNull(cause);
      assertTrue(cause instanceof UnsupportedOperationException);
      assertEquals("Testing with a UOE", cause.getMessage());
    }
  }

  public void testAssertNotEqualAndNotNull() throws Exception {
    AssertUtils.assertNotEqualAndNotNull("a");
    AssertUtils.assertNotEqualAndNotNull("a", "b");
    AssertUtils.assertNotEqualAndNotNull("a", "b", "c");
    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertNotEqualAndNotNull(null);
      }
    });
    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertNotEqualAndNotNull("a", "b", null);
      }
    });
    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertNotEqualAndNotNull("a", "a");
      }
    });
  }

  public void testAssertNotEqual() throws Exception {
    AssertUtils.assertNotEqual("a", "b");
    AssertUtils.assertNotEqual("a", null);
    AssertUtils.assertNotEqual(null, "b");
    AssertUtils.assertNotEqual("a", "b", "c");
    AssertUtils.assertNotEqual("a", "b", "c", null);
    AssertUtils.assertNotEqual("a", null, "c", "d");
    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertNotEqual(null);
      }
    });
    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertNotEqual("a");
      }
    });
    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertNotEqual("a", "b", "a");
      }
    });
    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertNotEqual("a", null, null);
      }
    });
    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertNotEqual("a", "a");
      }
    });
    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertNotEqual(null, null);
      }
    });
  }

  public void testAssertIntArraysEqual() throws Exception {
    AssertUtils.assertArraysEqual(new int[]{1, 2, 3}, new int[]{1, 2, 3});
    AssertionFailedError error = AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertArraysEqual(new int[]{1, 2, 3}, new int[]{1, 2, 4});
      }
    });
    assertEquals("Arrays not equal. expected:<[1, 2, 3]> but was:<[1, 2, 4]>", error.getMessage());
  }
  
  public void testAssertLongArraysEqual() throws Exception {
    AssertUtils.assertArraysEqual(new long[]{1, 2, 3}, new long[]{1, 2, 3});
    AssertionFailedError error = AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertArraysEqual(new long[]{1, 2, 3}, new long[]{1, 2, 4});
      }
    });
    assertEquals("Arrays not equal. expected:<[1, 2, 3]> but was:<[1, 2, 4]>", error.getMessage());
  }

  public void testAssertShortArraysEqual() throws Exception {
    AssertUtils.assertArraysEqual(new short[]{1, 2, 3}, new short[]{1, 2, 3});
    AssertionFailedError error = AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertArraysEqual(new short[]{1, 2, 3}, new short[]{1, 2, 4});
      }
    });
    assertEquals("Arrays not equal. expected:<[1, 2, 3]> but was:<[1, 2, 4]>", error.getMessage());
  }

  public void testAssertByteArraysEqual() throws Exception {
    AssertUtils.assertArraysEqual(new byte[]{1, 2, 3}, new byte[]{1, 2, 3});
    AssertionFailedError error = AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertArraysEqual(new byte[]{1, 2, 3}, new byte[]{1, 2, 4});
      }
    });
    assertEquals("Arrays not equal. expected:<[1, 2, 3]> but was:<[1, 2, 4]>", error.getMessage());
  }

  public void testAssertFloatArraysEqual() throws Exception {
    AssertUtils.assertArraysEqual(new float[]{1, 2, 3}, new float[]{1, 2, 3});
    AssertionFailedError error = AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertArraysEqual(new float[]{1, 2, 3}, new float[]{1, 2, 4});
      }
    });
    assertEquals("Arrays not equal. expected:<[1.0, 2.0, 3.0]> but was:<[1.0, 2.0, 4.0]>", error.getMessage());
  }

  public void testAssertDoubleArraysEqual() throws Exception {
    AssertUtils.assertArraysEqual(new double[]{1, 2, 3}, new double[]{1, 2, 3});
    AssertionFailedError error = AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertArraysEqual(new double[]{1, 2, 3}, new double[]{1, 2, 4});
      }
    });
    assertEquals("Arrays not equal. expected:<[1.0, 2.0, 3.0]> but was:<[1.0, 2.0, 4.0]>", error.getMessage());
  }

  public void testAssertBooleanArraysEqual() throws Exception {
    AssertUtils.assertArraysEqual(new boolean[]{true, true, false}, new boolean[]{true, true, false});
    AssertionFailedError error = AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertArraysEqual(new boolean[]{true, true, false}, new boolean[]{true, false, false});
      }
    });
    assertEquals("Arrays not equal. expected:<[true, true, false]> but was:<[true, false, false]>", error.getMessage());
  }

  public void testAssertObjectArraysEqual() throws Exception {
    AssertUtils.assertArraysEqual(new String[]{"1", "2", "3"}, new String[]{"1", "2", "3"});
    AssertionFailedError error = AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertArraysEqual(new String[]{"1", "2", "3"}, new String[]{"1", "2", "4"});
      }
    });
    assertEquals("Arrays not equal. expected:<[1, 2, 3]> but was:<[1, 2, 4]>", error.getMessage());
  }

  public void testAssertThat() throws Exception {
    // 1) test an AssertionBuilder with a string arg
    {
      AssertUtils.StringAssertionBuilder assFoo = AssertUtils.assertThat("foo");
      assertNotNull(assFoo);
      assertEquals("foo", assFoo.value);
      assFoo.isEqualTo("foo");
      assFoo.isNotEqualTo("bar");
      assFoo.isNotNull();
      // now test a chain of all of the above
      assFoo.isEqualTo("foo").isNotEqualTo("bar").isNotNull();
    }
    // 2) test an AssertionBuilder with a null arg
    {
      AssertUtils.AssertionBuilder assNull = AssertUtils.assertThat((Object)null);
      assertNotNull(assNull);
      assertEquals(null, assNull.value);
      assNull.isNull();
    }
    {
      AssertUtils.AssertionBuilder assNumberNull = AssertUtils.assertThat((Double)null);
      assertNotNull(assNumberNull);
      assertEquals(null, assNumberNull.value);
      assNumberNull.isNull();
    }
    // 3) test an AssertionBuilder with some numeric args
    chainNumberAssertions(5);

    // 4) now test some assertions that should fail
    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertThat("foo").isEqualTo("bar");
      }
    });
    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertThat("foo").isEqualTo("foo").isNull();  // the isNull assertion in this chain should fail
      }
    });
    AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
      public void run() {
        AssertUtils.assertThat("foo").isNotEqualTo("bar").isEqualTo("bar");  // the is .isEqualTo("bar") assertion should fail
      }
    });
    for (final int value : new int[]{-1, 0, 1, 2, 3, 4, 6, 7, 8}) {
      AssertUtils.assertThrows(AssertionFailedError.class, new Runnable() {
        public void run() {
          chainNumberAssertions(value);
        }
      });
    }
  }

  private AssertUtils.ComparableAssertionBuilder<Integer> chainNumberAssertions(int value) {
    return AssertUtils.assertThat(value).isGreaterThan(0).isGreaterThan(4).isGreaterThanOrEqualTo(4).isGreaterThanOrEqualTo(5)
        .isLessThanOrEqualTo(5).isLessThanOrEqualTo(6).isLessThan(6).isBetween(5, 5).isBetween(4, 5).isBetween(5, 6).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
  }
}