package solutions.trsoftware.commons.server.memquery.expressions;

import solutions.trsoftware.commons.server.memquery.Row;

/**
 * @author Alex, 4/17/2015
 */
public abstract class ColValuePredicate<T> extends RowPredicate {

  protected final String colName;

  protected ColValuePredicate(String colName) {
    this.colName = colName;
  }

  @Override
  public final Boolean call(Row arg) {
    return eval((T)arg.getValue(colName));
  }

  public abstract boolean eval(T value);


  /** Factory method */
  public static IsNull isNull(String colName) {
    return new IsNull(colName);
  }

  /** Factory method */
  public static IsNotNull isNotNull(String colName) {
    return new IsNotNull(colName);
  }
}
