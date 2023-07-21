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

package solutions.trsoftware.commons.server.testutil;

import com.google.gwt.core.shared.GwtIncompatible;
import solutions.trsoftware.commons.server.util.reflect.MemberSet;
import solutions.trsoftware.commons.server.util.reflect.ObjectDiffs;
import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static junit.framework.Assert.*;

/**
 * Extends {@link AssertUtils} with additional methods that are GWT-incompatible.
 *
 * @author Alex
 */
@GwtIncompatible
public abstract class ServerAssertUtils extends AssertUtils {

  /**
   * Asserts that the given instances have equal values for all public members of their class.
   * @deprecated use {@link #assertEqualsByReflection2(Object, Object)}
   */
  public static <T> void assertEqualsByReflection(T expected, T actual) throws Exception {
    assertEqualsByReflection(expected, actual, new MemberSet<>(assertSameType(expected, actual)).excludeMethodsInheritedFromObject());
  }

  /**
   * Assert that all fields (both public and private) of the given objects are equal, including the fields inherited
   * from any superclass.
   * @see ReflectionUtils#getAllDeclaredFields(Class)
   */
  public static <T> void assertEqualsByReflection2(T expected, T actual) throws Exception {
    // TODO: replace the original assertEqualsByReflection implementation with this method (can rename original); add unit test
    assertEqualsByReflection2(expected, actual, null);
  }

  /**
   * Assert that the fields (both public and private) of the given objects are equal, including the fields inherited
   * from any superclass.
   * <p>
   * The given predicate can be used to exclude certain fields from the comparison.
   *
   * @param filter a field will be compared only if it matches this predicate
   * @see ReflectionUtils#getAllDeclaredFields(Class)
   */
  public static <T> void assertEqualsByReflection2(T expected, T actual, @Nullable Predicate<Field> filter) throws Exception {
    // TODO: replace the original assertEqualsByReflection implementation with this method (can rename original); add unit test
    assertSameType(expected, actual);
    Class<?> cls = expected.getClass();
    Set<Field> allFields = ReflectionUtils.getAllDeclaredFields(cls);
    for (Field field : allFields) {
      if (field.isSynthetic() || (filter != null && !filter.test(field)))
        continue;  // ignore synthetic fields (like "this$0")
      if (!field.isAccessible()) {
        field.setAccessible(true);
        // Note: this override is temporary: future invocations of getDeclaredField will have the original value for isAccessible
      }
      assertDeepEquals(field.toString(), field.get(expected), field.get(actual));
    }
  }

  /**
   * Asserts that the given instances have equal values for all public members matching the given spec.
   */
  public static <T> void assertEqualsByReflection(T expected, T actual, MemberSet<T> memberSpec) throws Exception {
    assertEqualsByReflection(expected, actual, new ObjectDiffs().addReflectionSpec(memberSpec));
  }

  /**
   * Asserts that the given instances are equal according to the given {@linkplain ObjectDiffs differ}.
   */
  public static <T> void assertEqualsByReflection(T expected, T actual, ObjectDiffs differ) throws Exception {
    Class<T> type = assertSameType(expected, actual);
    MemberSet reflectionSpecForType = differ.getReflectionSpecFor(type);
    assertEquals(type, reflectionSpecForType.getType());
    assertFalse(reflectionSpecForType.getFilteredMembers().isEmpty());
    ObjectDiffs.Result diffs = differ.diffValues(expected, actual);
    assertTrue(diffs.toString(), diffs.isEmpty());
  }

  @SuppressWarnings("unchecked")
  public static <T> Class<T> assertSameType(T expected, T actual) {
    assertNotNull(expected);
    assertNotNull(actual);
    final Class<T> expectedClass = (Class<T>)expected.getClass();
    assertEquals(expectedClass, actual.getClass());
    return expectedClass;
  }

