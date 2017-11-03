package solutions.trsoftware.commons.client.util;

import java.util.Random;

/**
 * @author Alex, 10/23/2017
 */
public class TestUtils {

  private static Random rnd = new Random();

  /**
   * Generates some {@code int}s useful for unit tests.
   *
   * @param n The number of random {@code int}s to include in the result.
   * @return an array consisting of 9 interesting edge cases in the 32-bit integer space,
   * plus {@code n} random {@code int}s.
   */
  public static int[] randomInts(int n) {
    int[] ret = new int[9+n];
    ret[0] = Integer.MIN_VALUE;
    ret[1] = Integer.MIN_VALUE+1;
    ret[2] = Integer.MAX_VALUE-1;
    ret[3] = Integer.MAX_VALUE;
    ret[4] = -2;
    ret[5] = -1;
    ret[6] = 0;
    ret[7] = 1;
    ret[8] = 2;
    for (int i = 9; i < ret.length; i++) {
      ret[i] = rnd.nextInt();
    }
    return ret;
  }

}
