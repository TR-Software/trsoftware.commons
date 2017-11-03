package solutions.trsoftware.commons.server.memquery.expressions;

import solutions.trsoftware.commons.server.memquery.Row;

/**
 * @author Alex, 11/2/2016
 */
public class Or extends CompoundRowPredicate {

  public Or(RowPredicate lhs, RowPredicate rhs) {
    super(lhs, BooleanBinaryOperator.OR, rhs);
  }


  @Override
  public Boolean call(Row arg) {
    // we override this method to short-circuit the evaluation when the LHS expression is true
    return lhs.call(arg) || rhs.call(arg);
  }
}
