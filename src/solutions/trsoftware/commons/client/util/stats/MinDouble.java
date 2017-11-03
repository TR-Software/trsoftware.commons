package solutions.trsoftware.commons.client.util.stats;

import java.io.Serializable;

/**
 * Keeps track of the min of a sequence of double values.
 *
 * @author Alex
 */
public class MinDouble extends MinMaxDoubleBase implements Serializable, Mergeable<MinDouble> {

  @Override
  protected double absoluteWorst() {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  protected double bestOf(double a, double b) {
    return Math.min(a, b);
  }

  public MinDouble() {
  }

  public MinDouble(double initialValue) {
    super(initialValue);
  }

  public MinDouble(Iterable<Double> candidates) {
    super(candidates);
  }

  public MinDouble(double... candidates) {
    super(candidates);
  }

  @Override
  public void merge(MinDouble other) {
    update(other.get());
  }
}