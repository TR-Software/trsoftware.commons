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

package solutions.trsoftware.gwt;

import com.google.gwt.dev.GwtVersion;
import junit.framework.TestCase;
import solutions.trsoftware.commons.client.BaseGwtTestCase;
import solutions.trsoftware.commons.client.testutil.RunStyleValue;
import solutions.trsoftware.commons.shared.util.SetUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.util.*;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.*;
import static solutions.trsoftware.gwt.GwtArgs.*;

/**
 * Tests {@link BaseGwtTestCase}
 */
public class GwtArgsTest extends TestCase {

  /**
   * The last set of args passed to {@link #verifyArgs(GwtArgs, String...)}
   */
  private String[] expectedArgs;

  @Override
  protected void tearDown() throws Exception {
    expectedArgs = null;
    super.tearDown();
  }

  /**
   * Tests the constructor of {@link GwtArgs} (i.e. parsing the {@value GwtArgs#SYS_PROP_GWT_ARGS} system property)
   */
  public void testConstructor() throws Exception {
    verifyArgs(parseArgs("-foo -bar x -baz -foobar"),
        "foo", null,
        "bar", "x",
        "baz", null,
        "foobar", null);
    verifyArgs(parseArgs("    -foo    -bar    x   -baz "),
        "foo", null,
        "bar", "x",
        "baz", null);
    verifyArgs(parseArgs("-foo 1234 -bar x -baz"),
        "foo", "1234",
        "bar", "x",
        "baz", null);
    verifyArgs(parseArgs("-foo 1234 -b-ar x -baz foo-bar"),
        "foo", "1234",
        "b-ar", "x",
        "baz", "foo-bar");
    // repeated args should override their previous value
    verifyArgs(parseArgs("-foo 1234 -bar x -baz -bar y"),
        "foo", "1234",
        "bar", "y",
        "baz", null);
    // unless they can be repeated multiple times (e.g. -setProperty)
    verifyArgs(parseArgs("-foo 1234 -bar x -baz -setProperty a=1 -setProperty b=2,3"),
        "foo", "1234",
        "bar", "x",
        "baz", null,
        "setProperty", "a=1",
        "setProperty", "b=2,3");
    // except if multiple -setProperty args defining the same property (should keep only the last one)
    verifyArgs(parseArgs("-foo 1234 -bar x -baz -setProperty a=1 -setProperty b=2,3 -setProperty a=foo,bar"),
        "foo", "1234",
        "bar", "x",
        "baz", null,
        "setProperty", "a=foo,bar",
        "setProperty", "b=2,3"
    );
  }

  private GwtArgs verifyArgs(GwtArgs gwtArgs, String... keyValuePairs) {
    expectedArgs = keyValuePairs;
    assertEquals(entryList(keyValuePairs), new ArrayList<>(gwtArgs.getEntries()));
    return gwtArgs;
  }

  private GwtArgs verifyArgsUnchanged(GwtArgs gwtArgs) {
    return verifyArgs(gwtArgs, expectedArgs);
  }

  /** Tests {@link GwtArgs#toString()} */
  public void testToString() throws Exception {
    assertEquals("-foo -bar x -baz -foobar",
        parseArgs("-foo -bar x -baz -foobar").toString());
    assertEquals("-foo -bar x -baz",
        parseArgs("    -foo    -bar    x   -baz ").toString());
    // test proper quoting of values with spaces and other non-word chars
    assertEquals("-foo 1234 -bar \"x foobar\" -baz",
        parseArgs("-foo 1234 -bar \"x foobar\" -baz").toString());
    assertEquals("-foo 1234 -b-ar x -baz \"foo-bar\"",
        parseArgs("-foo 1234 -b-ar x -baz foo-bar").toString());
  }

  /** Tests {@link GwtArgs#isWebMode()} */
  public void testIsWebMode() throws Exception {
    assertFalse(new GwtArgs250("-foo -bar x -baz -foobar").isWebMode());
    assertTrue(new GwtArgs250("-foo -bar x -web -foobar").isWebMode());
    assertTrue(new GwtArgs250("-prod -bar x -baz -foobar").isWebMode());
    assertTrue(new GwtArgs250("-prod -bar x -baz -devMode").isWebMode());

    assertTrue(new GwtArgs280("-foo -bar x -baz -foobar").isWebMode());
    assertTrue(new GwtArgs280("-foo -bar x -web -foobar").isWebMode());
    assertTrue(new GwtArgs280("-prod -bar x -baz -foobar").isWebMode());
    assertFalse(new GwtArgs280("-prod -bar x -baz -devMode").isWebMode());
  }

  public void testGetRunStyle() throws Exception {
    assertNull(parseArgs("-foo -bar x -baz -foobar").getRunStyle());
    {
      RunStyleValue runStyle = parseArgs("-foo -runStyle HtmlUnit:Chrome,FF38,IE8,IE11 -foobar").getRunStyle();
      assertNotNull(runStyle);
      assertEquals("HtmlUnit", runStyle.getName());
      assertEquals(SetUtils.newSet("Chrome", "FF38", "IE8", "IE11"), runStyle.getArgs());
      assertEquals(SetUtils.newSet("Chrome"),
          parseArgs("-runStyle HtmlUnit:Chrome").getRunStyle().getArgs());
    }
    {
      RunStyleValue runStyle = parseArgs("-foo -runStyle Manual -foobar").getRunStyle();
      assertNotNull(runStyle);
      assertEquals("Manual", runStyle.getName());
      assertEquals(Collections.emptySet(), runStyle.getArgs());
    }
  }

