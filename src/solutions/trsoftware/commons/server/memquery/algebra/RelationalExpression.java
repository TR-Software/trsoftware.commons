package solutions.trsoftware.commons.server.memquery.algebra;

import solutions.trsoftware.commons.server.memquery.RelationSchema;

/**
 * A relational algebra expression: either a value (relation) or an operation that produces a relation.
 *
 * @author Alex, 1/14/14
 */
public interface RelationalExpression {

  RelationSchema getOutputSchema();

  /** Iterate itself using the given visitor */
  void accept(RelationalExpressionVisitor visitor);
}
