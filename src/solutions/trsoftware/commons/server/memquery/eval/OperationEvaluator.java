package solutions.trsoftware.commons.server.memquery.eval;

import solutions.trsoftware.commons.server.memquery.MaterializedRelation;
import solutions.trsoftware.commons.server.memquery.Relation;
import solutions.trsoftware.commons.server.memquery.algebra.RelationalExpression;

/**
 * @author Alex, 1/15/14
 */
public abstract class OperationEvaluator<O extends RelationalExpression, R extends Relation> implements RelationalEvaluator<R> {

  protected final O op;

  protected boolean verbose = true;  // TODO: use this in all nontrivial evaluators and provide a way to set its value

  protected OperationEvaluator(O op) {
    this.op = op;
  }

  public O getOp() {
    return op;
  }

  /**
   * Produces a materialized view from a streaming input relation.
   */
  public static MaterializedRelation materialize(Relation input) {
    // TODO: impl this
    return null;
  }
}
