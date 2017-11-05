/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.testutil;

import com.google.gwt.user.client.Element;
import junit.framework.AssertionFailedError;
import solutions.trsoftware.commons.client.util.GwtUtils;
import solutions.trsoftware.commons.shared.util.ComparisonOperator;
import solutions.trsoftware.commons.shared.util.callables.Function0_t;
import solutions.trsoftware.commons.shared.util.template.Template;

import java.util.*;

import static junit.framework.Assert.*;
import static solutions.trsoftware.commons.shared.util.CollectionUtils.asList;

/**
 * Date: Nov 28, 2008 Time: 6:25:03 PM
 *
 * @author Alex
 */
public abstract class AssertUtils {
  public static final double EPSILON = 0.0001; // a tiny constant used to ignore precision loss with flops

  protected AssertUtils() {

  }

  /** Makes sure all the arguments are equal to each other */
  public static void assertAllEqual(Object... args) {
    for (int i = 1; i < args.length; i++) {
      assertEquals(args[i - 1], args[i]);
    }
  }

  /** Makes sure all the arguments are equal to each other */
  public static void assertAllEqualAndNotNull(Object... args) {
    for (int i = 1; i < args.length; i++) {
      assertNotNull(args[i - 1]);
      assertEquals(args[i - 1], args[i]);
    }
  }

  /**
   * Asserts that running the given code capsule results in an exception of the same type and having the same
   * message as the given argument.
   */
  public static <T extends Throwable> void assertThrows(T expected, final Runnable code) {
    assertThrowableMessageEquals(expected, assertThrows(expected.getClass(), code));
  }

  /**
   * Asserts that running the given code capsule results in an exception of the same type and having the same
   * message as the given argument.
   */
  public static <T extends Throwable> void assertThrows(T expected, Function0_t<? extends Throwable> code) {
    assertThrowableMessageEquals(expected, assertThrows(expected.getClass(), code));
  }

  /**
   * Asserts that the given {@link Throwable} instances contain the same message string.
   */
  public static void assertThrowableMessageEquals(Throwable expected, Throwable actual) {
    assertEquals(expected.getMessage(), actual.getMessage());
  }

  /**
   * Asserts that running the given code capsule results in the given exception type.
   * @return The caught exception so that it may be examined by the caller.
   */
  public static <T extends Throwable> T assertThrows(Class<T> expectedThrowableClass, final Runnable code) {
    return assertThrows(expectedThrowableClass, new Function0_t<T>() {
      public void call() throws T {
        code.run();
      }
    });
  }

  /**
   * Asserts that running the given code capsule results in the given exception
   * type
   *
   * @return The caught exception so that it may be examined by the caller.
   */
  public static <T extends Throwable> T assertThrows(Class<T> expectedThrowableClass, Function0_t<? extends Throwable> code) {
    Throwable caught = null;
    try {
      code.call();
    }
    catch (Throwable ex) {
      caught = ex;
      // need this extra step because the try block could have thrown a valid JUnit AssertionFailedError
      // or some other unpredictable error, which we need to rethrow (assuming that we're not actually expecting to get an AssertionFailedError)
      if (!GwtUtils.isAssignableFrom(AssertionFailedError.class, expectedThrowableClass) && ex instanceof AssertionFailedError)
        throw (AssertionFailedError)ex;
      else if (!GwtUtils.isAssignableFrom(expectedThrowableClass, ex.getClass()))
        throw new Error("We were expecting an instance of " + expectedThrowableClass.getName() + " but instead got this exception you see in cause", ex);
    }
    assertNotNull(expectedThrowableClass.getName() + " expected but wasn't thrown.", caught);
    assertTrue(GwtUtils.isAssignableFrom(expectedThrowableClass, caught.getClass()));
    return (T)caught;  // this cast should succeed because we've asserted that the class we expect is assignable from the exception
  }

  /**
   * Asserts that no given object is .equals() to any other and that none of
   * them are null.
   */
  public static void assertNotEqualAndNotNull(Object... objects) {
    assertNotNull(objects);
    for (int i = 0; i < objects.length; i++) {
      assertNotNull(objects[i]);
      for (int j = i + 1; j < objects.length; j++) {
        assertNotNull(objects[j]);
        assertFalse("expected:<" + objects[i] + "> not equal to:<" + objects[j] + ">", objects[i].equals(objects[j]));
      }
    }
  }

  /**
   * Asserts that no given object is .equals() to any other.
   */
  public static void assertNotEqual(Object... objects) {
    assertNotNull(objects);
    assertTrue(objects.length > 1);
    for (int i = 0; i < objects.length; i++) {
      for (int j = i + 1; j < objects.length; j++) {
        if (objects[i] == null)
          assertNotNull(objects[j]);
        else if (objects[j] == null)
          assertNotNull(objects[i]);
        else
          assertFalse("expected:<" + objects[i] + "> should not be equal to:<" + objects[j] + ">", objects[i].equals(objects[j]));
      }
    }
  }

