package solutions.trsoftware.commons.client.util.geometry;

import solutions.trsoftware.commons.shared.BaseTestCase;

import static solutions.trsoftware.commons.client.util.geometry.Offset.*;

/**
 * @author Alex
 * @since 12/25/2024
 */
public class OffsetTest extends BaseTestCase {

  public void testParse() {
    assertEquals(px(5), parse("5px"));
    assertEquals(pct(5), parse("5%"));
    assertEquals(px(-55), parse("-55px"));
    assertEquals(pct(-55), parse("-55%"));
    assertEquals(ZERO, parse("0"));
  }
}