  public void testGetRunStyleHtmlUnitArgs() throws Exception {
    assertEquals(Collections.emptySet(), parseArgs("-foo -bar x -baz -foobar").getRunStyleHtmlUnitArgs());
    assertEquals(Collections.emptySet(), parseArgs("-foo -runStyle HtmlUnit -baz -foobar").getRunStyleHtmlUnitArgs());

    assertEquals(SetUtils.newSet("Chrome", "FF38", "IE8", "IE11"),
        parseArgs("-foo -runStyle HtmlUnit:Chrome,FF38,IE8,IE11 -foobar").getRunStyleHtmlUnitArgs());
  }

  public void testModification() throws Exception {
    GwtArgs args = parseArgs("-foo 1234 -bar");
    verifyArgs(args,
        "foo", "1234",
        "bar", null
    );
    // existing args
    assertTrue(args.containsKey("foo"));
    assertEquals("1234", args.get("foo"));
    assertEquals(Collections.singletonList("1234"), args.getAll("foo"));
    assertTrue(args.containsKey("bar"));
    assertNull(args.get("bar"));
    assertEquals(Collections.singletonList(null), args.getAll("bar"));

    // missing args
    assertFalse(args.containsKey("baz"));
    assertNull(args.get("baz"));
    assertEquals(Collections.emptyList(), args.getAll("baz"));

    // adding an arg with no value
    args.put("baz", null);
    System.out.println("After args.put(\"baz\", null): " + args);
    verifyArgs(args,
        "foo", "1234",
        "bar", null,
        "baz", null
    );
    assertTrue(args.containsKey("baz"));
    assertNull(args.get("baz"));
    assertEquals(Collections.singletonList(null), args.getAll("baz"));

    // replacing an arg value
    args.put("foo", "asdf");  // since this arg is not in GwtArgs.multiArgs, should replace the existing value
    System.out.println("After args.put(\"foo\", \"asdf\"): " + args);
    verifyArgs(args,
        "foo", "asdf",
        "bar", null,
        "baz", null
    );

    // now try an arg that can be repeated
    assertFalse(args.containsKey("includeJsInteropExports"));
    args.put("includeJsInteropExports", "asdf");
    System.out.println("After args.put(\"includeJsInteropExports\", \"asdf\"): " + args);
    verifyArgs(args,
        "foo", "asdf",
        "bar", null,
        "baz", null,
        "includeJsInteropExports", "asdf"
    );
    args.put("includeJsInteropExports", "qwer");
    System.out.println("After args.put(\"includeJsInteropExports\", \"qwer\"): " + args);
    verifyArgs(args,
        "foo", "asdf",
        "bar", null,
        "baz", null,
        "includeJsInteropExports", "asdf",
        "includeJsInteropExports", "qwer"
    );
    args.put("includeJsInteropExports", "zxcv");
    System.out.println("After args.put(\"includeJsInteropExports\", \"zxcv\"): " + args);
    verifyArgs(args,
        "foo", "asdf",
        "bar", null,
        "baz", null,
        "includeJsInteropExports", "asdf",
        "includeJsInteropExports", "qwer",
        "includeJsInteropExports", "zxcv"
    );

    // test removal
    assertFalse(args.remove("foo", "1234"));  // doesn't do anything b/c "foo" has a different value
    verifyArgsUnchanged(args);
    verifyArgs(args,
        "foo", "asdf",
        "bar", null,
        "baz", null,
        "includeJsInteropExports", "asdf",
        "includeJsInteropExports", "qwer",
        "includeJsInteropExports", "zxcv"
    );
    assertTrue(args.remove("foo"));
    System.out.println("After args.remove(\"foo\"): " + args);
    verifyArgs(args,
        "bar", null,
        "baz", null,
        "includeJsInteropExports", "asdf",
        "includeJsInteropExports", "qwer",
        "includeJsInteropExports", "zxcv"
    );
    assertTrue(args.remove("includeJsInteropExports", "zxcv"));
    System.out.println("After args.remove(\"includeJsInteropExports\", \"zxcv\"): " + args);
    verifyArgs(args,
        "bar", null,
        "baz", null,
        "includeJsInteropExports", "asdf",
        "includeJsInteropExports", "qwer"
    );
    assertTrue(args.remove("includeJsInteropExports"));
    System.out.println("After args.remove(\"includeJsInteropExports\"): " + args);
    verifyArgs(args,
        "bar", null,
        "baz", null
    );
  }

