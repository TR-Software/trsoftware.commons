package solutions.trsoftware.commons.client.util.stats;

/**
 * Calculates the fraction of the distance between <code>start</code> and <code>end</code> for any input <code>x</code>.
 *
 * @author Alex, 1/7/14
 */
public class ProgressCalculator {

  private final double start;
  private final double total;

  public ProgressCalculator(double min, double max) {
    this.start = min;
    this.total = max - min;
  }

  /** Returns the fraction of the total distance between min and max covered by x */
  public double calcProgress(double x) {
    return (x - start) / total;
  }

}
