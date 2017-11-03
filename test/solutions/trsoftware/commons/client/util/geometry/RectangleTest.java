/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.util.geometry;

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