  public void testSetProperty() throws Exception {
    {
      System.out.println("Testing an instance without any properties defined:");
      GwtArgs args = parseArgs("-foo 1234 -bar");
      verifyArgs(args,
          "foo", "1234",
          "bar", null
      );
      assertNull(args.get(SET_PROPERTY));
      assertEquals(Collections.emptyList(), args.getAll(SET_PROPERTY));
      assertNull(args.getProperty("foo"));

      assertFalse(args.setProperty("bar", "foo"));;
      System.out.println("After args.setProperty(\"bar\", \"foo\"): " + args);
      verifyArgs(args,
          "foo", "1234",
          "bar", null,
          "setProperty", "bar=foo"
      );
      assertEquals("bar=foo", args.get(SET_PROPERTY));
      assertEquals(Collections.singletonList("bar=foo"), args.getAll(SET_PROPERTY));
    }
    {
      System.out.println("\nTesting an instance with 2 pre-defined properties:");
      GwtArgs args = parseArgs("-foo 1234 -setProperty foo=1 -setProperty x=2,3 -bar");
      verifyArgs(args,
          "foo", "1234",
          "setProperty", "foo=1",
          "setProperty", "x=2,3",
          "bar", null
      );
      // TODO: assert GwtArgs.get throws if multiple values present?
      assertEquals(Arrays.asList("foo=1", "x=2,3"), args.getAll(SET_PROPERTY));
      assertEquals("1", args.getProperty("foo"));
      assertEquals("2,3", args.getProperty("x"));
      assertNull(args.getProperty("bar"));
      // should modify in-place if property already defined
      assertTrue(args.setProperty("foo", "asdf,qwer"));
      System.out.println("After args.setProperty(\"foo\", \"asdf,qwer\"): " + args);
      verifyArgs(args,
          "foo", "1234",
          "setProperty", "foo=asdf,qwer",
          "setProperty", "x=2,3",
          "bar", null
      );
      // should append at the end if new property
      assertFalse(args.setProperty("bar", "foo"));
      System.out.println("After args.setProperty(\"bar\", \"foo\"): " + args);
      verifyArgs(args,
          "foo", "1234",
          "setProperty", "foo=asdf,qwer",
          "setProperty", "x=2,3",
          "setProperty", "bar=foo",
          "bar", null
      );
      // test modifying properties via put(SET_PROPERTY, String): should have the same effect as setProperty(String, String)
      assertThrows(IllegalArgumentException.class, (Runnable)() -> args.put(SET_PROPERTY, "foo"));  // must have name=value format
      args.put(SET_PROPERTY, "foo=1");
      System.out.println("After args.put(SET_PROPERTY, \"foo=1\"): " + args);
      verifyArgs(args,
          "foo", "1234",
          "setProperty", "foo=1",
          "setProperty", "x=2,3",
          "setProperty", "bar=foo",
          "bar", null
      );
      // test removal
      assertFalse(args.removeProperty("qwer"));  // property didn't exist
      verifyArgsUnchanged(args);
      assertTrue(args.removeProperty("x"));  // removed an existing property setting
      System.out.println("After args.removeProperty(\"x\"): " + args);
      verifyArgs(args,
          "foo", "1234",
          "setProperty", "foo=1",
          "setProperty", "bar=foo",
          "bar", null
      );
    }
  }

  /**
   * Calls {@link GwtArgs#GwtArgs(String)} with the given argument and prints its {@link GwtArgs#toString()} value
   * @return the new instance
   */
  private GwtArgs parseArgs(String argsString) {
    GwtArgs gwtArgs = new GwtArgs(argsString);
    System.out.println(StringUtils.methodCallToString("new GwtArgs", argsString)
        + ".toString(): " + gwtArgs.toString());
    return gwtArgs;
  }


  // TODO: maybe extract the following util methods?

  private static Set<Map.Entry<String, String>> entrySet(String... keyValuePairs) {
    return addMapEntries(new LinkedHashSet<>(), keyValuePairs);
  }

  private static List<Map.Entry<String, String>> entryList(String... keyValuePairs) {
    return addMapEntries(new ArrayList<>(), keyValuePairs);
  }

  private static <T extends Collection<Map.Entry<String, String>>> T addMapEntries(T collection, String... keyValuePairs) {
    int n = keyValuePairs.length;
    if (n % 2 == 1)
      throw new IllegalArgumentException("Even number of args required.");
    for (int i = 0; i < n; i+=2)
      collection.add(new AbstractMap.SimpleEntry<>(keyValuePairs[i], keyValuePairs[i + 1]));
    return collection;
  }

  /**
   * Overrides {@link #getGwtVersion()} to emulate version 2.5.0
   */
  private class GwtArgs250 extends GwtArgs {

    public GwtArgs250(String argsString) {
      super(argsString);
    }

    @Override
    protected GwtVersion getGwtVersion() {
      return new GwtVersion("2.5.0");
    }
  }
  
  /**
   * Overrides {@link #getGwtVersion()} to emulate version 2.8.0
   */
  private class GwtArgs280 extends GwtArgs {

    public GwtArgs280(String argsString) {
      super(argsString);
    }

    @Override
    protected GwtVersion getGwtVersion() {
      return new GwtVersion("2.8.0");
    }
  }

}
