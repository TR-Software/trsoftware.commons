package solutions.trsoftware.commons.server.memquery.algebra;

import solutions.trsoftware.commons.server.memquery.RelationSchema;

/**
 * @author Alex, 1/14/14
 */
public abstract class UnaryOperation<P> extends RelationalOperation<P> {

  protected final RelationalExpression input;

  public UnaryOperation(RelationalExpression input, P parameters) {
    super(parameters);
    this.input = input;
  }

  public RelationalExpression getInput() {
    return input;
  }

  public RelationSchema getInputSchema() {
    return input.getOutputSchema();
  }

  @Override
  protected String createOutputName() {
    return getInputSchema().getName();
  }

  @Override
  public void accept(RelationalExpressionVisitor visitor) {
    getInput().accept(visitor);
    visitor.visit(this);
  }
}
