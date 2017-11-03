package solutions.trsoftware.commons.server.memquery.schema;

import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.server.memquery.expressions.Expression;

/**
 * A col spec that fetches its values from a given row by evaluating an expression on that row.
 *
 * @author Alex, 1/13/14
 */
public class ExpressionAccessorColSpec<T> extends NamedTypedColSpec<T> {

  private final Expression<Row, T> expression;

  public ExpressionAccessorColSpec(String name, Expression<Row, T> expression) {
    super(name, expression.getResultType());
    this.expression = expression;
  }

  public Expression getExpression() {
    return expression;
  }

  @Override
  public T getValue(Row row) {
    return expression.call(row);
  }
}