  public static String comparisonFailedMessage(String message, Object expected, Object actual) {
    String formatted = "";
    if (message != null)
      formatted = message + " ";
    return formatted + "expected:<" + expected + "> but was:<" + actual + ">";
  }

  public static void assertEqualAndNotNull(Object expected, Object actual) {
    assertNotNull(expected);
    assertEquals(expected, actual);
  }

  public static void assertSameSequence(Enumeration expected, Enumeration actual) {
    while (true) {
      if (!expected.hasMoreElements()) {
        assertTrue(!actual.hasMoreElements());
        break;  // both enums are finished
      }
      assertTrue(actual.hasMoreElements());
      assertEquals(expected.nextElement(), actual.nextElement());
    }
  }

  public static <T> void assertSameSequence(Iterator<T> expected, Iterator<T> actual) {
    assertEquals(asList(expected), asList(actual));
  }

  /**
   * Passes if the two specified arrays of longs are <i>equal</i> to one
   * another.  Two arrays are considered equal if both arrays contain the same
   * number of elements, and all corresponding pairs of elements in the two
   * arrays are equal.  In other words, two arrays are equal if they contain the
   * same elements in the same order.  Also, two array references are considered
   * equal if both are <tt>null</tt>.<p>
   *
   * @param a one array to be tested for equality
   * @param a2 the other array to be tested for equality
   * @return <tt>true</tt> if the two arrays are equal
   */
  public static void assertArraysEqual(long[] a, long[] a2) {
    boolean pass = Arrays.equals(a, a2);
    if (!pass)
      fail(comparisonFailedMessage("Arrays not equal.", Arrays.toString(a), Arrays.toString(a2)));
  }

  /**
   * Passes if the two specified arrays of ints are <i>equal</i> to one another.
   *  Two arrays are considered equal if both arrays contain the same number of
   * elements, and all corresponding pairs of elements in the two arrays are
   * equal.  In other words, two arrays are equal if they contain the same
   * elements in the same order.  Also, two array references are considered
   * equal if both are <tt>null</tt>.<p>
   *
   * @param a one array to be tested for equality
   * @param a2 the other array to be tested for equality
   */
  public static void assertArraysEqual(int[] a, int[] a2) {
    boolean pass = Arrays.equals(a, a2);
    if (!pass)
      fail(comparisonFailedMessage("Arrays not equal.", Arrays.toString(a), Arrays.toString(a2)));
  }

  /**
   * Passes if the two specified arrays of shorts are <i>equal</i> to one
   * another.  Two arrays are considered equal if both arrays contain the same
   * number of elements, and all corresponding pairs of elements in the two
   * arrays are equal.  In other words, two arrays are equal if they contain the
   * same elements in the same order.  Also, two array references are considered
   * equal if both are <tt>null</tt>.<p>
   *
   * @param a one array to be tested for equality
   * @param a2 the other array to be tested for equality
   */
  public static void assertArraysEqual(short[] a, short a2[]) {
    boolean pass = Arrays.equals(a, a2);
    if (!pass)
      fail(comparisonFailedMessage("Arrays not equal.", Arrays.toString(a), Arrays.toString(a2)));
  }

  /**
   * Passes if the two specified arrays of chars are <i>equal</i> to one
   * another.  Two arrays are considered equal if both arrays contain the same
   * number of elements, and all corresponding pairs of elements in the two
   * arrays are equal.  In other words, two arrays are equal if they contain the
   * same elements in the same order.  Also, two array references are considered
   * equal if both are <tt>null</tt>.<p>
   *
   * @param a one array to be tested for equality
   * @param a2 the other array to be tested for equality
   */
  public static void assertArraysEqual(char[] a, char[] a2) {
    boolean pass = Arrays.equals(a, a2);
    if (!pass)
      fail(comparisonFailedMessage("Arrays not equal.", Arrays.toString(a), Arrays.toString(a2)));
  }

  /**
   * Passes if the two specified arrays of bytes are <i>equal</i> to one
   * another.  Two arrays are considered equal if both arrays contain the same
   * number of elements, and all corresponding pairs of elements in the two
   * arrays are equal.  In other words, two arrays are equal if they contain the
   * same elements in the same order.  Also, two array references are considered
   * equal if both are <tt>null</tt>.<p>
   *
   * @param a one array to be tested for equality
   * @param a2 the other array to be tested for equality
   */
  public static void assertArraysEqual(byte[] a, byte[] a2) {
    boolean pass = Arrays.equals(a, a2);
    if (!pass)
      fail(comparisonFailedMessage("Arrays not equal.", Arrays.toString(a), Arrays.toString(a2)));
  }

