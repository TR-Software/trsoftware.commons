package solutions.trsoftware.commons.shared.util;

import com.google.common.collect.BiMap;
import junit.framework.TestCase;

import static solutions.trsoftware.commons.client.testutil.AssertUtils.assertEqualsAndHashCode;
import static solutions.trsoftware.commons.client.testutil.AssertUtils.assertNotEqualsAndHashCode;

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