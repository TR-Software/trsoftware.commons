/*
 * Copyright 2021 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util;

/**
 * Provides some JUnit-like assertions to production code without requiring the JUnit lib to be available in production.
 *
 * @since Dec 30, 2009
 * @author Alex
 * @see com.google.common.base.Preconditions
 */
public abstract class Assert {

  public static final String DEFAULT_ERROR_MSG = "Assertion failed";

  public static void assertTrue(boolean condition, String msg) {
    if (!condition)
      fail(msg);
  }

  public static void assertTrue(boolean condition) {
    assertTrue(condition, DEFAULT_ERROR_MSG);
  }


  public static void assertFalse(boolean condition, String msg) {
    assertTrue(!condition, msg);
  }

  public static void assertFalse(boolean condition) {
    assertFalse(condition, DEFAULT_ERROR_MSG);
  }

  /**
   * Same as {@link java.util.Objects#requireNonNull(Object)}.  Provided for compatibility with Java 1.6 and older.
   *
   * @return The given arg if it's not null
   * @throws NullPointerException with no message if the arg is null
   */
  public static <T> T assertNotNull(T arg) {
    return assertNotNull(arg, null);
  }

  /**
   * Same as {@link java.util.Objects#requireNonNull(Object, String)}.  Provided for compatibility with Java 1.6 and older.
   *
   * @return The given arg if it's not null
   * @throws NullPointerException with the given message if the arg is null
   */
  public static <T> T assertNotNull(T arg, String msg) {
    if (arg == null) {
      throw new NullPointerException(msg);
    }
    return arg;
  }

  public static void assertNull(Object obj) {
    assertTrue(obj == null);
  }

  public static void assertNull(Object obj, String msg) {
    assertTrue(obj == null, msg);
  }

  public static void fail(String msg) {
    throw new AssertionError(msg);
  }

  public static void assertEquals(Object expected, Object actual) {
    assertEquals(DEFAULT_ERROR_MSG, expected, actual);
  }

  public static void assertEquals(String message, Object expected, Object actual) {
    if (!LogicUtils.eq(expected, actual))
      fail(formatNotEqualsMsg(message, expected, actual));
  }

  private static String formatNotEqualsMsg(String message, Object expected, Object actual) {
    StringBuilder str = new StringBuilder();
    if (message != null)
      str.append(message);
    str.append(": ").append("expected:<").append(expected).append("> but was:<").append(actual).append(">");
    return str.toString();
  }

  /**
   * @param size a size for an array
   * @throws NegativeArraySizeException iff {@code size < 0}
   */
  public static void checkArraySize(int size) {
    if (size < 0)
      throw new NegativeArraySizeException("Negative array size: " + size);
  }

}