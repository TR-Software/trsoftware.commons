package solutions.trsoftware.commons.server.memquery.eval;

import solutions.trsoftware.commons.server.memquery.Relation;

/**
 * Simply returns the relation that was passed to its constructor.
 *
 * @author Alex, 1/16/14
 */
public class ValueEvaluator<R extends Relation> implements RelationalEvaluator<R> {

  private R value;

  public ValueEvaluator(R value) {
    this.value = value;
  }

  public R getValue() {
    return value;
  }

  @Override
  public R call() throws Exception {
    return value;
  }

  @Override
  public void accept(RelationalEvaluatorVisitor visitor) {
    visitor.visit(this);
  }
}
