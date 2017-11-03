package solutions.trsoftware.commons.server.memquery.expressions;

import solutions.trsoftware.commons.server.memquery.Row;

/**
 * @author Alex, 11/2/2016
 */
public class And extends CompoundRowPredicate {

  public And(RowPredicate lhs, RowPredicate rhs) {
    super(lhs, BooleanBinaryOperator.AND, rhs);
  }

  @Override
  public Boolean call(Row arg) {
    // we override this method to short-circuit the evaluation when the LHS expression is false
    return lhs.call(arg) && rhs.call(arg);
  }
}
