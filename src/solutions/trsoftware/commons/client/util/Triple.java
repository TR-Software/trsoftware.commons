package solutions.trsoftware.commons.client.util;

/**
 * Like Pair, tries to compensate for the lack of expando classes in Java.
 * Instances of this class are immutable.
 *
 * @author Alex
 */
public class Triple<A, B, C> {
  private final A first;
  private final B second;
  private final C third;

  public Triple(A first, B second, C third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }

  public A first() {
    return first;
  }

  public B second() {
    return second;
  }

  public C third() {
    return third;
  }
}
