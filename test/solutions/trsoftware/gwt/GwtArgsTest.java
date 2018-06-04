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

package solutions.trsoftware.gwt;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.BaseGwtTestCase;
import solutions.trsoftware.commons.shared.gwt.GwtVersion;
import solutions.trsoftware.commons.shared.util.MapUtils;

/**
 * Tests {@link BaseGwtTestCase}
 */
public class GwtArgsTest extends TestCase {

  /**
   * Tests the constructor of {@link GwtArgs} (i.e. parsing the {@value GwtArgs#SYS_PROP_GWT_ARGS} system property)
   */
  public void testGwtArgsParsing() throws Exception {
    assertEquals(
        MapUtils.<String,String>linkedHashMap(
            "foo", null,
            "bar", "x",
            "baz", null,
            "foobar", null),
        new GwtArgs("-foo -bar x -baz -foobar"));
    assertEquals(
        MapUtils.<String,String>linkedHashMap(
            "foo", null,
            "bar", "x",
            "baz", null),
        new GwtArgs("    -foo    -bar    x   -baz "));
    assertEquals(
        MapUtils.<String,String>linkedHashMap(
            "foo", "1234",
            "bar", "x",
            "baz", null),
        new GwtArgs("-foo 1234 -bar x -baz"));
    assertEquals(
        MapUtils.<String,String>linkedHashMap(
            "foo", "1234",
            "b-ar", "x",
            "baz", "foo-bar"),
        new GwtArgs("-foo 1234 -b-ar x -baz foo-bar"));
  }

  /** Tests {@link GwtArgs#toString()} */
  public void testToString() throws Exception {
    assertEquals("-foo -bar x -baz -foobar",
        new GwtArgs("-foo -bar x -baz -foobar").toString());
    assertEquals("-foo -bar x -baz",
        new GwtArgs("    -foo    -bar    x   -baz ").toString());
    // test proper quoting of values with spaces and other non-word chars
    assertEquals("-foo 1234 -bar \"x foobar\" -baz",
        new GwtArgs("-foo 1234 -bar \"x foobar\" -baz").toString());
    assertEquals("-foo 1234 -b-ar x -baz \"foo-bar\"",
        new GwtArgs("-foo 1234 -b-ar x -baz foo-bar").toString());
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

  /** Tests {@link GwtArgs#putIfValueNotStartsWith(String, String, String)} */
  public void testPutIfValueNotStartsWith() throws Exception {
    // test a few cases where putIfValueNotStartsWith should modify the args
    {
      GwtArgs gwtArgs = new GwtArgs("-foo -bar x -baz -foobar");
      gwtArgs.putIfValueNotStartsWith("newKey", "foo", "bar");
      assertEquals("-foo -bar x -baz -foobar -newKey bar", gwtArgs.toString());
    }
    {
      GwtArgs gwtArgs = new GwtArgs("-foo 1234 -bar \"x foobar\" -baz");
      gwtArgs.putIfValueNotStartsWith("bar", "foo", "newValue");
      assertEquals("-foo 1234 -bar newValue -baz", gwtArgs.toString());
    }
    // now test a few cases where putIfValueNotStartsWith should not modify
    {
      GwtArgs gwtArgs = new GwtArgs("-foo 1234 -bar \"x foobar\" -baz");
      gwtArgs.putIfValueNotStartsWith("bar", "x", "newValue");
      assertEquals("-foo 1234 -bar \"x foobar\" -baz", gwtArgs.toString());
    }
    {
      GwtArgs gwtArgs = new GwtArgs("-foo 1234 -bar \"x foobar\" -baz");
      gwtArgs.putIfValueNotStartsWith("bar", "x foobar", "newValue");
      assertEquals("-foo 1234 -bar \"x foobar\" -baz", gwtArgs.toString());
    }
    {
      GwtArgs gwtArgs = new GwtArgs("-foo 1234 -bar \"x foobar\" -baz");
      gwtArgs.putIfValueNotStartsWith("bar", "", "newValue");
      assertEquals("-foo 1234 -bar \"x foobar\" -baz", gwtArgs.toString());
    }
  }

}
