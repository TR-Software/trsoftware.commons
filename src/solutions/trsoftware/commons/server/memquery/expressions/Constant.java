package solutions.trsoftware.commons.server.memquery.expressions;

/**
 * @author Alex, 1/11/14
 */
public class Constant<T> extends TypedExpression<Object, T> implements VisitableExpression<Object, T> {

  private final T value;

  public Constant(T value) {
    super(Object.class, (Class<T>)value.getClass());
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  @Override
  public T call(Object arg) {
    return value;
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visit(this);
  }

}
