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

import javax.annotation.Nonnull;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Immutable rectangle with {@code double}-precision position and dimensions, based on {@link java.awt.geom.Rectangle2D}.
 *
 * @author Alex
 */
public class Rectangle2D implements Rect {

  public final double x, y, width, height;

  public Rectangle2D(double x, double y, double width, double height) {
    this.x = x;
    this.y = y;
    // TODO: maybe, like int Rectangle, should we force the dimensions to be nonnegative (for proper operation of intersection)?
    this.width = width;
    this.height = height;
  }

  /**
   * Copy constructor
   */
  public Rectangle2D(Rectangle2D r) {
    this(r.x, r.y, r.width, r.height);
  }

  /**
   * Copy of {@link Rectangle}
   */
  public Rectangle2D(Rectangle r) {
    this(r.x, r.y, r.width, r.height);
  }

  /**
   * Copy of {@link DOMRect}
   */
  public Rectangle2D(DOMRect domRect) {
    this(domRect.getAbsoluteX(), domRect.getAbsoluteY(), domRect.getWidth(), domRect.getHeight());
  }

  /**
   * Factory method that can be used with {@code import static} for less-verbose instantiation.
   * @return a new rectangle constructed with {@link #Rectangle2D(double, double, double, double)}
   */
  public static Rectangle2D rect(double x, double y, double width, double height) {
    return new Rectangle2D(x, y, width, height);
  }

  @Override
  public double getX() {
    return x;
  }

  @Override
  public double getY() {
    return y;
  }

  @Override
  public double getWidth() {
    return width;
  }

  @Override
  public double getHeight() {
    return height;
  }

  // TODO: update doc and unit test this class


  /**
   * @return the smallest X coordinate of the rectangle
   */
  public double getMinX() {
    return getX();
  }

  /**
   * @return the smallest Y coordinate of the rectangle
   */
  public double getMinY() {
    return getY();
  }

  /**
   * @return the largest X coordinate of the rectangle
   */
  public double getMaxX() {
    return getX() + getWidth();
  }

  /**
   * @return the largest Y coordinate of the rectangle
   */
  public double getMaxY() {
    return getY() + getHeight();
  }

  /**
   * @return the X coordinate of the center of the rectangle
   */
  public double getCenterX() {
    return getX() + getWidth() / 2.0;
  }

  /**
   * @return the Y coordinate of the center of the rectangle
   */
  public double getCenterY() {
    return getY() + getHeight() / 2.0;
  }

  /**
   * Computes the intersection of this rectangle with the specified rectangle.
   * Returns a new rectangle that represents the intersection of the two rectangles.
   * If the two rectangles do not intersect, the result will be an {@linkplain #isEmpty() empty} rectangle.
   *
   * @param r the specified rectangle
   * @return the largest rectangle contained in both the specified rectangle and in this rectangle; or
   *         if the rectangles do not intersect, an {@linkplain #isEmpty() empty} rectangle.
   * @throws NullPointerException if argument is null
   */
  public Rectangle2D intersection(Rectangle2D r) {
    requireNonNull(r, "r");
    // Code adapted from java.awt.geom.Rectangle2D.intersect:
    double x1 = Math.max(this.getMinX(), r.getMinX());
    double y1 = Math.max(this.getMinY(), r.getMinY());
    double x2 = Math.min(this.getMaxX(), r.getMaxX());
    double y2 = Math.min(this.getMaxY(), r.getMaxY());
    return rect(x1, y1, x2-x1, y2-y1);
  }

  /**
   * Computes the {@linkplain #intersection(Rectangle2D) intersection} of the given rectangles
   * @return the largest rectangle contained in both of the specified rectangles;
   *   or if the rectangles do not intersect, an {@linkplain #isEmpty() empty} rectangle.
   * @throws NullPointerException if either argument is null
   */
  public static Rectangle2D intersection(@Nonnull Rectangle2D r1, @Nonnull Rectangle2D r2) {
    requireNonNull(r1, "r1");
    requireNonNull(r2, "r2");
    return r1.intersection(r2);
  }

