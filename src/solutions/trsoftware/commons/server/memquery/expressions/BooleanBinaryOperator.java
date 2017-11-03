package solutions.trsoftware.commons.server.memquery.expressions;

/**
 * @author Alex, 1/11/14
 */
public enum BooleanBinaryOperator {
  AND() {
    @Override
    public boolean apply(boolean lhs, boolean rhs) {
      return lhs && rhs;
    }
  },
  OR() {
    @Override
    public boolean apply(boolean lhs, boolean rhs) {
      return lhs || rhs;
    }
  };

  public abstract boolean apply(boolean lhs, boolean rhs);

}
