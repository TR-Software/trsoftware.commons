package solutions.trsoftware.commons.client.util.stats;

/**
 * Like {@link MinComparable}, but instead of finding a min value in a sequence, this class finds the argument that
 * produced that min value in the sequence.
 *
 * Similar to the mathematical <a href="https://en.wikipedia.org/wiki/Arg_max#Arg_min">argmin</a> function.
 *
 * @author Alex Mar 2, 2010
 */
public class ArgMin<A, T extends Comparable<T>> extends AbstractArgMinMax<A,T> {
  public ArgMin() {
    super(-1);
  }
}
