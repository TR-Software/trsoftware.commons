package solutions.trsoftware.commons.shared.util.geometry;

import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.util.StringUtils;

import static com.google.common.base.Strings.lenientFormat;
import static solutions.trsoftware.commons.shared.util.geometry.Point.point;
import static solutions.trsoftware.commons.shared.util.geometry.Rectangle2D.*;

/**
 * @author Alex
 * @since 2/2/2025
 */
public class Rectangle2DTest extends BaseTestCase {

  public void testIntersection() throws Exception {
    assertEquals(rect(0, 0, 1, 1), intersection(
        rect(-1, -1, 2, 2),
        rect(0, 0, 2, 2)));
    assertEquals(rect(500, 240, 140, 100), intersection(
        rect(0, 0, 640, 480),
        rect(500, 240, 160, 100)));
    {
      // these do not intersect:
      Rectangle2D empty = intersection(
          rect(0, 0, 640, 480),
          rect(641, 481, 100, 100));
      assertAreaEquals(0.0, empty);
      assertTrue(empty.isEmpty());
    }
  }

  public void testUnion() throws Exception {
    assertEquals(rect(-1, -1, 3, 3), union(
        rect(-1, -1, 2, 2),
        rect(0, 0, 2, 2)));
    assertEquals(rect(0, 0, 660, 480), union(
        rect(0, 0, 640, 480),
        rect(500, 240, 160, 100)));
    /* TODO(3/31/2025): test the edge cases described in java.awt.Rectangle.union:
        - either rect (or both) having any dimension less than zero
     */
    System.out.println("TODO: Non-existant rects:");
    union(
        rect(0, 0, -640, 480),
        rect(500, 240, 160, 100));
    union(
        rect(0, 0, 640, 480),
        rect(500, 240, 160, -100));
    union(
        rect(0, 0, 640, -480),
        rect(500, 240, -160, 100));
    System.out.println();
    union(
        rect(0, 0, -2, 5),
        rect(1, 1, -3, 6));
    union(
        rect(0, 0, -2, -5),
        rect(1, 1, -3, 6));
  }

  private Rectangle2D union(Rectangle2D r1, Rectangle2D r2) {
    Rectangle2D ret = Rectangle2D.union(r1, r2);
    System.out.println(StringUtils.methodCallToStringWithResult("union", ret, r1, r2));
    System.out.println(lenientFormat("union(%s, %s) = %s", toPointString(r1), toPointString(r2), toPointString(ret)));
    return ret;
  }

  private String toPointString(Rectangle2D r) {
    return lenientFormat("%s(%s, %s)", Rectangle2D.class.getSimpleName(), r.getTopLeft(), r.getBottomRight());
  }

  public void testArea() throws Exception {
    assertAreaEquals(0d, rect(0, 0, 0, 0));
    for (double width = 0; width < 10; width+=.25) {
      for (double height = 0; height < 10; height+=.25) {
        assertAreaEquals(width * height, rect(0, 0, width, height));
      }
    }
  }

  private void assertAreaEquals(double expected, Rectangle2D rect) {
    assertEquals(expected, rect.area());
  }

  public void testGetTopLeft() throws Exception {
    assertEquals(point(0, 0),
        rect(0, 0, 10, 15).getTopLeft());
  }

  public void testGetTopRight() throws Exception {
    assertEquals(point(10, 0),
        rect(0, 0, 10, 15).getTopRight());
  }

  public void testGetBottomLeft() throws Exception {
    assertEquals(point(0, 15),
        rect(0, 0, 10, 15).getBottomLeft());
  }

  public void testGetBottomRight() throws Exception {
    assertEquals(point(10, 15),
        rect(0, 0, 10, 15).getBottomRight());
  }

  public void testGetCenter() throws Exception {
    assertEquals(point(5, 7.5),
        rect(0, 0, 10, 15).getCenter());
  }

  public void testIsEmpty() throws Exception {
    assertFalse(rect(0, 0, 2, 2).isEmpty());
    assertTrue(rect(0, 0, 0, 0).isEmpty());
    assertTrue(rect(0, 0, 2, -1).isEmpty());
    assertTrue(rect(0, 0, -2, 2).isEmpty());
  }

  public void testFromDiagonal() throws Exception {
    assertEquals(fromDiagonal(0, 0, 1, 1), rect(0, 0, 1, 1));
    assertEquals(fromDiagonal(1, 1, 0, 0), rect(0, 0, 1, 1));
  }
}