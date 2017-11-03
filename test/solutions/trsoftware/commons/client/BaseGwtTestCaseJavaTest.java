package solutions.trsoftware.commons.client;

import solutions.trsoftware.commons.client.util.MapUtils;
import junit.framework.TestCase;

/**
 * Tests {@link BaseGwtTestCase}
 */
public class BaseGwtTestCaseJavaTest extends TestCase {

  /** Tests the constructor of {@link BaseGwtTestCase.GwtArgs} */
  public void testGwtArgsParsing() throws Exception {
    assertEquals(
        MapUtils.<String,String>linkedHashMap(
            "foo", null,
            "bar", "x",
            "baz", null,
            "foobar", null),
        new BaseGwtTestCase.GwtArgs("-foo -bar x -baz -foobar"));
    assertEquals(
        MapUtils.<String,String>linkedHashMap(
            "foo", null,
            "bar", "x",
            "baz", null),
        new BaseGwtTestCase.GwtArgs("    -foo    -bar    x   -baz "));
    assertEquals(
        MapUtils.<String,String>linkedHashMap(
            "foo", "1234",
            "bar", "x",
            "baz", null),
        new BaseGwtTestCase.GwtArgs("-foo 1234 -bar x -baz"));
    assertEquals(
        MapUtils.<String,String>linkedHashMap(
            "foo", "1234",
            "b-ar", "x",
            "baz", "foo-bar"),
        new BaseGwtTestCase.GwtArgs("-foo 1234 -b-ar x -baz foo-bar"));
  }

  /** Tests {@link BaseGwtTestCase.GwtArgs#toString()} */
  public void testGwtArgs_toString() throws Exception {
    assertEquals("-foo -bar x -baz -foobar",
        new BaseGwtTestCase.GwtArgs("-foo -bar x -baz -foobar").toString());
    assertEquals("-foo -bar x -baz",
        new BaseGwtTestCase.GwtArgs("    -foo    -bar    x   -baz ").toString());
    // test proper quoting of values with spaces and other non-word chars
    assertEquals("-foo 1234 -bar \"x foobar\" -baz",
        new BaseGwtTestCase.GwtArgs("-foo 1234 -bar \"x foobar\" -baz").toString());
    assertEquals("-foo 1234 -b-ar x -baz \"foo-bar\"",
        new BaseGwtTestCase.GwtArgs("-foo 1234 -b-ar x -baz foo-bar").toString());

  }
  /** Tests {@link BaseGwtTestCase.GwtArgs#isWebMode()} */
  public void testGwtArgs_isWebMode() throws Exception {
    assertFalse(new BaseGwtTestCase.GwtArgs("-foo -bar x -baz -foobar").isWebMode());
    assertTrue(new BaseGwtTestCase.GwtArgs("-foo -bar x -web -foobar").isWebMode());
    assertTrue(new BaseGwtTestCase.GwtArgs("-prod -bar x -baz -foobar").isWebMode());
  }

  /** Tests {@link BaseGwtTestCase.GwtArgs#putIfValueNotStartsWith(String, String, String)} */
  public void testGwtArgs_putIfValueNotStartsWith() throws Exception {
    // test a few cases where putIfValueNotStartsWith should modify the args
    {
      BaseGwtTestCase.GwtArgs gwtArgs = new BaseGwtTestCase.GwtArgs("-foo -bar x -baz -foobar");
      gwtArgs.putIfValueNotStartsWith("newKey", "foo", "bar");
      assertEquals("-foo -bar x -baz -foobar -newKey bar", gwtArgs.toString());
    }
    {
      BaseGwtTestCase.GwtArgs gwtArgs = new BaseGwtTestCase.GwtArgs("-foo 1234 -bar \"x foobar\" -baz");
      gwtArgs.putIfValueNotStartsWith("bar", "foo", "newValue");
      assertEquals("-foo 1234 -bar newValue -baz", gwtArgs.toString());
    }
    // now test a few cases where putIfValueNotStartsWith should not modify
    {
      BaseGwtTestCase.GwtArgs gwtArgs = new BaseGwtTestCase.GwtArgs("-foo 1234 -bar \"x foobar\" -baz");
      gwtArgs.putIfValueNotStartsWith("bar", "x", "newValue");
      assertEquals("-foo 1234 -bar \"x foobar\" -baz", gwtArgs.toString());
    }
    {
      BaseGwtTestCase.GwtArgs gwtArgs = new BaseGwtTestCase.GwtArgs("-foo 1234 -bar \"x foobar\" -baz");
      gwtArgs.putIfValueNotStartsWith("bar", "x foobar", "newValue");
      assertEquals("-foo 1234 -bar \"x foobar\" -baz", gwtArgs.toString());
    }
    {
      BaseGwtTestCase.GwtArgs gwtArgs = new BaseGwtTestCase.GwtArgs("-foo 1234 -bar \"x foobar\" -baz");
      gwtArgs.putIfValueNotStartsWith("bar", "", "newValue");
      assertEquals("-foo 1234 -bar \"x foobar\" -baz", gwtArgs.toString());
    }
  }

}
