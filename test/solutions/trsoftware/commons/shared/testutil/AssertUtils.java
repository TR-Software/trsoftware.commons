/*
 * Copyright 2022 TR Software Inc.
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
 */

package solutions.trsoftware.commons.shared.testutil;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.gwt.user.client.Element;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import solutions.trsoftware.commons.client.util.GwtUtils;
import solutions.trsoftware.commons.shared.util.ArrayUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.compare.ComparisonOperator;
import solutions.trsoftware.commons.shared.util.function.BiConsumerThrows;
import solutions.trsoftware.commons.shared.util.function.ThrowingRunnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static junit.framework.Assert.*;
import static solutions.trsoftware.commons.shared.util.CollectionUtils.asList;
import static solutions.trsoftware.commons.shared.util.CollectionUtils.first;

/**
 * Date: Nov 28, 2008 Time: 6:25:03 PM
 *
 * @author Alex
 */
public abstract class AssertUtils {

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

  /** Makes sure all the arguments are equal to the first argument */
  @SafeVarargs
  public static <T> void assertAllEqualTo(T expected, T... args) {
    for (int i = 1; i < args.length; i++) {
      assertEquals(expected, args[i]);
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
  public static <T extends Throwable> void assertThrows(T expected, ThrowingRunnable code) {
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
   *
   * @param expectedThrowableClass must be a class (not an interface)
   * @param code will be executed to trigger the expected exception
   * @return The caught exception so that it may be examined by the caller.
   * @throws IllegalArgumentException if the first arg is an interface (it should be an actual class because
   * this method uses {@link GwtUtils#isAssignableFrom(Class, Class)} to check whether the thrown exception is of the
   * expected type)
   */
  public static <T extends Throwable> T assertThrows(Class<T> expectedThrowableClass, final Runnable code) {
    return assertThrows(expectedThrowableClass, (ThrowingRunnable)code::run);
  }

  /**
   * Asserts that running the given code capsule results in the given exception type.
   *
   * @param expectedThrowableClass must be a class (not an interface)
   * @param code will be executed to trigger the expected exception
   * @return The caught exception so that it may be examined by the caller.
   * @throws IllegalArgumentException if the first arg is an interface (it should be an actual class because
   * this method uses {@link GwtUtils#isAssignableFrom(Class, Class)} to check whether the thrown exception is of the
   * expected type)
   */
  public static <T extends Throwable> T assertThrows(Class<T> expectedThrowableClass, final Supplier<?> code) {
    return assertThrows(expectedThrowableClass, (ThrowingRunnable)code::get);
  }

  /**
   * Asserts that running the given code capsule results in the given exception type.
   *
   * @param expectedThrowableClass must be a class (not an interface)
   * @param code will be executed to trigger the expected exception
   * @return The caught exception so that it may be examined by the caller.
   * @throws IllegalArgumentException if the first arg is an interface (it should be an actual class because
   * this method uses {@link GwtUtils#isAssignableFrom(Class, Class)} to check whether the thrown exception is of the
   * expected type)
   */
  @SuppressWarnings("unchecked")
  public static <T extends Throwable> T assertThrows(Class<T> expectedThrowableClass, ThrowingRunnable code) {
    if (expectedThrowableClass.isInterface())
      throw new IllegalArgumentException(expectedThrowableClass + " must be a class (not an interface)");
    Throwable caught = null;
    try {
      code.run();
    }
    catch (Throwable ex) {
      caught = ex;
      // need this extra step because the try block could have thrown a valid JUnit AssertionFailedError
      // or some other unpredictable error, which we need to rethrow (assuming that we're not actually expecting to get an AssertionFailedError)
      if (!GwtUtils.isAssignableFrom(AssertionFailedError.class, expectedThrowableClass) && ex instanceof AssertionFailedError)
        throw (AssertionFailedError)ex;
      else if (!GwtUtils.isAssignableFrom(expectedThrowableClass, ex.getClass()))
        throw new AssertionError("We were expecting an instance of " + expectedThrowableClass.getName() + " but instead got this exception you see in the cause", ex);
    }
    assertNotNull(expectedThrowableClass.getName() + " expected but wasn't thrown.", caught);
    assertTrue(GwtUtils.isAssignableFrom(expectedThrowableClass, caught.getClass()));
    String successMsg = "---------- Caught expected exception: ----------";
    System.out.println(successMsg);
    caught.printStackTrace(System.out);
    System.out.println(StringUtils.repeat('-', successMsg.length()));
    return (T)caught;  // this cast should succeed because we've asserted that the class we expect is assignable from the exception
  }

  /**
   * Asserts that no given object is {@link Object#equals(Object) equal} to any other and that none of
   * them are {@code null}.
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
   * Asserts that no given object is {@link Object#equals(Object) equal} to any other.
   */
  public static void assertNotEqual(Object... objects) {
    assertNotNull(objects);
    assertTrue(objects.length > 1);
    for (int i = 0; i < objects.length; i++) {
      for (int j = i + 1; j < objects.length; j++) {
        assertNotEqual(objects[i], objects[j]);
      }
    }
  }

  public static void assertNotEqual(Object o1, Object o2) {
    if (o1 == null)
      assertNotNull(o2);
    else if (o2 == null)
      assertNotNull(o1);
    else {
      assertFalse("expected:<" + o1 + "> should not be equal to:<" + o2 + ">", o1.equals(o2));
      assertFalse("expected:<" + o2 + "> should not be equal to:<" + o1 + ">", o2.equals(o1));
    }
  }

  /**
   * Combines {@link #assertNotEqual(Object, Object)} and {@link #assertComparablesNotEqual(Comparable, Comparable)}
   */
  public static <T extends Comparable<T>> void assertNotEqual(T o1, T o2) {
    assertNotEqual((Object)o1, o2);
    assertComparablesNotEqual(o1, o2);
  }

  /**
   * Generates the same message as {@link Assert#format(java.lang.String, java.lang.Object, java.lang.Object)}.
   * Adhering to this message format allows the IntelliJ to show diffs for this type of failure.
   *
   * @return a message that can be passed to the {@link AssertionFailedError} constructor
   */
  public static String formatComparisonFailedMessage(String message, Object expected, Object actual) {
    String formatted = "";
    if (message != null)
      formatted = message + " ";
    return formatted + "expected:<" + expected + "> but was:<" + actual + ">";
  }

  /**
   * Same as the private JUnit method {@link Assert#failNotEquals(String, Object, Object)}
   */
  private static void failNotEquals(String message, Object expected, Object actual) {
    fail(formatComparisonFailedMessage(message, expected, actual));
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

  /*
   TODO: can get rid of all the assertArraysEqual methods, because they duplicate those already provided by JUnit
    (see org.junit.Assert.assertArrayEquals(int[], int[]))
  */

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
      fail(formatComparisonFailedMessage("Arrays not equal.", Arrays.toString(a), Arrays.toString(a2)));
  }

  /**
   * Passes if the two specified arrays of ints are <i>equal</i> to one another.
   * Two arrays are considered equal if both arrays contain the same number of
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
      fail(formatComparisonFailedMessage("Arrays not equal.", Arrays.toString(a), Arrays.toString(a2)));
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
  public static void assertArraysEqual(short[] a, short[] a2) {
    boolean pass = Arrays.equals(a, a2);
    if (!pass)
      fail(formatComparisonFailedMessage("Arrays not equal.", Arrays.toString(a), Arrays.toString(a2)));
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
      fail(formatComparisonFailedMessage("Arrays not equal.", Arrays.toString(a), Arrays.toString(a2)));
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
      fail(formatComparisonFailedMessage("Arrays not equal.", Arrays.toString(a), Arrays.toString(a2)));
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
      fail(formatComparisonFailedMessage("Arrays not equal.", Arrays.toString(a), Arrays.toString(a2)));
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
      fail(formatComparisonFailedMessage("Arrays not equal.", Arrays.toString(a), Arrays.toString(a2)));
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
      fail(formatComparisonFailedMessage("Arrays not equal.", Arrays.toString(a), Arrays.toString(a2)));
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
    boolean pass = Arrays.deepEquals(a, a2);
    if (!pass)
      fail(formatComparisonFailedMessage("Arrays not equal.", Arrays.deepToString(a), Arrays.deepToString(a2)));
  }

  /**
   * Asserts that the given args are equal and have the same hash code.
   *
   * @see Object#equals(Object)
   * @see Object#hashCode()
   */
  public static void assertEqualsAndHashCode(Object a, Object b) {
    assertEquals(a, b);
    assertEquals(b, a);
    assertEquals(a.hashCode(), b.hashCode());
  }

  /**
   * Asserts that the given {@link Comparable} objects are equal according to both {@link Object#equals}
   * and {@link Comparable#compareTo}, and that they have the same hash code.
   *
   * In other words, this method combines {@link #assertEqualsAndHashCode(Object, Object)} and
   * {@link #assertComparablesEqual(Comparable, Comparable)}.
   *
   * @see Object#equals(Object)
   * @see Object#hashCode()
   */
  public static <T extends Comparable<T>> void assertEqualsAndHashCode(T a, T b) {
    assertEqualsAndHashCode((Object)a, b);
    assertComparablesEqual(a, b);
  }

  /**
   * Asserts that the given are not equal according to the {@link Object#equals} method, and that
   * they have different hash codes (according to {@link Object#hashCode}).
   * <p>
   * <strong>NOTE</strong>: this assertion makes sense only when you absolutely have to assure that your implementation
   * of {@link Object#hashCode} produces a <em>perfect hashing</em> (i.e. a unique hash when {@link Object#equals}
   * returns {@code false}), despite the fact this behavior is not required by the contract of {@link Object#equals}.
   *
   * @deprecated this assertion is too restrictive, because, as described above, objects are
   * allowed to have the same hash code despite {@link Object#equals} returning {@code false}.
   * In other words, there is no prescribed relationship between {@link Object#equals} and {@link Object#hashCode}
   * Use {@link #assertNotEqual(Object, Object)} instead of this method.
   */
  public static void assertNotEqualsAndHashCode(Object a, Object b) {
    assertNotEqual(a, b);
    assertFalse(a.hashCode() == b.hashCode());
  }

  /*
    NOTE: although the following overloaded methods (assertEmpty & assertContains) don't seem necessary,
    they allow implementations to change the underlying collection type without having to modify the unit tests
   */

  public static void assertEmpty(Map<?, ?> map) {
    assertTrue(map.isEmpty());
    assertEquals(0, map.size());
  }

  public static void assertEmpty(Iterable<?> iterable) {
    assertTrue(Iterables.isEmpty(iterable));
  }

  public static <T> void assertEmpty(T[] array) {
    assertTrue(ArrayUtils.isEmpty(array));
  }

  public static void assertEmpty(Enumeration<?> e) {
    assertFalse(e.hasMoreElements());
  }

  public static void assertContains(Iterable<?> iterable, @Nullable Object element) {
    assertTrue(Iterables.contains(iterable, element));
  }

  public static <T> void assertContains(T[] array, @Nullable T element) {
    assertTrue(ArrayUtils.contains(array, element));
  }

  /**
   * Asserts that the given list contains exactly 1 element and returns that element
   * @return the only element from the list
   * @throws AssertionFailedError if list size != 1
   * @see Iterables#getOnlyElement(Iterable)
   */
  public static <T> T getOnlyElement(List<T> list) {
    assertEquals(1, list.size());
    return list.get(0);
  }

  /**
   * Asserts that the given array contains exactly 1 element and returns that element
   * @return the only element from the array
   * @throws AssertionFailedError if list size != 1
   * @see #getOnlyElement(List)
   */
  public static <T> T getOnlyElement(T[] arr) {
    assertEquals(1, arr.length);
    return arr[0];
  }

  /**
   * Asserts that {@code a} and {@code b} be are "equal" to each-other,
   * according to their {@link Comparable#compareTo(Object)} methods, without checking {@link Object#equals(Object)}.
   */
  public static <T extends Comparable<T>> void assertComparablesEqual(@Nonnull T a, @Nonnull T b) {
    assertTrue(a.compareTo(b) == 0);
    assertTrue(b.compareTo(a) == 0);
  }

  /**
   * Asserts that {@code a != b}, as defined by their {@link Comparable#compareTo(Object)} method.
   */
  public static <T extends Comparable<T>> void assertComparablesNotEqual(@Nonnull T a, @Nonnull T b) {
    assertTrue(a.compareTo(b) != 0);
    assertTrue(b.compareTo(a) != 0);
  }

  /**
   * Asserts that each element in the given sequence is "less than" the next element
   * according to the "natural ordering" defined by {@link Comparable#compareTo(Object)}
   * @see Comparable
   */
  @SafeVarargs
  public static <T extends Comparable<T>> void assertComparablesOrdering(T... comparables) {
    if (comparables.length > 1) {
      for (int i = 1; i < comparables.length; i++) {
        T a = comparables[i - 1];
        T b = comparables[i];
        assertLessThan(a, b);
      }
    }
  }

  /**
   * Asserts that {@code a < b}, as defined by their {@link Comparable#compareTo(Object)} method.
   */
  public static <T extends Comparable<T>> void assertLessThan(T a, T b) {
    assertTrue(StringUtils.template("Expected {$1}.compareTo({$2}) < 0, but was $3", a, b, a.compareTo(b)), a.compareTo(b) < 0);
    assertTrue(StringUtils.template("Expected {$1}.compareTo({$2}) > 0, but was $3", a, b, b.compareTo(a)), b.compareTo(a) > 0);
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
   * Asserts that the given multimap contains exactly 1 entry for the given key and returns its value.
   */
  public static <K, V> V getSingleValue(Multimap<K, V> multimap, K key) {
    Collection<V> values = multimap.get(key);
    assertEquals(1, values.size());
    return first(values);
  }

  public static <T> void assertPredicateResult(Predicate<T> predicate, T arg, boolean expected) {
    assertEquals(expected, predicate.test(arg));
    // also test negation of the predicate
    assertEquals(!expected, predicate.negate().test(arg));
  }

  public static void assertIntPredicateResult(IntPredicate predicate, int arg, boolean expected) {
    assertEquals(expected, predicate.test(arg));
    // also test negation of the predicate
    assertEquals(!expected, predicate.negate().test(arg));
  }

  public static <T> void assertBiPredicateResult(BiPredicate<T, T> predicate, T lhs, T rhs, boolean expected) {
    assertEquals(expected, predicate.test(lhs, rhs));
    // also test negation of the predicate
    assertEquals(!expected, predicate.negate().test(lhs, rhs));
  }

  /**
   * Tests two lists for equality using a custom assertion function to compare the elements.
   * <p>
   * This is similar to {@link junit.framework.Assert#assertEquals(Object, Object) assertEquals(List, List)}, but
   * allows the elements to be tested for equality using something other than {@link Object#equals(Object)}
   *
   * @param equalityAssertion a function that takes a pair of elements and throws an exception if it doesn't
   *     consider them to be equal.
   * @param <E> the type of elements in the given lists
   * @throws AssertionFailedError if the lists differ in size or
   *                              if {@code equalityAssertion} throws an exception for any pair of elements
   */
  public static <E> void assertListsEqual(List<E> expected, List<E> actual, BiConsumerThrows<E, E, Throwable> equalityAssertion) {
    assertEquals("Lists differ in size", expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      E a = expected.get(i);
      E b = actual.get(i);
      // TODO: should this first perform the same null checks as Assert.assertEquals(String, Object, Object)?
      try {
        equalityAssertion.accept(a, b);
      }
      catch (Throwable ex) {
        ex.printStackTrace();
        failNotEquals("Lists differ on element " + i, expected, actual);
      }
    }
  }

  /**
   * Calls {@link #assertListsEqual(List, List, BiPredicate)} with {@link Objects#equals(Object, Object)}
   * as the predicate.
   *
   * @throws AssertionFailedError if the lists differ in size or any pair of elements are not equal
   */
  public static <E extends Object> void assertListsEqual(List<E> expected, List<E> actual) {
    assertListsEqual(expected, actual, (BiPredicate<E, E>)Objects::equals);
  }

  /**
   * Tests two lists for equality using a custom comparison function to compare the elements.
   * <p>
   * This is similar to {@link junit.framework.Assert#assertEquals(Object, Object) assertEquals(List, List)}, but
   * allows the elements to be tested for equality using something other than {@link Object#equals(Object)}
   *
   * @param equalityPredicate a predicate that takes a pair of elements and returns {@code true} if it considers
   *     them equal
   * @param <E> the type of elements in the given lists
   * @throws AssertionFailedError if the lists differ in size or
   *                              if the given predicate returns {@code false} for any pair of elements
   */
  public static <E> void assertListsEqual(List<E> expected, List<E> actual, BiPredicate<E, E> equalityPredicate) {
    assertEquals("Lists differ in size", expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      E a = expected.get(i);
      E b = actual.get(i);
      // TODO: should this first perform the same null checks as Assert.assertEquals(String, Object, Object)?
      if (!equalityPredicate.test(a, b)) {
        failNotEquals("Lists differ on element " + i, expected, actual);
      }
    }
  }

  /**
   * Similar to {@link Assert#assertEquals(Object, Object)}, but uses the given predicate instead of
   * {@link Object#equals(Object)} to compare the given objects.
   *
   * @throws AssertionFailedError if the given predicate returns {@code false} for the given args
   */
  public static <T> void assertEqual(T expected, T actual, BiPredicate<T, T> equalityPredicate) {
    assertEqual(null, expected, actual, equalityPredicate);
  }

  /**
   * Similar to {@link Assert#assertEquals(String, Object, Object)}, but uses the given predicate instead of
   * {@link Object#equals(Object)} to compare the given objects.
   *
   * @throws AssertionFailedError if the given predicate returns {@code false} for the given args
   */
  public static <T> void assertEqual(String message, T expected, T actual, BiPredicate<T, T> equalityPredicate) {
    // TODO: should this first perform the same null checks as Assert.assertEquals(String, Object, Object)?
    if (!equalityPredicate.test(expected, actual))
      failNotEquals(message, expected, actual);
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

    @SuppressWarnings("unchecked")
    public T isEqualTo(V expected) {
      assertEquals(expected, value);
      return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T isNotEqualTo(V expected) {
      assertNotEqual(expected, value);
      return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T isNull() {
      assertNull(value);
      return (T)this;
    }

    @SuppressWarnings("unchecked")
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
  public static class ComparableAssertionBuilder<T extends Comparable<T>> extends AssertionBuilder<T, ComparableAssertionBuilder<T>> {

    public ComparableAssertionBuilder(T value) {
      super(value);
    }

    private ComparableAssertionBuilder<T> compare(ComparisonOperator op, T arg) {
      assertTrue(StringUtils.template("$1 $2 $3", value, op, arg), op.compare(value, arg));
      return this;
    }

    @Override
    public ComparableAssertionBuilder<T> isEqualTo(T expected) {
      return compare(ComparisonOperator.EQ, expected);
    }

    @Override
    public ComparableAssertionBuilder<T> isNotEqualTo(T expected) {
      return compare(ComparisonOperator.NE, expected);
    }

    public ComparableAssertionBuilder<T> isLessThan(T upperBound) {
      return compare(ComparisonOperator.LT, upperBound);
    }

    public ComparableAssertionBuilder<T> isLessThanOrEqualTo(T upperBound) {
      return compare(ComparisonOperator.LE, upperBound);
    }

    public ComparableAssertionBuilder<T> isGreaterThan(T lowerBound) {
      return compare(ComparisonOperator.GT, lowerBound);
    }

    public ComparableAssertionBuilder<T> isGreaterThanOrEqualTo(T lowerBound) {
      return compare(ComparisonOperator.GE, lowerBound);
    }

    /** Assert that {@link #value} is in the range {@code [lowerBound, upperBound]} */
    public ComparableAssertionBuilder<T> isBetween(T lowerBound, T upperBound) {
      assertTrue(StringUtils.template("Invalid interval: [$1, $2]", lowerBound, upperBound), lowerBound.compareTo(upperBound) <= 0);
      return isGreaterThanOrEqualTo(lowerBound).isLessThanOrEqualTo(upperBound);
    }
  }

  /**
   * Allows chaining additional assertions for string types.
   *
   * The simplest way to use this class is by calling {@link #assertThat(String)} and chaining the assertions
   * to the result.
   */
  public static class StringAssertionBuilder extends AssertionBuilder<String, StringAssertionBuilder> {

    public StringAssertionBuilder(String value) {
      super(value);
    }

    public StringAssertionBuilder matchesRegex(String regex) {
      assertTrue(StringUtils.template("The string \"$1\" doesn't match pattern /$2/", value, regex),
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

    public StringAssertionBuilder startsWith(String prefix) {
      assertTrue(StringUtils.template("The string \"$1\" doesn't start with \"$2\"", value, prefix),
          value.startsWith(prefix));
      return this;
    }
    
    public StringAssertionBuilder endsWith(String suffix) {
      assertTrue(StringUtils.template("The string \"$1\" doesn't end with \"$2\"", value, suffix),
          value.endsWith(suffix));
      return this;
    }

    public StringAssertionBuilder contains(String substring) {
      assertTrue(StringUtils.template("The string \"$1\" doesn't contain \"$2\"", value, substring),
          value.contains(substring));
      return this;
    }

  }


}