  /**
   * Passes if the two specified arrays of booleans are <i>equal</i> to one
   * another.  Two arrays are considered equal if both arrays contain the same
   * number of elements, and all corresponding pairs of elements in the two
   * arrays are equal.  In other words, two arrays are equal if they contain the
   * same elements in the same order.  Also, two array references are considered
   * equal if both are <tt>null</tt>.<p>
   *
   * @param a one array to be tested for equality
   * @param a2 the other array to be tested for equality
   */
  public static void assertArraysEqual(boolean[] a, boolean[] a2) {
    boolean pass = Arrays.equals(a, a2);
    if (!pass)
      fail(comparisonFailedMessage("Arrays not equal.", Arrays.toString(a), Arrays.toString(a2)));
  }

  /**
   * Passes if the two specified arrays of doubles are <i>equal</i> to one
   * another.  Two arrays are considered equal if both arrays contain the same
   * number of elements, and all corresponding pairs of elements in the two
   * arrays are equal.  In other words, two arrays are equal if they contain the
   * same elements in the same order.  Also, two array references are considered
   * equal if both are <tt>null</tt>.<p>
   *
   * Two doubles <tt>d1</tt> and <tt>d2</tt> are considered equal if:
   * <pre>    <tt>new Double(d1).equals(new Double(d2))</tt></pre>
   * (Unlike the <tt>==</tt> operator, this method considers <tt>NaN</tt> equals
   * to itself, and 0.0d unequal to -0.0d.)
   *
   * @param a one array to be tested for equality
   * @param a2 the other array to be tested for equality
   * @see Double#equals(Object)
   */
  public static void assertArraysEqual(double[] a, double[] a2) {
    boolean pass = Arrays.equals(a, a2);
    if (!pass)
      fail(comparisonFailedMessage("Arrays not equal.", Arrays.toString(a), Arrays.toString(a2)));
  }

  /**
   * Passes if the two specified arrays of floats are <i>equal</i> to one
   * another.  Two arrays are considered equal if both arrays contain the same
   * number of elements, and all corresponding pairs of elements in the two
   * arrays are equal.  In other words, two arrays are equal if they contain the
   * same elements in the same order.  Also, two array references are considered
   * equal if both are <tt>null</tt>.<p>
   *
   * Two floats <tt>f1</tt> and <tt>f2</tt> are considered equal if:
   * <pre>    <tt>new Float(f1).equals(new Float(f2))</tt></pre>
   * (Unlike the <tt>==</tt> operator, this method considers <tt>NaN</tt> equals
   * to itself, and 0.0f unequal to -0.0f.)
   *
   * @param a one array to be tested for equality
   * @param a2 the other array to be tested for equality
   * @see Float#equals(Object)
   */
  public static void assertArraysEqual(float[] a, float[] a2) {
    boolean pass = Arrays.equals(a, a2);
    if (!pass)
      fail(comparisonFailedMessage("Arrays not equal.", Arrays.toString(a), Arrays.toString(a2)));
  }


  /**
   * Passes if the two specified arrays of Objects are <i>equal</i> to one
   * another.  The two arrays are considered equal if both arrays contain the
   * same number of elements, and all corresponding pairs of elements in the two
   * arrays are equal.  Two objects <tt>e1</tt> and <tt>e2</tt> are considered
   * <i>equal</i> if <tt>(e1==null ? e2==null : e1.equals(e2))</tt>.  In other
   * words, the two arrays are equal if they contain the same elements in the
   * same order.  Also, two array references are considered equal if both are
   * <tt>null</tt>.<p>
   *
   * @param a one array to be tested for equality
   * @param a2 the other array to be tested for equality
   */
  public static void assertArraysEqual(Object[] a, Object[] a2) {
    boolean pass = Arrays.equals(a, a2);
    if (!pass)
      fail(comparisonFailedMessage("Arrays not equal.", Arrays.toString(a), Arrays.toString(a2)));
  }

  public static void assertEqualsAndHashCode(Object a, Object b) throws Exception {
    assertTrue(a.equals(b));
    assertTrue(a.hashCode() == b.hashCode());
  }

  public static void assertNotEqualsAndHashCode(Object a, Object b) throws Exception {
    assertFalse(a.equals(b));
    assertFalse(a.hashCode() == b.hashCode());
  }

  public static <M extends Map> void assertEmpty(M map) {
    assertTrue(map.isEmpty());
    assertEquals(0, map.size());
  }

  public static <C extends Collection> void assertEmpty(C collection) {
    assertTrue(collection.isEmpty());
    assertEquals(0, collection.size());
  }

  public static <C extends Comparable<C>> void assertComparablesEqual(C a, C b) {
    assertTrue(a.compareTo(b) == 0);
  }

