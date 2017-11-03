package solutions.trsoftware.commons.client.util.geometry;
/**
 *
 * Date: Nov 3, 2008
 * Time: 6:27:25 PM
 * @author Alex
 */

import junit.framework.TestCase;

public class RectangleTest extends TestCase {

  public void testIntersection() throws Exception {
    {
      Rectangle intersection = new Rectangle(-1, -1, 2, 2).intersection(new Rectangle(0, 0, 2, 2));
      assertEquals(0, intersection.x);
      assertEquals(0, intersection.y);
      assertEquals(1, intersection.width);
      assertEquals(1, intersection.height);
    }
    {
      Rectangle intersection = new Rectangle(0, 0, 640, 480).intersection(new Rectangle(500, 240, 160, 100));
      assertEquals(500, intersection.x);
      assertEquals(240, intersection.y);
      assertEquals(140, intersection.width);
      assertEquals(100, intersection.height);
    }
    {
      // these do not intersect
      Rectangle intersection = new Rectangle(0, 0, 640, 480).intersection(new Rectangle(641, 481, 100, 100));
      assertEquals(0, intersection.area());
    }
  }


  public void testArea() throws Exception {
    assertEquals(4, new Rectangle(-1, -1, 2, 2).area());
    assertEquals(307200, new Rectangle(0, 0, 640, 480).area());
  }
}