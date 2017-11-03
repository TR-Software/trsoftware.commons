package solutions.trsoftware.commons.server.memquery.algebra;

import solutions.trsoftware.commons.server.memquery.RelationSchema;

/**
 * The leaf node in the relational expression tree: represents a single relation.
 *
 * @author Alex, 1/15/14
 */
public class RelationalValue implements RelationalExpression {

  private RelationSchema schema;

  public RelationalValue(RelationSchema schema) {
    this.schema = schema;
  }

  @Override
  public RelationSchema getOutputSchema() {
    return schema;
  }

  @Override
  public void accept(RelationalExpressionVisitor visitor) {
    visitor.visit(this);
  }

}
