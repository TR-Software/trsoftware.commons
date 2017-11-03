package solutions.trsoftware.commons.server.memquery.expressions;

import solutions.trsoftware.commons.server.memquery.Row;

/**
 * @author Alex, 4/17/2015
 */
public class CompoundRowPredicate extends RowPredicate {

  protected final RowPredicate lhs;
  protected final RowPredicate rhs;
  protected final BooleanBinaryOperator op;

  public CompoundRowPredicate(RowPredicate lhs, BooleanBinaryOperator op, RowPredicate rhs) {
    this.lhs = lhs;
    this.op = op;
    this.rhs = rhs;
  }

  @Override
  public Boolean call(Row arg) {
    return op.apply(lhs.call(arg), rhs.call(arg));
  }

  @Override
  public String toString() {
    return String.valueOf(lhs) + ' ' + op.name() + ' ' + rhs;
  }
}
