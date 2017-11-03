package solutions.trsoftware.commons.client.util.stats;

/**
 * Mar 26, 2009
 *
 * @author Alex
 */
public interface SampleStatistics<N extends Number> extends Updatable<N> {
  int size();

  N min();

  N max();

  double sum();

  double mean();

  /** The upper median of the dataset (if there are 2 medians) */
  N median();

  double stdev();

  double variance();

  ImmutableStats<N> summarize();
}
