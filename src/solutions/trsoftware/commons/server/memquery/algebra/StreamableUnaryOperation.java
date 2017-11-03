package solutions.trsoftware.commons.server.memquery.algebra;

import solutions.trsoftware.commons.client.util.callables.Function1;
import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.server.memquery.RowImpl;

/**
 * An unary operation that can be evaluated one row at a time.  Implements {@link Function1} to do
 * just that.
 *
 * @author Alex, 1/15/14
 */
public abstract class StreamableUnaryOperation<P> extends UnaryOperation<P> implements Function1<Row, Row> {

  public StreamableUnaryOperation(RelationalExpression input, P parameters) {
    super(input, parameters);
  }

  /**
   * Applies the encapsulated operation to a single row of the input relation.
   * @param inputRow a row of the input relation.
   * @return the corresponding row of the output relation.
   */
  @Override
  public Row call(Row inputRow) {
    return RowImpl.transform(getOutputSchema(), inputRow);
  }


}
