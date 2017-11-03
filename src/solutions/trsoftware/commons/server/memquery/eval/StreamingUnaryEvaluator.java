package solutions.trsoftware.commons.server.memquery.eval;

import solutions.trsoftware.commons.client.util.iterators.TransformingIterator;
import solutions.trsoftware.commons.server.memquery.Relation;
import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.server.memquery.StreamingRelation;
import solutions.trsoftware.commons.server.memquery.algebra.StreamableUnaryOperation;

/**
 * An evaluator that can be used in a pipeline that doesn't produce any intermediate materialized relations.
 *
 * @author Alex, 1/15/14
 */
public class StreamingUnaryEvaluator extends
    UnaryOperationEvaluator<StreamableUnaryOperation, Relation, StreamingRelation> {

  public StreamingUnaryEvaluator(StreamableUnaryOperation op, RelationalEvaluator<Relation> inputEvaluator) {
    super(op, inputEvaluator);
  }

  @Override
  public StreamingRelation call(Relation input) {
    return new StreamingRelation(op.getOutputSchema(),
        new TransformingIterator<Row, Row>(input.iterator()) {
          @Override
          public Row transform(Row inputRow) {
            return op.call(inputRow);
          }
        });
  }
}
