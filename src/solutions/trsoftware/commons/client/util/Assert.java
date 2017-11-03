package solutions.trsoftware.commons.client.util;

/**
 * Provides some JUnit-like assertions to the production code without requiring the JUnit lib to be available in production.
 *
 * Dec 30, 2009
 *
 * @author Alex
 */
public class Assert {

  public static final String DEFAULT_ERROR_MSG = "Assertion failed";

  public static void assertTrue(boolean condition, String msg) {
    if (!condition)
      throw new AssertionError(msg);
  }

  public static void assertTrue(boolean condition) {
    assertTrue(condition, DEFAULT_ERROR_MSG);
  }


  public static void assertFalse(boolean condition, String msg) {
    assertTrue(!condition);
  }

  public static void assertFalse(boolean condition) {
    assertFalse(condition, DEFAULT_ERROR_MSG);
  }

  /**
   * @return The given arg if it's not null
   * @throws NullPointerException with no message if the arg is null
   */
  public static <T> T assertNotNull(T arg) {
    return assertNotNull(arg, null);
  }

  /**
   * @return The given arg if it's not null
   * @throws NullPointerException with the given message if the arg is null
   */
  public static <T> T assertNotNull(T arg, String msg) {
    if (arg == null) {
      if (msg != null)
        throw new NullPointerException(msg);
      else
        throw new NullPointerException();
    }
    return arg;
  }

  public static void fail(String msg) {
    assertTrue(false, msg);
  }

  public static void assertEquals(Object expected, Object actual) {
    if (!LogicUtils.eq(expected, actual))
      fail(formatNotEqualsMsg(DEFAULT_ERROR_MSG, expected, actual));
  }

  private static String formatNotEqualsMsg(String message, Object expected, Object actual) {
    StringBuilder str = new StringBuilder();
    if (message != null)
      str.append(message);
    str.append(": ").append("expected:<").append(expected).append("> but was:<").append(actual).append(">");
    return str.toString();
  }



}