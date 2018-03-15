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

package solutions.trsoftware.commons.shared.util.geometry;

import solutions.trsoftware.commons.shared.util.ArrayUtils;

/**
 * Immutable rectangle with integer position and dimensions.
 *
 * @author Alex
 */
public class Rectangle {
  public final int x, y, width, height;

  public Rectangle(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    // force the dimensions to be nonnegative (for proper operation of intersection)
    if (width < 0 || height < 0) {
      width = 0;
      height = 0;
    }
    this.width = width;
    this.height = height;
  }

  /**
   * Code adopted from {@link java.awt.Rectangle}:
   * 
   * Computes the intersection of this <code>Rectangle</code> with the specified
   * <code>Rectangle</code>. Returns a new <code>Rectangle</code> that
   * represents the intersection of the two rectangles. If the two rectangles do
   * not intersect, the result will be an empty rectangle.
   *
   * @param r the specified <code>Rectangle</code>
   * @return the largest <code>Rectangle</code> contained in both the specified
   *         <code>Rectangle</code> and in this <code>Rectangle</code>; or if
   *         the rectangles do not intersect, an empty rectangle.
   */
  public Rectangle intersection(Rectangle r) {
    int tx1 = this.x;
    int ty1 = this.y;
    int rx1 = r.x;
    int ry1 = r.y;
    int tx2 = tx1;
    tx2 += this.width;
    int ty2 = ty1;
    ty2 += this.height;
    int rx2 = rx1;
    rx2 += r.width;
    int ry2 = ry1;
    ry2 += r.height;
    if (tx1 < rx1) tx1 = rx1;
    if (ty1 < ry1) ty1 = ry1;
    if (tx2 > rx2) tx2 = rx2;
    if (ty2 > ry2) ty2 = ry2;
    tx2 -= tx1;
    ty2 -= ty1;
    return new Rectangle(tx1, ty1, tx2, ty2);
  }

  public int area() {
    return width * height;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Rectangle rectangle = (Rectangle)o;

    if (height != rectangle.height) return false;
    if (width != rectangle.width) return false;
    if (x != rectangle.x) return false;
    if (y != rectangle.y) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = x;
    result = 31 * result + y;
    result = 31 * result + width;
    result = 31 * result + height;
    return result;
  }

  @Override
  public String toString() {
    return "Rectangle(" + ArrayUtils.toString(new int[]{x, y, width, height}, ", ") + ")"; 
  }
}
