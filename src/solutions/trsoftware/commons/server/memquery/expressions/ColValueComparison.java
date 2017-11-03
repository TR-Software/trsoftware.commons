package solutions.trsoftware.commons.server.memquery.expressions;

import solutions.trsoftware.commons.client.util.ComparisonOperator;

/**
 * @author Alex, 4/17/2015
 */
public class ColValueComparison<T extends Comparable> extends ColValuePredicate<T> {

  private final ComparisonOperator op;
  private final T operand;

  public ColValueComparison(String colName, ComparisonOperator op, T operand) {
    super(colName);
    this.op = op;
    this.operand = operand;
  }

  @Override
  public boolean eval(T value) {
    return op.compare(value, operand);
  }

  @Override
  public String toString() {
    return new StringBuilder(colName).append(' ').append(op).append(' ').append(operand).toString();
  }
}
