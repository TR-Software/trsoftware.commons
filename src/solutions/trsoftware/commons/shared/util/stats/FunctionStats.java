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

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import solutions.trsoftware.commons.shared.util.Point;
import solutions.trsoftware.commons.shared.util.iterators.FilteringIterator;

import java.util.ArrayList;
import java.util.List;

import static solutions.trsoftware.commons.shared.util.stats.FunctionStats.Direction.DOWN;
import static solutions.trsoftware.commons.shared.util.stats.FunctionStats.Direction.UP;

/**
 * Computes the basic properties of a discrete (mathematical) function, specified as a set of {@linkplain Point points}.
 * The computed properties include the inflection points and the local/absolute minimums/maximums.
 *
 * @author Alex
 * @since 12/18/2017
 */
public class FunctionStats<P extends Point> {

  /**
   * Helper used to compute the inflection points
   * @see #getDirection(double, double)
   */
  enum Direction {UP, DOWN}

  private List<P> points;
  private List<P> inflections = new ArrayList<P>();
  private List<P> minimums = new ArrayList<P>();
  private List<P> maximums = new ArrayList<P>();
  private MinAndMaxDouble minAndMax = new MinAndMaxDouble();

  public FunctionStats(List<P> points) {
    this.points = points;
    findInflections();
  }

  private FilteringIterator<P> filterDuplicates() {
    return new FilteringIterator<P>(points.iterator()) {
      private P last;
      @Override
      protected boolean filter(P elt) {
        boolean ret = last == null || last.getY() != elt.getY();
        last = elt;
        return ret;
      }
    };
  }

  private void findInflections() {
    Direction dir = null;
    for (PeekingIterator<P> it = Iterators.peekingIterator(filterDuplicates()); it.hasNext(); ) {
      P pt = it.next();
      double y = pt.getY();
      minAndMax.update(y);
      if (it.hasNext()) {
        Direction newDir = getDirection(y, it.peek().getY());
        if (dir != newDir) {
          // change of direction: this point is either a local minimum or maximum
          if (newDir == UP) {
            inflections.add(pt);
            minimums.add(pt);
          }
          else if (newDir == DOWN) {
            inflections.add(pt);
            maximums.add(pt);
          }
          dir = newDir;
        }
      }
      else {
        // this is the last point, which we always consider to be a point of inflection
        inflections.add(pt);
        // if we have a direction, then this point can be considered a local min or max
        if (dir == UP)
          maximums.add(pt);
        else if (dir == DOWN)
          minimums.add(pt);
      }
    }
  }

  private Direction getDirection(double a, double b) {
    if (a < b)
      return UP;
    else if (a > b)
      return DOWN;
    return null; // no change in direction
  }

  public List<P> getPoints() {
    return points;
  }

  /**
   * @return all inflection points of the function
   * @see #getMinimums()
   * @see #getMaximums()
   */
  public List<P> getInflections() {
    return inflections;
  }

  /**
   * @return the local minimums of the function
   */
  public List<P> getMinimums() {
    return minimums;
  }

  /**
   * @return the local maximums of the function
   */
  public List<P> getMaximums() {
    return maximums;
  }

  /**
   * @return the absolute minimum y-value the function
   */
  public double getMin() {
    return minAndMax.getMin();
  }
  
  /**
   * @return the absolute maximum of the function
   */
  public double getMax() {
    return minAndMax.getMax();
  }

  /**
   * @return The average x interval between 2 consecutive maximums
   */
  public double getPeriod() {
    if (maximums.isEmpty())
      return Double.POSITIVE_INFINITY;
    MeanAndVariance stats = new MeanAndVariance();
    for (PeekingIterator<P> it = Iterators.peekingIterator(maximums.iterator()); it.hasNext(); ) {
      P pt = it.next();
      if (it.hasNext()) {
        stats.update(it.peek().getX() - pt.getX());
      }
    }
    return stats.mean();
  }
}
