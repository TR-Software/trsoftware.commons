package solutions.trsoftware.commons.client.util.stats;

import java.io.Serializable;

/**
 * A superclass for the MaxComparable and MinComparable classes.
 *
 * @author Alex
 */
public abstract class MinMaxComparableBase<T extends Comparable<T>> implements Serializable {
  /** The current max or min value of all the samples that have been given */
  private T best;
  
  private int multiplier;

  protected MinMaxComparableBase(int multiplier) {
    this.multiplier = multiplier;
  }

  protected MinMaxComparableBase(int multiplier, Iterable<T> candidates) {
    this.multiplier = multiplier;
    updateAll(candidates);
  }

  private MinMaxComparableBase() {
    // default constructor for serialization
  }

  /** Updates the current best value with a new sample, returning the new best value */
  public T update(T candidate) {
    if (best == null)
      best = candidate;
    else if ((multiplier * best.compareTo(candidate)) < 0)
      best = candidate;
    return best;
  }

  public T updateAll(Iterable<T> candidates) {
    for (T candidate : candidates) {
      update(candidate);
    }
    return get();
  }

  public T updateAll(T... candidates) {
    for (T candidate : candidates) {
      update(candidate);
    }
    return get();
  }

  public T get() {
    return best;
  }

}