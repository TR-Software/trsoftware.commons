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

package solutions.trsoftware.commons.shared.util.stats;

import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * Base class for {@link MinDouble} and {@link MaxDouble}, which keep track of the min/max of a sequence of double values.
 *
 * @see DoubleStream#min()
 * @see DoubleStream#summaryStatistics()
 * @see Collectors#summarizingDouble
 * @author Alex
 */
public abstract class MinMaxDoubleBase<R extends MinMaxDoubleBase<R>> implements CollectableStats<Double, R>, UpdatableDouble, Serializable {
  // TODO(11/3/2021): create a similar implementation for primitive ints

  /** The current max or min value of all the samples that have been given */
  private double best = absoluteWorst();

  protected abstract double absoluteWorst();
  protected abstract double bestOf(double a, double b);

  protected MinMaxDoubleBase() {  // default constructor to support Serializable
  }

  protected MinMaxDoubleBase(double initialValue) {
    best = initialValue;
  }

  protected MinMaxDoubleBase(Iterable<Double> candidates) {
    updateAll(candidates);
  }

  protected MinMaxDoubleBase(double... candidates) {
    updateAll(candidates);
  }

  public double get() {
    return best;
  }

  /** Updates the current best value with a new sample, returning the new best value. */
  public void update(double x) {
    best = bestOf(best, x);
  }

  @Override
  public void update(Double x) {
    update(x.doubleValue());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MinMaxDoubleBase)) return false;
    MinMaxDoubleBase that = (MinMaxDoubleBase)o;
    return getClass().equals(o.getClass()) && Double.compare(that.best, best) == 0;
  }

  @Override
  public int hashCode() {
    long temp = Double.doubleToLongBits(best);
    int result = (int)(temp ^ (temp >>> 32));
    result = 31 * result + getClass().hashCode();
    return result;
  }
}