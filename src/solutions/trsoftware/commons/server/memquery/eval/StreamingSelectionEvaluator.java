package solutions.trsoftware.commons.server.memquery.eval;

import solutions.trsoftware.commons.client.util.Predicate;
import solutions.trsoftware.commons.client.util.iterators.PredicatedIterator;
import solutions.trsoftware.commons.client.util.iterators.TransformingIterator;
import solutions.trsoftware.commons.server.memquery.Relation;
import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.server.memquery.StreamingRelation;
import solutions.trsoftware.commons.server.memquery.algebra.Selection;

/**
 * An evaluator that can be used in a pipeline that doesn't produce any intermediate materialized relations.
 *
 * @author Alex, 1/15/14
 */
public class StreamingSelectionEvaluator extends
    UnaryOperationEvaluator<Selection, Relation, StreamingRelation> {

  public StreamingSelectionEvaluator(Selection op, RelationalEvaluator<Relation> inputEvaluator) {
    super(op, inputEvaluator);
  }

  @Override
  public StreamingRelation call(Relation input) {
    return new StreamingRelation(op.getOutputSchema(),
        new TransformingIterator<Row, Row>(
            new PredicatedIterator<Row>(input.iterator(), new Predicate<Row>() {
              @Override
              public boolean apply(Row item) {
                return op.getParams().call(item);
              }}) {
            }) {
          @Override
          protected Row transform(Row inputRow) {
            return op.call(inputRow);
          }
        }
    );
  }
}
