/*
 * Copyright 2018 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.shared.testutil;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.*;

/**
 * Date: Nov 28, 2008 Time: 6:25:03 PM
 *
 * @author Alex
 */
public class AssertUtilsTest extends TestCase {

  public void testAssertAllEqual() throws Exception {
    assertAllEqual(1);
    assertAllEqual(1, 1);
    assertAllEqual(1, 1, 1);

    assertThrows(AssertionFailedError.class, (Runnable)() -> assertAllEqual(1, 2));
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertAllEqual(1, 1, 2));

    assertAllEqual("a");
    assertAllEqual("a", "a");
    assertAllEqual("a", "a", "a");

    assertThrows(AssertionFailedError.class, (Runnable)() -> assertAllEqual("a", "b"));
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertAllEqual("a", "a", "b"));

  }

  public void testAssertAllEqualAndNotNull() throws Exception {
    assertAllEqualAndNotNull(1);
    assertAllEqualAndNotNull(1, 1);
    assertAllEqualAndNotNull(1, 1, 1);

    assertThrows(AssertionFailedError.class, (Runnable)() -> assertAllEqualAndNotNull(1, 2));
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertAllEqualAndNotNull(1, 1, 2));
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertAllEqualAndNotNull(null, 1, 2));

    assertAllEqualAndNotNull("a");
    assertAllEqualAndNotNull("a", "a");
    assertAllEqualAndNotNull("a", "a", "a");

    assertThrows(AssertionFailedError.class, (Runnable)() -> assertAllEqualAndNotNull("a", "b"));
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertAllEqualAndNotNull("a", "a", "b"));
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertAllEqualAndNotNull("a", "a", null));
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertAllEqualAndNotNull("a", null, "a"));
  }

  public void testAssertThrows() throws Exception {
    // test an obvious case first
    assertThrows(IllegalArgumentException.class, (Runnable)() -> {
      throw new IllegalArgumentException();
    });

    {
      // now make sure that if a valid JUnit assertion arises in the given block,
      // that assertion is propagated instead of failing because the expected
      // exception isn't thrown
      Throwable caught = null;
      try {
        assertThrows(IllegalArgumentException.class, (Runnable)() -> fail("Calling fail()"));
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
        assertThrows(IllegalArgumentException.class, (Runnable)() -> {
          throw new UnsupportedOperationException("Testing with a UOE");
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
    assertNotEqualAndNotNull("a");
    assertNotEqualAndNotNull("a", "b");
    assertNotEqualAndNotNull("a", "b", "c");
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertNotEqualAndNotNull(null));
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertNotEqualAndNotNull("a", "b", null));
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertNotEqualAndNotNull("a", "a"));
  }

  public void testAssertNotEqual() throws Exception {
    assertNotEqual("a", "b");
    assertNotEqual((Object)"a", null);
    assertNotEqual((Object)null, "b");
    assertNotEqual("a", "b", "c");
    assertNotEqual("a", "b", "c", null);
    assertNotEqual("a", null, "c", "d");
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertNotEqual(null));
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertNotEqual("a"));
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertNotEqual("a", "b", "a"));
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertNotEqual("a", null, null));
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertNotEqual("a", "a"));
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertNotEqual(null, null));
  }

  public void testAssertIntArraysEqual() throws Exception {
    assertArraysEqual(new int[]{1, 2, 3}, new int[]{1, 2, 3});
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
        (Runnable)() -> assertArraysEqual(new int[]{1, 2, 3}, new int[]{1, 2, 4}));
    assertEquals("Arrays not equal. expected:<[1, 2, 3]> but was:<[1, 2, 4]>", error.getMessage());
  }
  
  public void testAssertLongArraysEqual() throws Exception {
    assertArraysEqual(new long[]{1, 2, 3}, new long[]{1, 2, 3});
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
        (Runnable)() -> assertArraysEqual(new long[]{1, 2, 3}, new long[]{1, 2, 4}));
    assertEquals("Arrays not equal. expected:<[1, 2, 3]> but was:<[1, 2, 4]>", error.getMessage());
  }

  public void testAssertShortArraysEqual() throws Exception {
    assertArraysEqual(new short[]{1, 2, 3}, new short[]{1, 2, 3});
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
        (Runnable)() -> assertArraysEqual(new short[]{1, 2, 3}, new short[]{1, 2, 4}));
    assertEquals("Arrays not equal. expected:<[1, 2, 3]> but was:<[1, 2, 4]>", error.getMessage());
  }

  public void testAssertByteArraysEqual() throws Exception {
    assertArraysEqual(new byte[]{1, 2, 3}, new byte[]{1, 2, 3});
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
        (Runnable)() -> assertArraysEqual(new byte[]{1, 2, 3}, new byte[]{1, 2, 4}));
    assertEquals("Arrays not equal. expected:<[1, 2, 3]> but was:<[1, 2, 4]>", error.getMessage());
  }

  public void testAssertFloatArraysEqual() throws Exception {
    assertArraysEqual(new float[]{1, 2, 3}, new float[]{1, 2, 3});
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
        (Runnable)() -> assertArraysEqual(new float[]{1, 2, 3}, new float[]{1, 2, 4}));
    assertEquals("Arrays not equal. expected:<[1.0, 2.0, 3.0]> but was:<[1.0, 2.0, 4.0]>", error.getMessage());
  }

  public void testAssertDoubleArraysEqual() throws Exception {
    assertArraysEqual(new double[]{1, 2, 3}, new double[]{1, 2, 3});
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
        (Runnable)() -> assertArraysEqual(new double[]{1, 2, 3}, new double[]{1, 2, 4}));
    assertEquals("Arrays not equal. expected:<[1.0, 2.0, 3.0]> but was:<[1.0, 2.0, 4.0]>", error.getMessage());
  }

  public void testAssertBooleanArraysEqual() throws Exception {
    assertArraysEqual(new boolean[]{true, true, false}, new boolean[]{true, true, false});
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
        (Runnable)() -> assertArraysEqual(new boolean[]{true, true, false}, new boolean[]{true, false, false}));
    assertEquals("Arrays not equal. expected:<[true, true, false]> but was:<[true, false, false]>", error.getMessage());
  }

  public void testAssertObjectArraysEqual() throws Exception {
    assertArraysEqual(new String[]{"1", "2", "3"}, new String[]{"1", "2", "3"});
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
        (Runnable)() -> assertArraysEqual(new String[]{"1", "2", "3"}, new String[]{"1", "2", "4"}));
    assertEquals("Arrays not equal. expected:<[1, 2, 3]> but was:<[1, 2, 4]>", error.getMessage());
  }

  public void testAssertThat() throws Exception {
    // 1) test an AssertionBuilder with a string arg
    {
      StringAssertionBuilder assFoo = assertThat("foo");
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
      AssertionBuilder assNull = assertThat((Object)null);
      assertNotNull(assNull);
      assertEquals(null, assNull.value);
      assNull.isNull();
    }
    {
      AssertionBuilder assNumberNull = assertThat((Double)null);
      assertNotNull(assNumberNull);
      assertEquals(null, assNumberNull.value);
      assNumberNull.isNull();
    }
    // 3) test an AssertionBuilder with some numeric args
    chainNumberAssertions(5);

    // 4) now test some assertions that should fail
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertThat("foo").isEqualTo("bar"));
    assertThrows(AssertionFailedError.class, (Runnable)() -> {
      assertThat("foo").isEqualTo("foo").isNull();  // the isNull assertion in this chain should fail
    });
    assertThrows(AssertionFailedError.class, (Runnable)() -> {
      assertThat("foo").isNotEqualTo("bar").isEqualTo("bar");  // the is .isEqualTo("bar") assertion should fail
    });
    for (final int value : new int[]{-1, 0, 1, 2, 3, 4, 6, 7, 8}) {
      assertThrows(AssertionFailedError.class, (Runnable)() -> chainNumberAssertions(value));
    }
  }

  private AssertUtils.ComparableAssertionBuilder<Integer> chainNumberAssertions(int value) {
    return assertThat(value).isGreaterThan(0).isGreaterThan(4).isGreaterThanOrEqualTo(4).isGreaterThanOrEqualTo(5)
        .isLessThanOrEqualTo(5).isLessThanOrEqualTo(6).isLessThan(6).isBetween(5, 5).isBetween(4, 5).isBetween(5, 6).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

  public void testAssertComparablesEqual() throws Exception {
    assertComparablesEqual(1, 1);
    assertComparablesEqual("foo", "foo");
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertComparablesEqual(0, 1));
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertComparablesEqual("foo", "bar"));
  }

  public void testAssertComparablesNotEqual() throws Exception {
    assertComparablesNotEqual(0, 1);
    assertComparablesNotEqual("foo", "bar");
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertComparablesNotEqual(1, 1));
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertComparablesNotEqual("foo", "foo"));
  }

  public void testAssertComparablesOrdering() throws Exception {
    assertComparablesOrdering();
    assertComparablesOrdering(0);
    assertComparablesOrdering(1);
    assertComparablesOrdering(0, 1);
    assertComparablesOrdering(-1, 0, 1, 2);
    assertComparablesOrdering("bar", "foo");
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertComparablesOrdering(1, 0));
    assertThrows(AssertionFailedError.class, (Runnable)() -> assertComparablesOrdering("foo", "foo"));
  }
}