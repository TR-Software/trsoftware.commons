package solutions.trsoftware.commons.server.bridge.util;

import solutions.trsoftware.commons.client.bridge.util.RandomGen;

import java.util.Random;

/**
 * Date: Nov 26, 2008 Time: 6:38:31 PM
 *
 * @author Alex
 */
public class RandomGenJavaImpl extends RandomGen {

  private Random rnd = new Random();

  public boolean nextBoolean() {
    return rnd.nextBoolean();
  }

  public double nextDouble() {
    return rnd.nextDouble();
  }

  public int nextInt() {
    return rnd.nextInt();
  }

  public int nextInt(int upperBound) {
    return rnd.nextInt(upperBound);
  }
}
