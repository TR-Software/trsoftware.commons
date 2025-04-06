package solutions.trsoftware.commons.shared.util.geometry;

import solutions.trsoftware.commons.client.dom.DOMRect;

/**
 * Defines the basic properties of a rectangle.
 *
 * @author Alex
 * @see Rectangle2D
 * @see DOMRect
 * @since 2/27/2025
 */
public interface Rect {
  /**
   * @return {@code x} coordinate of the rectangle's origin (typically the top-left corner of the rectangle)
   */
  double getX();

  /**
   * @return {@code y} coordinate of the rectangle's origin (typically the top-left corner of the rectangle)
   */
  double getY();

  /**
   * @return the width of the rectangle
   */
  double getWidth();

  /**
   * @return the height of the rectangle
   */
  double getHeight();

  /**
   * @return the left coordinate of the rectangle (defaults to {@link #getX()})
   */
  default double getLeft() {
    return getX();
  }

  /**
   * @return the top coordinate of the rectangle (defaults to {@link #getY()})
   */
  default double getTop() {
    return getY();
  }

  // TODO: maybe add similar default methods for right and bottom
}
