package solutions.trsoftware.commons.client.util.stats;

/**
 * An arithmetic mean that can be updated with new samples;
 *
 * @author Alex
 */
public class Mean<N extends Number> {
  /** The current mean value of all the samples that have been given */
  private double mean = 0;
  /** Number of samples */
  private int n = 0;

  /** Updates the mean with a new sample, returning the new mean */
  public double update(N sample) {
    return update(sample.doubleValue());
  }

  /** Updates the mean with a new sample, returning the new mean */
  public double update(double sample) {
    return mean = ((mean * n) + sample) / ++n;
  }

  public double getMean() {
    return mean;
  }

  public int getNumSamples() {
    return n;
  }
}
