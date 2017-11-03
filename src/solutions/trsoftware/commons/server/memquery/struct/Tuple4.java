package solutions.trsoftware.commons.server.memquery.struct;

/**
 * An immutable 4-tuple, with each element individually typed and accessed with a dedicated getter in addition to
 * the {@link #getValue(int)} method.
 *
 * @author Alex, 1/15/14
 */
public class Tuple4<A, B, C, D> extends Tuple3<A,B, C> {

  private final D d;

  public Tuple4(A a, B b, C c, D d) {
    super(a, b, c);
    this.d = d;
  }

  public D getD() {
    return d;
  }

  @Override
  public int size() {
    return 4;
  }

  @Override
  public Object getValue(int index) {
    if (index == 3)
      return d;
    return super.getValue(index);
  }
}
