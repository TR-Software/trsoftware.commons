package solutions.trsoftware.commons.client.util.stats;

/**
 * Like {@link MaxComparable}, but instead of finding a max value in a sequence, this class finds the argument that
 * produced that max value in the sequence.
 *
 * Similar to the mathematical <a href="https://en.wikipedia.org/wiki/Arg_max">argmax</a> function.
 *
 * @author Alex
 * @since Mar 2, 2010
 */
public class ArgMax<A, T extends Comparable<T>> extends AbstractArgMinMax<A, T> {
  public ArgMax() {
    super(1);
  }
}
