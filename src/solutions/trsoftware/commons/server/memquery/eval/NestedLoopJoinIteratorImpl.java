package solutions.trsoftware.commons.server.memquery.eval;

import solutions.trsoftware.commons.client.util.iterators.FilteringIterator;
import solutions.trsoftware.commons.server.memquery.MaterializedRelation;
import solutions.trsoftware.commons.server.memquery.Relation;
import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.server.memquery.algebra.Join;

import java.util.Iterator;

/**
 * Implements the brute force ("nested loop") algorithm for evaluating a join: for every row on the LHS, performs
 * a linear scan of the RHS to find the matching rows.  This results in O(n^2) time.
 */
class NestedLoopJoinIteratorImpl<J extends Join<? extends Join.Params>> extends NestedLoopJoinIterator<J> {


  NestedLoopJoinIteratorImpl(J joinOp, Relation leftInputRelation, MaterializedRelation rightInputRelation) {
    super(joinOp, leftInputRelation.iterator(), rightInputRelation);
  }

  @Override
  protected Iterator<Row> getRightMatchesIterator() {
    return new FilteringIterator<Row>(rightInputRelation.iterator()) {
      @Override
      protected boolean filter(Row elt) {
        return elt != null && joinOp.match(nextLeft, elt);
      }
    };

  }

}
