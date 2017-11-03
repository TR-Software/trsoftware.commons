package solutions.trsoftware.commons.client.bridge.util.impl;

import com.google.gwt.user.client.Random;
import solutions.trsoftware.commons.client.bridge.util.RandomGen;

/**
 * Date: Nov 26, 2008 Time: 6:37:31 PM
 *
 * @author Alex
 */
public class RandomGenGwtImpl extends RandomGen {
  public boolean nextBoolean() {
    return Random.nextBoolean();
  }

  public double nextDouble() {
    return Random.nextDouble();
  }

  public int nextInt() {
    return Random.nextInt();
  }

  public int nextInt(int upperBound) {
    // make sure the upperBound is positive, to match java.util.Random's behavior
    if (upperBound <= 0)
      throw new IllegalArgumentException("upperBound must be positive");
    return Random.nextInt(upperBound);
  }
}
