package solutions.trsoftware.commons.client.util.stats;

import java.io.Serializable;

/**
 * Base class for MinDouble and MaxDouble, which keep track of the min/max of a sequence of double values.
 *
 * @author Alex
 */
public abstract class MinMaxDoubleBase implements Serializable {
  /** The current max or min value of all the samples that have been given */
  private double best = absoluteWorst();

  protected abstract double absoluteWorst();
  protected abstract double bestOf(double a, double b);

  protected MinMaxDoubleBase() {
  }

  protected MinMaxDoubleBase(double initialValue) {
    best = initialValue;
  }

  protected MinMaxDoubleBase(Iterable<Double> candidates) {
    update(candidates);
  }

  protected MinMaxDoubleBase(double... candidates) {
    update(candidates);
  }

  public double get() {
    return best;
  }

  /** Updates the current best value with a new sample, returning the new best value. */
  public double update(double x) {
    best = bestOf(best, x);
    return best;
  }

  public double update(Iterable<Double> candidates) {
    for (Double x : candidates) {
      update(x);
    }
    return best;
  }

  public double update(double... candidates) {
    for (double x : candidates) {
      update(x);
    }
    return best;
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