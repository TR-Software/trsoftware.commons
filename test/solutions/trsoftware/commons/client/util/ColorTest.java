package solutions.trsoftware.commons.client.util;

import junit.framework.TestCase;

public class ColorTest extends TestCase {

  public void testColor() throws Exception {
    Color c = new Color(115, 255, 45);
    System.out.println(c.toString());
    assertEquals("#73ff2d", c.toString());
    Color parsedColor = Color.valueOf("#73ff2d");
    assertEquals(115, parsedColor.r);
    assertEquals(255, parsedColor.g);
    assertEquals(45, parsedColor.b);

    // equals & hash code methods should be properly implemented
    assertEquals(c.hashCode(), parsedColor.hashCode());
    assertEquals(c, parsedColor);
    assertNotSame(c, parsedColor);

    Color parsedColor2 = Color.valueOf("73ff2d");  // without the # prefix
    assertEquals(115, parsedColor2.r);
    assertEquals(255, parsedColor2.g);
    assertEquals(45, parsedColor2.b);

    assertEquals(parsedColor2.hashCode(), parsedColor.hashCode());
    assertEquals(parsedColor2, parsedColor);
    assertNotSame(parsedColor2, parsedColor);

    // make sure that all components will be automatically coerced to the range 0..255
    assertEquals("#0000ff", new Color(-25, 0, 256).toString());
  }

}