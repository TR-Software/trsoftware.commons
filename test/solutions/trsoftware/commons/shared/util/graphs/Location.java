package solutions.trsoftware.commons.shared.util.graphs;

import javax.annotation.Nonnull;

import static solutions.trsoftware.commons.shared.util.MathUtils.floorMod;

/**
 * Represents a location in 2-dimensional space with integer {@code x} and {@code y} coordinates.
 * This class is immutable.
 */
public class Location {

  public final int x;
  public final int y;

  public Location(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Factory method that can be used with {@code import static} for a less-verbose way of creating {@link Location} instances.
   */
  @Nonnull
  public static Location loc(int x, int y) {
    return new Location(x, y);
  }

  /**
   * Returns a valid location on a grid of size {@code width} &times; {@code height} that corresponds to this location,
   * if either the {@code x} or {@code y} component of this location lies outside the bounds of such a board.
   * The resulting location will "wrap-around" the sides of the grid.
   *
   * @return an equivalent location where <code>x &isin; [0, width), y &isin; [0, height)</code>
   */
  public Location normalize(int width, int height) {
    int x = floorMod(this.x, width);
    int y = floorMod(this.y, height);
    if (x == this.x && y == this.y)
      return this;  // already normalized; avoid unnecessary allocation
    return loc(x, y);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Location location = (Location)o;

    if (x != location.x) return false;
    return y == location.y;
  }

  @Override
  public int hashCode() {
    int result = x;
    result = 31 * result + y;
    return result;
  }

  @Override
  public String toString() {
    return "(" + x + ", " + y + ")";
  }
}
