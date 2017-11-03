package solutions.trsoftware.commons.client.util.stats;

import java.io.Serializable;

/**
 * Keeps track of the max of a sequence of double values.
 *
 * @author Alex
 */
public class MaxDouble extends MinMaxDoubleBase implements Serializable, Mergeable<MaxDouble> {

  @Override
  protected double absoluteWorst() {
    return Double.NEGATIVE_INFINITY;
  }

  @Override
  protected double bestOf(double a, double b) {
    return Math.max(a, b);
  }

  public MaxDouble() {
  }

  public MaxDouble(double initialValue) {
    super(initialValue);
  }

  public MaxDouble(Iterable<Double> candidates) {
    super(candidates);
  }

  public MaxDouble(double... candidates) {
    super(candidates);
  }

  @Override
  public void merge(MaxDouble other) {
    update(other.get());
  }
}