  public static <C extends Comparable<C>> void assertLessThan(C a, C b) {
    assertTrue(a.compareTo(b) < 0);
    assertTrue(b.compareTo(a) > 0);
  }

  // AssertionBuilder methods: --------------------------------------------------------------------------------

  /** @return a builder for specifying a chain of assertions on the given object */
  public static <T> AssertionBuilder<T, AssertionBuilder> assertThat(T value) {
    return new AssertionBuilder<T, AssertionBuilder>(value);
  }

  /** @return a builder for specifying a chain of assertions on the given number */
  public static <T extends Comparable<T>> ComparableAssertionBuilder<T> assertThat(T value) {
    return new ComparableAssertionBuilder<T>(value);
  }

  /** @return a builder for specifying a chain of assertions on the given string */
  public static StringAssertionBuilder assertThat(String value) {
    return new StringAssertionBuilder(value);
  }

  public static void assertElementTextEquals(Element element, String expectedTextAndHtml) {
    assertEquals(expectedTextAndHtml, element.getInnerText());
    assertEquals(expectedTextAndHtml, element.getInnerHTML());
  }

  /**
   * Allows chaining assertions (sort of like a simpler version of the AssertJ library).
   * The simplest way to use this class is by calling {@link #assertThat(Object)} and chaining the assertions
   * to the result.
   *
   * @param <V> the value type
   * @see <a href="http://joel-costigliola.github.io/assertj/assertj-core-quick-start.html">AssertJ</a>
   */
  public static class AssertionBuilder<V, T extends AssertionBuilder> {
    protected final V value;

    public AssertionBuilder(V value) {
      this.value = value;
    }

    public T isEqualTo(V expected) {
      assertEquals(expected, value);
      return (T)this;
    }

    public T isNotEqualTo(V expected) {
      assertNotEqual(expected, value);
      return (T)this;
    }

    public T isNull() {
      assertNull(value);
      return (T)this;
    }

    public T isNotNull() {
      assertNotNull(value);
      return (T)this;
    }
  }

  /**
   * Allows chaining additional assertions for comparable types.
   *
   * The simplest way to use this class is by calling {@link #assertThat(Comparable)} and chaining the assertions
   * to the result.
   */
  public static class ComparableAssertionBuilder<T extends Comparable<T>> extends AssertionBuilder<T, ComparableAssertionBuilder> {

    public ComparableAssertionBuilder(T value) {
      super(value);
    }

    private ComparableAssertionBuilder<T> compare(ComparisonOperator op, T arg) {
      assertTrue(Template.printf("%value %op %arg", value, op, arg), op.compare(value, arg));
      return this;
    }

    @Override
    public ComparableAssertionBuilder isEqualTo(T expected) {
      return compare(ComparisonOperator.EQ, expected);
    }

    @Override
    public ComparableAssertionBuilder isNotEqualTo(T expected) {
      return compare(ComparisonOperator.NEQ, expected);
    }

    public ComparableAssertionBuilder<T> isLessThan(T upperBound) {
      return compare(ComparisonOperator.LT, upperBound);
    }

    public ComparableAssertionBuilder<T> isLessThanOrEqualTo(T upperBound) {
      return compare(ComparisonOperator.LTE, upperBound);
    }

    public ComparableAssertionBuilder<T> isGreaterThan(T lowerBound) {
      return compare(ComparisonOperator.GT, lowerBound);
    }

    public ComparableAssertionBuilder<T> isGreaterThanOrEqualTo(T lowerBound) {
      return compare(ComparisonOperator.GTE, lowerBound);
    }

    /** Assert that {@link #value} is in the range {@code [lowerBound, upperBound]} */
    public ComparableAssertionBuilder<T> isBetween(T lowerBound, T upperBound) {
      assertTrue(Template.printf("Invalid interval: [%a, %b]", lowerBound, upperBound), lowerBound.compareTo(upperBound) <= 0);
      return isGreaterThanOrEqualTo(lowerBound).isLessThanOrEqualTo(upperBound);
    }
  }

  /**
   * Allows chaining additional assertions for string types.
   *
   * The simplest way to use this class is by calling {@link #assertThat(Number)} and chaining the assertions
   * to the result.
   */
  public static class StringAssertionBuilder extends AssertionBuilder<String, StringAssertionBuilder> {

    public StringAssertionBuilder(String value) {
      super(value);
    }

    public StringAssertionBuilder matchesRegex(String regex) {
      assertTrue(Template.printf("The string \"%s\" doesn't match pattern /%regex/", value, regex),
          value.matches(regex));
      return this;
    }

    public StringAssertionBuilder isNotEmpty() {
      assertFalse(value.isEmpty());
      return this;
    }

    public StringAssertionBuilder isEmpty() {
      assertTrue(value.isEmpty());
      return this;
    }

  }


}