/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.commons.shared.util.geometry;

import com.google.common.base.MoreObjects;
import solutions.trsoftware.commons.client.dom.DOMRect;

/**
 * Immutable rectangle with integer position and dimensions, based on {@link java.awt.Rectangle}.
 *
 * @see Rectangle2D
 * @author Alex
 */
public class Rectangle {  // TODO: maybe impl Rect (with getters returning double values)
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
   * Sets the bounds of this {@code Rectangle} to the integer bounds
   * which encompass the specified {@code x}, {@code y}, {@code width},
   * and {@code height}.
   * If the parameters specify a {@code Rectangle} that exceeds the
   * maximum range of integers, the result will be the best
   * representation of the specified {@code Rectangle} intersected
   * with the maximum integer bounds.
   * @param x the X coordinate of the upper-left corner
   * @param y the Y coordinate of the upper-left corner
   * @param width the width of the specified rectangle
   * @param height the height of the specified rectangle
   */
  public Rectangle(double x, double y, double width, double height) {
    // copied from java.awt.Rectangle.setRect
    int newx, newy, neww, newh;

    if (x > 2.0 * Integer.MAX_VALUE) {
      // Too far in positive X direction to represent...
      // We cannot even reach the left side of the specified
      // rectangle even with both x & width set to MAX_VALUE.
      // The intersection with the "maximal integer rectangle"
      // is non-existant so we should use a width < 0.
      // REMIND: Should we try to determine a more "meaningful"
      // adjusted value for neww than just "-1"?
      newx = Integer.MAX_VALUE;
      neww = -1;
    } else {
      newx = clip(x, false);
      if (width >= 0) width += x-newx;
      neww = clip(width, width >= 0);
    }

    if (y > 2.0 * Integer.MAX_VALUE) {
      // Too far in positive Y direction to represent...
      newy = Integer.MAX_VALUE;
      newh = -1;
    } else {
      newy = clip(y, false);
      if (height >= 0) height += y-newy;
      newh = clip(height, height >= 0);
    }

    this.x = newx;
    this.y = newy;
    this.width = neww;
    this.height = newh;
    // TODO: maybe force the dimensions to be non-negative (for proper operation of intersection)?
  }

  /**
   * Copy constructor
   */
  public Rectangle(Rectangle r) {
    this(r.x, r.y, r.width, r.height);
  }

  /**
   * Delegates to the {@link #Rectangle(double, double, double, double)} constructor to convert the {@code double}
   * values specified by the given {@link Rectangle2D} to {@code int}.
   */
  public Rectangle(Rectangle2D r) {
    this(r.x, r.y, r.width, r.height);
  }

  public Rectangle(DOMRect domRect) {
    this(domRect.getAbsoluteX(), domRect.getAbsoluteY(), domRect.getWidth(), domRect.getHeight());
  }

  /**
   * Return best integer representation for v, clipped to integer range and floor-ed or ceiling-ed, depending on the boolean.
   */
  private static int clip(double v, boolean doceil) {
    // copied from java.awt.Rectangle.clip
    if (v <= Integer.MIN_VALUE) {
      return Integer.MIN_VALUE;
    }
    if (v >= Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    }
    return (int) (doceil ? Math.ceil(v) : Math.floor(v));
  }

  /**
   * Computes the intersection of this rectangle with the specified rectangle.
   * Returns a new rectangle that represents the intersection of the two rectangles.
   * If the two rectangles do not intersect, the result will be an empty rectangle.
   *
   * @param r the specified rectangle
   * @return the largest rectangle contained in both the specified rectangle and in this rectangle;
   *    or if the rectangles do not intersect, an empty rectangle.
   */
  public Rectangle intersection(Rectangle r) {
    // code adopted from java.awt.Rectangle#intersection
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

  /**
   * Computes the union of this rectangle with the specified rectangle.
   * <p>&nbsp;
   * <p>
   * If either rectangle has any dimension less than zero the rules for "non-existant" rectangles apply
   * (see documentation of {@link java.awt.Rectangle}).
   * <p>
   * If only one has a dimension less than zero, then the result will be a copy of the other rectangle.
   * <p>
   * If both have dimension less than zero, then the result will have at least one dimension less than zero.
   * <p>
   * If the resulting rectangle would have a dimension too large to be expressed as an {@code int}, the result
   * will have a dimension of {@link Integer#MAX_VALUE} along that axis.
   * @param r the specified rectangle
   * @return the smallest rectangle containing both the specified rectangle and this rectangle
   */
  public Rectangle union(Rectangle r) {
    long tx2 = this.width;
    long ty2 = this.height;
    if ((tx2 | ty2) < 0) {
      // This rectangle has negative dimensions...
      // If r has non-negative dimensions then it is the answer.
      // If r is non-existant (has a negative dimension), then both
      // are non-existant and we can return any non-existant rectangle
      // as an answer.  Thus, returning r meets that criterion.
      // Either way, r is our answer.
      return new Rectangle(r);
    }
    long rx2 = r.width;
    long ry2 = r.height;
    if ((rx2 | ry2) < 0) {
        return new Rectangle(this);
    }
    int tx1 = this.x;
    int ty1 = this.y;
    tx2 += tx1;
    ty2 += ty1;
    int rx1 = r.x;
    int ry1 = r.y;
    rx2 += rx1;
    ry2 += ry1;
    if (tx1 > rx1) tx1 = rx1;
    if (ty1 > ry1) ty1 = ry1;
    if (tx2 < rx2) tx2 = rx2;
    if (ty2 < ry2) ty2 = ry2;
    tx2 -= tx1;
    ty2 -= ty1;
    // tx2,ty2 will never underflow since both original rectangles
    // were already proven to be non-empty
    // they might overflow, though...
    if (tx2 > Integer.MAX_VALUE) tx2 = Integer.MAX_VALUE;
    if (ty2 > Integer.MAX_VALUE) ty2 = Integer.MAX_VALUE;
    return new Rectangle(tx1, ty1, (int) tx2, (int) ty2);
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
    return MoreObjects.toStringHelper(this)
        .addValue(x).addValue(y)
        .addValue(width).addValue(height)
        .toString();
  }
}
