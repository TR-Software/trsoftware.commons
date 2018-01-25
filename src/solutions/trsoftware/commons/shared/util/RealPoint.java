package solutions.trsoftware.commons.shared.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Point} whose {@code x,y} components are real numbers (represented as {@code double}).
 *
 * @author Alex
 * @since 12/18/2017
 */
public class RealPoint implements Point {

  private double x;
  private double y;

  public RealPoint(double x, double y) {
    this.x = x;
    this.y = y;
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
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    RealPoint realPoint = (RealPoint)o;
    return Double.compare(realPoint.x, x) == 0 && Double.compare(realPoint.y, y) == 0;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(x);
    result = (int)(temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(y);
    result = 31 * result + (int)(temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "(" + x + ", " + y + ")";
  }

  /**
   * @param values array of alternating x/y values
   * @return a list of instances constructed from the given array of alternating x/y values; Example:
   * <pre>
   *   makePoints(1,2,3,4) &rarr; [(1,2),(3,4)]
   * </pre>
   * @throws IllegalArgumentException if the given array doesn't have an even number of elements
   */
  public static List<RealPoint> makePoints(double... values) {
    if (values.length % 2 != 0)
      throw new IllegalArgumentException("Expected even number of values");
    List<RealPoint> points = new ArrayList<RealPoint>();
    for (int i = 0; i < values.length; i++) {
      if (i % 2 == 1)
        points.add(new RealPoint(values[i - 1], values[i]));
    }
    return points;
  }

}
