package solutions.trsoftware.commons.server.memquery.eval;

import solutions.trsoftware.commons.client.util.callables.Function1;
import solutions.trsoftware.commons.server.memquery.Relation;
import solutions.trsoftware.commons.server.memquery.algebra.RelationalExpression;

/**
 * Evaluates a single relational algebra operation on an input relation to produce an output relation.
 *
 * @param <I> the input relation type.
 * @param <R> the output relation type.
 *
 * @author Alex, 1/15/14
 */
public abstract class UnaryOperationEvaluator<O extends RelationalExpression, I extends Relation, R extends Relation>
    extends OperationEvaluator<O, R> implements Function1<I, R> {

  protected final RelationalEvaluator<I> inputEvaluator;

  protected UnaryOperationEvaluator(O op, RelationalEvaluator<I> inputEvaluator) {
    super(op);
    this.inputEvaluator = inputEvaluator;
  }


  @Override
  public R call() throws Exception {
    return call(inputEvaluator.call());
  }

  @Override
  public void accept(RelationalEvaluatorVisitor visitor) {
    inputEvaluator.accept(visitor);
    visitor.visit(this);
  }
}
