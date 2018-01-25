package solutions.trsoftware.commons.shared.util.stats;

import com.google.common.base.Predicate;
import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.ListUtils;
import solutions.trsoftware.commons.shared.util.RealPoint;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static solutions.trsoftware.commons.shared.util.stats.FunctionStatsTest.PointType.MAX;
import static solutions.trsoftware.commons.shared.util.stats.FunctionStatsTest.PointType.MIN;

/**
 * @author Alex
 * @since 12/18/2017
 */
public class FunctionStatsTest extends TestCase {

  public void testInflections() throws Exception {
    List<Point> points = Arrays.asList(
        new Point(0, 1, MAX),
        new Point(.5, .5),
        new Point(1, 0, MIN),
        new Point(1.5, .5),
        new Point(2, 1, MAX),
        new Point(2.5, .5),
        new Point(3, 0, MIN)
    );
    List<Point> expectedInflectionPoints = ListUtils.filter(points, new Predicate<Point>() {
      @Override
      public boolean apply(@Nullable Point input) {
        assert input != null;
        return input.isInflection();
      }
    });
    List<Point> expectedMinimums = ListUtils.filter(points, new Predicate<Point>() {
      @Override
      public boolean apply(@Nullable Point input) {
        assert input != null;
        return input.type == MIN;
      }
    });
    List<Point> expectedMaximums = ListUtils.filter(points, new Predicate<Point>() {
      @Override
      public boolean apply(@Nullable Point input) {
        assert input != null;
        return input.type == MAX;
      }
    });
    FunctionStats fs = new FunctionStats<Point>(points);
    assertEquals(expectedInflectionPoints, fs.getInflections());
    assertEquals(expectedMinimums, fs.getMinimums());
    assertEquals(expectedMaximums, fs.getMaximums());
    assertEquals(2.0, fs.getPeriod());
    assertEquals(0.0, fs.getMin());
    assertEquals(1.0, fs.getMax());
  }

  enum PointType {MIN, MAX}

  static class Point extends RealPoint {
    private PointType type;

    public Point(double x, double y, PointType type) {
      super(x, y);
      this.type = type;
    }

    public Point(double x, double y) {
      super(x, y);
    }

    boolean isInflection() {
      return type != null;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      if (!super.equals(o))
        return false;

      Point point = (Point)o;

      return type == point.type;
    }

    @Override
    public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + (type != null ? type.hashCode() : 0);
      return result;
    }
  }
}