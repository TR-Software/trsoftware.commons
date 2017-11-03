package solutions.trsoftware.commons.client.util.stats;

/**
 * A superclass for the {@link ArgMax} and {@link ArgMin} classes.
 *
 * @param <A> the arg type
 * @param <V> the value type produced by the arg
 *
 * @author Alex
 */
public class AbstractArgMinMax<A, V extends Comparable<V>> {
  /** The current max or min value of all the samples that have been given */
  private V bestValue;

  /** The current argument associated with the best value */
  private A bestArg;

  private int multiplier;

  public AbstractArgMinMax(int multiplier) {
    this.multiplier = multiplier;
  }

  /** Updates the mean with a new sample, returning the new argmax */
  public V update(A arg, V value) {
    if (bestValue == null) {
      bestValue = value;
      bestArg = arg;
    }
    else if ((multiplier * bestValue.compareTo(value)) < 0) {
      bestValue = value;
      bestArg = arg;
    }
    return bestValue;
  }

  public A get() {
    return bestArg;
  }

}