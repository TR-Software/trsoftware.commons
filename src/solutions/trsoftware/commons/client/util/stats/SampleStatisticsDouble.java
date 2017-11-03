package solutions.trsoftware.commons.client.util.stats;

/**
 * Mar 26, 2009
 *
 * @author Alex
 */
public interface SampleStatisticsDouble extends Updatable<Double>, UpdatableDouble {
  int size();

  double min();

  double max();

  double sum();

  double mean();

  /** The upper median of the dataset (if there are 2 medians) */
  double median();

  double stdev();

  double variance();

  ImmutableStats<Double> summarize();
}
