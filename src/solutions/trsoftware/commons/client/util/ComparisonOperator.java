package solutions.trsoftware.commons.client.util;

/**
 * Provides an abstraction for numerical comparison operators.
 *
 * @author Alex, 1/11/14
 */
public enum ComparisonOperator {
  GT() {
    @Override
    protected boolean eval(int cmp) {
      return cmp > 0;
    }
    @Override
    public String toString() {
      return ">";
    }
  },
  GTE() {
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
  LTE() {
    @Override
    protected boolean eval(int cmp) {
      return cmp <= 0;
    }
    @Override
    public String toString() {
      return "<=";
    }
  },
  LT() {
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
