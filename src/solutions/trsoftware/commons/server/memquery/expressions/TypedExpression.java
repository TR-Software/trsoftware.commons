package solutions.trsoftware.commons.server.memquery.expressions;

/**
 * A base class for expressions.  Encapsulates the result type and the arg type of the expression.
 *
 * @author Alex, 1/11/14
 */
public abstract class TypedExpression<A, R> implements Expression<A, R> {

  protected final Class<A> argType;
  protected Class<R> resultType;

  protected TypedExpression(Class<A> argType, Class<R> resultType) {
    this.resultType = resultType;
    this.argType = argType;
  }

  @Override
  public Class<R> getResultType() {
    return resultType;
  }

  @Override
  public Class<A> getArgType() {
    return argType;
  }


}
