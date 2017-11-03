package solutions.trsoftware.commons.server.memquery.expressions;

/**
 * @author Alex, 1/11/14
 */
public enum ComparisonOperator {
  GREATER_THAN() {
    @Override
    protected boolean eval(int cmp) {
      return cmp > 0;
    }
    @Override
    public String toString() {
      return ">";
    }
  },
  GEQ() {
    @Override
    protected boolean eval(int cmp) {
      return cmp >= 0;
    }
    @Override
    public String toString() {
      return ">=";
    }
  },
  EQ() {
    @Override
    protected boolean eval(int cmp) {
      return cmp == 0;
    }
    @Override
    public String toString() {
      return "=";
    }
  },
  NEQ() {
    @Override
    protected boolean eval(int cmp) {
      return cmp != 0;
    }
    @Override
    public String toString() {
      return "!=";
    }
  },
  LEQ() {
    @Override
    protected boolean eval(int cmp) {
      return cmp <= 0;
    }
    @Override
    public String toString() {
      return "<=";
    }
  },
  LESS_THAN() {
    @Override
    protected boolean eval(int cmp) {
      return cmp < 0;
    }
    @Override
    public String toString() {
      return "<";
    }
  };

  protected abstract boolean eval(int cmp);

  public <T> boolean compare(Comparable<T> lhs, T rhs) {
    return eval(lhs.compareTo(rhs));
  }

}
