package solutions.trsoftware.commons.server.util;

import java.util.Random;

/**
 * Mar 19, 2010
 *
 * @author Alex
 */
public class ServerMathUtils {
  /**
   * Returns the next pseudorandom number in the generator's sequence
   * drawn from a Gaussian with the given mean and stdev.
   */
  public static double randomGaussian(Random rnd, double mean, double stdev) {
    return rnd.nextGaussian() * stdev + mean;
  }
}
