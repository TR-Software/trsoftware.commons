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

package solutions.trsoftware.tools.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.annotations.ExcludeFromSuite;
import solutions.trsoftware.commons.shared.util.MapUtils;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;

/**
 * @author Alex
 * @since 2/25/2018
 */
public class ResolvedPropertiesTest extends TestCase {

  public void testGetProperty() throws Exception {
    // 1) simple cases, with no resolution issues
    {
      ResolvedProperties props = create(
          "p1", "123",
          "p2", "${p1}456",
          "p3", "${p2}-${p1}**${p2}");
      assertEquals("123", props.getProperty("p1"));
      assertEquals("123456", props.getProperty("p2"));
      assertEquals("123456-123**123456", props.getProperty("p3"));
    }
    // 2) case with cyclic references
    {
      // a) immediate self-reference
      ResolvedProperties props = create(
          "p1", "123",
          "p2", "${p1}456",
          "p3", "${p2}-${p3}");
      assertEquals("123", props.getProperty("p1"));
      assertEquals("123456", props.getProperty("p2"));
      assertThrows(new IllegalStateException("Recursive variable reference (${p3}) in property 'p3'"),
          (Runnable)() -> props.getProperty("p3"));
      // b) a cycle once-removed
      addProperties(props,
          "p4", "abc${p5}",
          "p5", "${p4}def");
      assertThrows(new IllegalStateException("Recursive variable reference (${p4}) in property 'p5'"),
          (Runnable)() -> props.getProperty("p4"));
    }
    // 3) forward-references and missing variables
    {
      // TODO: cont here: what if a variable's value is missing? Will it substitute "null" or ""?
    }
    // 4) variable brackets not closed, nested variable tags
    // TODO:
  }

  @ExcludeFromSuite
  public void testResolveAll() throws Exception {
    fail("TODO"); // TODO
  }

  private static ResolvedProperties create(String... keyValuePairs) {
    ResolvedProperties props = new ResolvedProperties();
    addProperties(props, keyValuePairs);
    return props;

  }

  private static void addProperties(ResolvedProperties props, String... kvPairs) {
    MapUtils.putAll(props, (Object[])kvPairs);
  }
}