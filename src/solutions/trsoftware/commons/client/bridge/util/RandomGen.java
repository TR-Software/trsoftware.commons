package solutions.trsoftware.commons.client.bridge.util;

import solutions.trsoftware.commons.bridge.BridgeTypeFactory;

/**
 * A bridge between the GWT and Java versions of random number generation.
 *
 * @author Alex
 * @deprecated GWT 2.5 includes an emulated version of {@link java.util.Random}, so this bridge class is now redundant.
 * TODO: replace all usages of this class with java.util.Random
 */
public abstract class RandomGen {

  /**
   * Returns true or false with roughly equal probability. The underlying
   * browser's random implementation is used.
   */
  public abstract boolean nextBoolean();

  /**
   * Returns a random <code>double</code> between 0 (inclusive) and 1
   * (exclusive). The underlying browser's random implementation is used.
   */
  public abstract double nextDouble();

  /**
   * Returns a random <code>int</code> between -2147483648 and 2147483647
   * (inclusive) with roughly equal probability of returning any particular
   * <code>int</code> in this range. The underlying browser's random
   * implementation is used.
   */
  public abstract int nextInt();

  /**
   * Returns a random <code>int</code> between 0 (inclusive) and
   * <code>upperBound</code> (exclusive) with roughly equal probability of
   * returning any particular <code>int</code> in this range. The underlying
   * browser's random implementation is used.
   */
  public abstract int nextInt(int upperBound);

  /**
   * Returns a random <code>int</code> between lowerBound (inclusive) and
   * <code>upperBound</code> (exclusive) with roughly equal probability of
   * returning any particular <code>int</code> in this range.
   * 
   * This is not a standard method provided by a typical RNG, we just provide it 
   * for convenience here.
   *
   * @param lowerBound inclusive
   * @param upperBound exclusive
   */
  public int nextIntInRange(int lowerBound, int upperBound) {
    if (lowerBound >= upperBound)
      throw new IllegalArgumentException("lowerBound < upperBound must be true");
    return nextInt(upperBound - lowerBound) + lowerBound;
  }

  /** Provides the appropriate instance for the present context (either GWT or Java). */
  public static RandomGen getInstance() {
    return BridgeTypeFactory.newRandomGen();
  }

}
