package solutions.trsoftware.commons.server.memquery.expressions;

/**
 * @author Alex, 4/17/2015
 */
public abstract class RowPredicate extends RowExpression<Boolean> {

  public RowPredicate() {
    super(Boolean.TYPE);
  }

  public static And and(RowPredicate lhs, RowPredicate rhs) {
    return new And(lhs, rhs);
  }

  public static Or or(RowPredicate lhs, RowPredicate rhs) {
    return new Or(lhs, rhs);
  }
}
