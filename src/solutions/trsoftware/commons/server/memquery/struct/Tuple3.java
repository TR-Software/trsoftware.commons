package solutions.trsoftware.commons.server.memquery.struct;

/**
 * An immutable 3-tuple, with each element individually typed and accessed with a dedicated getter in addition to
 * the {@link #getValue(int)} method.
 *
 * @author Alex, 1/15/14
 */
public class Tuple3<A, B, C> extends Tuple2<A,B> {

  private final C c;

  public Tuple3(A a, B b, C c) {
    super(a, b);
    this.c = c;
  }

  public C getC() {
    return c;
  }

  @Override
  public int size() {
    return 3;
  }

  @Override
  public Object getValue(int index) {
    if (index == 2)
      return c;
    return super.getValue(index);
  }
}
