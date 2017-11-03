package solutions.trsoftware.commons.server.memquery.struct;

/**
 * An immutable 2-tuple, with each element individually typed and accessed with a dedicated getter in addition to
 * the {@link #getValue(int)} method.
 *
 * @author Alex, 1/15/14
 */
public class Tuple2<A, B> implements OrderedTuple {

  private final A a;
  private final B b;

  public Tuple2(A a, B b) {
    this.a = a;
    this.b = b;
  }

  public A getA() {
    return a;
  }

  public B getB() {
    return b;
  }

  @Override
  public int size() {
    return 2;
  }

  @Override
  public Object getValue(int index) {
    switch (index) {
      case 0:
        return a;
      case 1:
        return b;
    }
    throw new IndexOutOfBoundsException(Integer.toString(index));
  }
}
