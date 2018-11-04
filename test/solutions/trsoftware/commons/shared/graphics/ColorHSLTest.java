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

package solutions.trsoftware.commons.shared.graphics;

import com.google.common.collect.BiMap;
import junit.framework.TestCase;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertEqualsAndHashCode;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertNotEqualsAndHashCode;

/**
 * @author Alex
 * @since 12/22/2017
 */
public class ColorHSLTest extends TestCase {

  public void testToString() throws Exception {
    assertEquals("hsl(60, 100%, 50%)", new ColorHSL(60, 1, .5).toString());
    assertEquals("hsl(60.12, 112.34%, 50.13%)", new ColorHSL(60.1234, 1.1234123, .50126).toString());
  }

  public void testParser() throws Exception {
    ColorHSL.Parser parser = ColorHSL.getParser();
    assertEquals(new ColorHSL(60, 1, .5), parser.parse("hsl(60, 100%, 50%)"));
    assertEquals(new ColorHSL(60.12, 1.1234, .5013), parser.parse("hsl(60.12, 112.34%, 50.13%)"));
  }

  /**
   * Should produce the same results as {@link #testParser()}
   */
  public void testValueOf() throws Exception {
    assertEquals(new ColorHSL(60, 1, .5), ColorHSL.valueOf("hsl(60, 100%, 50%)"));
    assertEquals(new ColorHSL(60.12, 1.1234, .5013), ColorHSL.valueOf("hsl(60.12, 112.34%, 50.13%)"));
  }

  public void testToRGB() throws Exception {
    BiMap<ColorHSL, ColorRGB> expectedResults = ColorRGBTest.EQUIVALENT_COLORS.inverse();
    for (ColorHSL hsl : expectedResults.keySet()) {
      assertEquals(expectedResults.get(hsl), hsl.toRGB());
    }
  }

  public void testEqualsAndHashCode() throws Exception {
    assertEqualsAndHashCode(new ColorHSL(60.1234, 1.1234123, .50126), new ColorHSL(60.12, 1.1234, .5013));
    assertNotEqualsAndHashCode(new ColorHSL(60.1234, 1.1234123, .50126), new ColorHSL(60.13, 1.1234, .5013));
  }

}