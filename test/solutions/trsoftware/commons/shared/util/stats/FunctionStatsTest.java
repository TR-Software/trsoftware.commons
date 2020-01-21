/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.shared.util.stats;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.ListUtils;
import solutions.trsoftware.commons.shared.util.RealPoint;

import java.util.Arrays;
import java.util.Collections;
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
    List<Point> expectedInflectionPoints = ListUtils.filter(points, Point::isInflection);
    List<Point> expectedMinimums = ListUtils.filter(points, point -> point.type == MIN);
    List<Point> expectedMaximums = ListUtils.filter(points, point -> point.type == MAX);
    // test with the points shuffled in various ways (to check that the class can handle points specified in any order)
    for (int i = 0; i < 10; i++) {
      FunctionStats fs = new FunctionStats<>(points);
      assertEquals(expectedInflectionPoints, fs.getInflections());
      assertEquals(expectedMinimums, fs.getMinimums());
      assertEquals(expectedMaximums, fs.getMaximums());
      assertEquals(2.0, fs.getPeriod());
      assertEquals(0.0, fs.getMin());
      assertEquals(1.0, fs.getMax());
      Collections.shuffle(points);
    }
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