  /**
   * Specifies how two instances of a class should be compared by reflection.
   *
   * By default, matches all instance fields and all non-void 0-arg instance methods, but that behavior can be adjusted
   * with methods like {@link #includeFieldsMatching(Pattern)} or {@link #excludeAllMethods()}).
   */
  public static class EqualsByReflectionSpec {
    public static final Pattern NOTHING = Pattern.compile("a^");  // see http://stackoverflow.com/a/940840/1965404
    public static final Pattern EVERYTHING = Pattern.compile(".*");

    private Pattern fieldsToInclude = EVERYTHING;
    private Pattern fieldsToExclude = NOTHING;
    private Pattern methodsToInclude = EVERYTHING;
    private Pattern methodsToExclude = NOTHING;

    private boolean matches(Field field) {
      return memberNotStaticAndNameMatches(field, fieldsToInclude, fieldsToExclude);
    }

    private boolean matches(Method method) {
      return method.getParameterTypes().length == 0
          && method.getReturnType() != Void.TYPE
          && memberNotStaticAndNameMatches(method, methodsToInclude, methodsToExclude);
    }

    private static boolean memberNotStaticAndNameMatches(Member member, Pattern namesToInclude, Pattern namesToExclude) {
      return !Modifier.isStatic(member.getModifiers())
          && namesToInclude.matcher(member.getName()).matches()
          && !namesToExclude.matcher(member.getName()).matches();
    }

    public EqualsByReflectionSpec includeFieldsMatching(Pattern pattern) {
      assertNotNull(pattern);
      fieldsToInclude = pattern;
      return this; // for method chaining
    }

    public EqualsByReflectionSpec excludeFieldsMatching(Pattern pattern) {
      assertNotNull(pattern);
      fieldsToExclude = pattern;
      return this; // for method chaining
    }

    public EqualsByReflectionSpec includeMethodsMatching(Pattern pattern) {
      assertNotNull(pattern);
      methodsToInclude = pattern;
      return this; // for method chaining
    }

    public EqualsByReflectionSpec excludeMethodsMatching(Pattern pattern) {
      assertNotNull(pattern);
      methodsToExclude = pattern;
      return this; // for method chaining
    }

    public EqualsByReflectionSpec excludeAllMethods() {
      return includeMethodsMatching(NOTHING);
    }

    public EqualsByReflectionSpec excludeAllFields() {
      return includeFieldsMatching(NOTHING);
    }
  }

  /**
   * Tests the given function against the expected results.
   * <p>
   * In other words, asserts that
   * <pre>
   *   r.equals(fcn(a)) // &forall;(a, r) &isin; expectedResults
   * </pre>
   * @param fcnName used for printing error messages
   * @param fcn the function being tested
   * @param expectedResults a mapping of function args to their expected results
   * @param <A> the type of the input to the function
   * @param <R> the type of the result of the function
   */
  public static <A, R> void assertFunctionResults(String fcnName, Function<A, R> fcn, Map<A, R> expectedResults) {
    for (Map.Entry<A, R> entry : expectedResults.entrySet()) {
      A arg = entry.getKey();
      R result = fcn.apply(arg);
      R expectedResult = entry.getValue();
      assertEquals("Unexpected result from function call " + StringUtils.methodCallToString(fcnName, arg),
          expectedResult, result);
    }
  }

  /**
   * Equivalent to
   * <pre>
   * if (o instance of T)
   *   return (T)o;
   * else
   *   throw new AssertionFailedError();
   * </pre>
   *
   * @return the given object cast to the expected type
   */
  public static <T> T assertInstanceOf(@Nonnull Class<T> expectedType, @Nonnull Object o) {
    return assertInstanceOf(null, expectedType, o);
  }

  /**
   * Equivalent to
   * <pre>
   * if (o instance of T)
   *   return (T)o;
   * else
   *   throw new AssertionFailedError(message);
   * </pre>
   *
   * @return the given object cast to the expected type
   */
  public static <T> T assertInstanceOf(String message, @Nonnull Class<T> expectedType, @Nonnull Object o) {
    assertTrue(formatComparisonFailedMessage(message, expectedType, o.getClass()),
        expectedType.isAssignableFrom(o.getClass()));
    return expectedType.cast(o);

  }

}