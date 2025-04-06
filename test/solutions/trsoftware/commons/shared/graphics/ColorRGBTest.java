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
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import junit.framework.TestCase;

import java.util.Map;

import static solutions.trsoftware.commons.shared.graphics.ColorRGB.*;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertEqualsAndHashCode;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertNotEqualsAndHashCode;
import static solutions.trsoftware.commons.shared.util.RandomUtils.nextIntInRange;
import static solutions.trsoftware.commons.shared.util.RandomUtils.randInt;

/**
 * @author Alex
 * @since 12/24/2017
 */
public class ColorRGBTest extends TestCase {

  // test data obtained from http://hslpicker.com/
  static final BiMap<ColorRGB, ColorHSL> EQUIVALENT_COLORS = ImmutableBiMap.of(
      new ColorRGB(255, 228, 196), new ColorHSL(32.54237288135594, 1, 0.884313725490196)
  );

  private static final Map<String, ColorRGB> STRING_VALUES = ImmutableMap.of(
      "#12afb1", new ColorRGB(0x12, 0xaf, 0xb1),  // RGB
      "#12afb1ca", new ColorRGB(0x12, 0xaf, 0xb1, 0xca));  // RGBA

  public void testToHSL() throws Exception {
    for (ColorRGB rgb : EQUIVALENT_COLORS.keySet()) {
      assertEquals(EQUIVALENT_COLORS.get(rgb), rgb.toHSL());
    }
  }

  public void testToString() throws Exception {
    for (Map.Entry<String, ColorRGB> entry : STRING_VALUES.entrySet()) {
      assertEquals(entry.getKey(), entry.getValue().toString());
    }
  }

  public void testValueOf() throws Exception {
    // test the RGB shorthand form (3 chars: each hex digit should be doubled up when parsing)
    assertEquals(new ColorRGB(0x00, 0x99, 0xcc), valueOf("#09C"));
    // test the RGBA shorthand form (4 chars: each hex digit should be doubled up when parsing)
    assertEquals(new ColorRGB(0x00, 0x99, 0xcc, 0xdd), valueOf("#09CD"));
    // now test the RGB / RGBA longhand forms (6/8 chars)
    for (Map.Entry<String, ColorRGB> entry : STRING_VALUES.entrySet()) {
      assertEquals(entry.getValue(), valueOf(entry.getKey()));
      // should also work without the "#" prefix
      assertEquals(entry.getValue(), valueOf(entry.getKey().substring(1)));
    }
  }

  public void testEqualsAndHashCode() throws Exception {
    // NOTE: we could loop through all possible RGBA values here, but that's extremely slow, so we test only a random sample
    for (int i = 0; i < 100; i++) {
      int r = randInt(256);
      int g = randInt(256);
      int b = randInt(256);
      ColorRGB color = new ColorRGB(r, g, b);
      ColorRGB sameColor = new ColorRGB(r, g, b);
      assertEqualsAndHashCode(color, sameColor);
      if (r > 0)
        assertNotEqualsAndHashCode(color, new ColorRGB(r - 1, g, b));
      else if (g > 1)
        assertNotEqualsAndHashCode(color, new ColorRGB(r, g - 1, b));
      // test the same color with alpha:
      // a) a color without explicit alpha should be equal to the same color with alpha == 255
      assertEqualsAndHashCode(color, new ColorRGB(r, g, b, 255));
      // b) otherwise should not be considered equal
      int a = nextIntInRange(0, 128);  // max value here is 254 (to make sure it's != 255)
      ColorRGB colorWithAlpha = new ColorRGB(r, g, b, a);
      assertNotEqualsAndHashCode(color, colorWithAlpha);
      int a2 = nextIntInRange(128, 255);
      assertNotEqualsAndHashCode(colorWithAlpha, new ColorRGB(r, g, b, a2));
    }
  }

  public void testConstructor() throws Exception {
    // verify that constructors that take components restrict the args to the range [0,255]
    assertEquals(new ColorRGB(0, 123, 0xFF), new ColorRGB(-1, 123, 0xFF +10));
    assertEquals(new ColorRGB(0, 123, 0xFF, 0), new ColorRGB(-1, 123, 0xFF +10, Integer.MIN_VALUE));
    // test the constructors that take an sRGB int
    assertEquals(new ColorRGB(0xaabbccdd, true), new ColorRGB(0xbb, 0xcc, 0xdd, 0xaa));
    assertEquals(new ColorRGB(0xffbbccdd, false), new ColorRGB(0xbb, 0xcc, 0xdd));
    assertEquals(new ColorRGB(0xffbbccdd), new ColorRGB(0xbb, 0xcc, 0xdd));
  }

  /**
   * Tests that {@link ColorRGB#getRGB()} returns the same internal representation as {@link java.awt.Color#getRGB()}
   */
  public void testGetRGB() throws Exception {
    assertEquals(0xaabbccdd, new ColorRGB(0xbb, 0xcc, 0xdd, 0xaa).getRGB());
    assertEquals(0xaabbccdd, new ColorRGB(0xaabbccdd, true).getRGB());

    assertEquals(0xffbbccdd, new ColorRGB(0xbb, 0xcc, 0xdd).getRGB());
    assertEquals(0xffbbccdd, new ColorRGB(0xaabbccdd, false).getRGB());
    assertEquals(0xffbbccdd, new ColorRGB(0xaabbccdd).getRGB());
  }

  public void testBlendAlpha() {
    // any color blended with itself should result in the same color
    assertEquals(RED, blendAlpha(RED, RED));
    assertEquals(WHITE, blendAlpha(WHITE, WHITE));
    // TODO: loop through all possible RGB values to check the above condition
    // manual example
    System.out.println("blendAlpha(#fff266, null) = " + blendAlpha(valueOf("#fff266"), null));
    System.out.println("blendAlpha(#fff2664d, null) = " + blendAlpha(valueOf("#fff2664d"), null));
    // no effect if foreground color is fully opaque (no explicit alpha)
    System.out.println("blendAlpha(BLUE, YELLOW) = " + blendAlpha(BLUE, YELLOW));
  }

  public void testToCssString() {
    // color with an opacity value (alpha < 255)
    assertEquals("rgba(255, 242, 102, 0.75)", valueOf("#fff266bf").toCssString());
    // color without opacity (alpha == 255)
    assertEquals("rgb(255, 0, 0)", RED.toCssString());
  }
}