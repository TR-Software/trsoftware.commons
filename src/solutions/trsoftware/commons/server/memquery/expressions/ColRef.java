package solutions.trsoftware.commons.server.memquery.expressions;

import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.server.memquery.schema.ColSpec;

/**
 * @author Alex, 1/11/14
 */
public class ColRef<T> extends RowExpression<T> implements VisitableExpression<Row, T>  {

  private final ColSpec<T> colSpec;

  public ColRef(ColSpec<T> colSpec) {
    super(colSpec.getType());
    this.colSpec = colSpec;
  }

  public ColSpec getColSpec() {
    return colSpec;
  }

  @Override
  public T call(Row arg) {
    return colSpec.getValue(arg);
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visit(this);
  }
}
