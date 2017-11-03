package solutions.trsoftware.commons.server.memquery.eval;

import solutions.trsoftware.commons.client.util.callables.Function2;
import solutions.trsoftware.commons.server.memquery.Relation;
import solutions.trsoftware.commons.server.memquery.algebra.RelationalExpression;

/**
 * Evaluates a single relational algebra operation on two input relations to produce an output relation.
 *
 * @param <O> the operation type.
 * @param <L> the LHS input relation type.
 * @param <R> the RHS input relation type.
 * @param <T> the output relation type.
 *
 * @author Alex, 1/15/14
 */
public abstract class BinaryOperationEvaluator<O extends RelationalExpression, L extends Relation, R extends Relation, T extends Relation>
    extends OperationEvaluator<O, T> implements Function2<L, R, T> {

  protected final RelationalEvaluator<L> lhsEvaluator;
  protected final RelationalEvaluator<R> rhsEvaluator;

  protected BinaryOperationEvaluator(O op, RelationalEvaluator<L> lhsEvaluator, RelationalEvaluator<R> rhsEvaluator) {
    super(op);
    this.lhsEvaluator = lhsEvaluator;
    this.rhsEvaluator = rhsEvaluator;
  }

  @Override
  public T call() throws Exception {
    return call(lhsEvaluator.call(), rhsEvaluator.call());
  }

  @Override
  public void accept(RelationalEvaluatorVisitor visitor) {
    lhsEvaluator.accept(visitor);
    rhsEvaluator.accept(visitor);
    visitor.visit(this);
  }
}
