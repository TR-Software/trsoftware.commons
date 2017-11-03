package solutions.trsoftware.commons.server.memquery.expressions;

import solutions.trsoftware.commons.server.memquery.Row;

/**
 * @author Alex, 6/4/2014
 */
public abstract class RowExpression<T> extends TypedExpression<Row, T> {

  public RowExpression(Class<T> resultType) {
    super(Row.class, resultType);
  }


}