  /**
   * Computes the union of this rectangle with the specified rectangle, which is
   * the smallest rectangle containing both the specified rectangle and this rectangle.
   * <p>
   * <i>Note:</i> unlike {@link Rectangle#union(Rectangle)}, this method doesn't differentiate between existant
   * and non-existant rectangles.  If any dimension is less than zero, it will still return a valid rectangle,
   * which may not always be the desired outcome.
   *
   * @param r the specified rectangle
   * @return the smallest rectangle containing both the specified rectangle and this rectangle
   * @throws NullPointerException if the argument is null
   */
  public Rectangle2D union(Rectangle2D r) {
    requireNonNull(r, "r");
    /* TODO: maybe reconcile the "non-existant" rectangle issues (i.e. impl of union from java.awt.geom.Rectangle2D vs java.awt.Rectangle)
         (the Rectangle2D version still returns a valid rect when either input has a negative dimension - is this correct?)
     */
    // Code adapted from java.awt.geom.Rectangle2D.union:
    double x1 = Math.min(this.getMinX(), r.getMinX());
    double y1 = Math.min(this.getMinY(), r.getMinY());
    double x2 = Math.max(this.getMaxX(), r.getMaxX());
    double y2 = Math.max(this.getMaxY(), r.getMaxY());
    return fromDiagonal(x1, y1, x2, y2);
  }

  /**
   * Computes the {@linkplain #union(Rectangle2D) union} of the given rectangles.
   * @return the smallest rectangle containing both of the specified rectangles
   * @throws NullPointerException if either argument is null
   */
  public static Rectangle2D union(@Nonnull Rectangle2D r1, @Nonnull Rectangle2D r2) {
    requireNonNull(r1, "r1");
    requireNonNull(r2, "r2");
    return r1.union(r2);
  }

  // corner points
  /* TODO:
      - move Point class to this package or create a new Point2D class in this package
      - maybe pull the corner point methods to Rect (as well as getCenterX|Y)
      - maybe even extract a Shape interface that defines the min/max/center x,y methods
  */
  public Point getTopLeft() {
    return new RealPoint(getMinX(), getMinY());
  }
  public Point getTopRight() {
    return new RealPoint(getMaxX(), getMinY());
  }
  public Point getBottomLeft() {
    return new RealPoint(getMinX(), getMaxY());
  }
  public Point getBottomRight() {
    return new RealPoint(getMaxX(), getMaxY());
  }
  public Point getCenter() {
    return new RealPoint(getCenterX(), getCenterY());
  }

  /**
   * @return the 4 corner points of this rectangle: [top-left, top-right, bottom-left, bottom-right]
   */
  public Point[] getCornerPoints() {
    return new Point[] {
        getTopLeft(),
        getTopRight(),
        getBottomLeft(),
        getBottomRight()
    };
  }

  /**
   * Creates a rectangle given two diagonal corner points (e.g. top-left and bottom-right).
   *
   * @param x1 the X coordinate of the start point of the specified diagonal
   * @param y1 the Y coordinate of the start point of the specified diagonal
   * @param x2 the X coordinate of the end point of the specified diagonal
   * @param y2 the Y coordinate of the end point of the specified diagonal
   * @see java.awt.geom.Rectangle2D#setFrameFromDiagonal(double, double, double, double)
   */
  public static Rectangle2D fromDiagonal(double x1, double y1,
                                         double x2, double y2) {
    if (x2 < x1) {
      double t = x1;
      x1 = x2;
      x2 = t;
    }
    if (y2 < y1) {
      double t = y1;
      y1 = y2;
      y2 = t;
    }
    return rect(x1, y1, x2 - x1, y2 - y1);
  }

  /**
   * @return {@code width} &times; {@code height}, or {@code 0} if either dimension is negative
   */
  public double area() {
    // TODO: if width or height is negative, does it make sense to return 0?
    return isEmpty() ? 0 : width * height;
  }

  /**
   * @return {@code true} if either width or height is &le; 0
   */
  public boolean isEmpty() {
    return width <= 0 || height <= 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Rectangle2D that = (Rectangle2D)o;
    return Double.compare(that.x, x) == 0 &&
        Double.compare(that.y, y) == 0 &&
        Double.compare(that.width, width) == 0 &&
        Double.compare(that.height, height) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y, width, height);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("x", x)
        .add("y", y)
        .add("width", width)
        .add("height", height)
        .toString();
  }
}
