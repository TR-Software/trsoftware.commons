package solutions.trsoftware.commons.server.memquery.algebra;

import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.server.memquery.expressions.Expression;
import solutions.trsoftware.commons.server.memquery.schema.ColSpec;
import solutions.trsoftware.commons.server.memquery.schema.NameAccessorColSpec;

/**
 * A relational selection (&sigma;) operation.  Produces an output relation with the same schema as the input relation
 * and containing the subset of rows matching the filter expression.
 *
 * @author Alex, 1/14/14
 */
public class Selection extends StreamableUnaryOperation<Expression<Row, Boolean>> {

  public Selection(RelationalExpression input, Expression<Row, Boolean> filterExpression) {
    super(input, filterExpression);
  }

  @Override
  protected Iterable<String> getOutputColNames() {
    return getInputSchema().getColNames();
  }

  @Override
  protected ColSpec createColSpec(String name) {
    return new NameAccessorColSpec(getInputSchema().get(name));
  }

}
