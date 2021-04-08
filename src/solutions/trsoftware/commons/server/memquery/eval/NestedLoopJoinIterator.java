/*
 * Copyright 2021 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.commons.server.memquery.eval;

import com.google.common.collect.UnmodifiableIterator;
import solutions.trsoftware.commons.server.memquery.MaterializedRelation;
import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.server.memquery.RowFactory;
import solutions.trsoftware.commons.server.memquery.algebra.Join;
import solutions.trsoftware.commons.server.util.Duration;
import solutions.trsoftware.commons.shared.util.iterators.FilteringIterator;
import solutions.trsoftware.commons.shared.util.iterators.SingletonIterator;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;

/**
 * Uses a modified nested loop algorithm for evaluating a join in a streaming fashion (emits an output row as soon
 * as it's available).
 *
 * Requires that the RHS input relation is materialized (because it will be iterated many times),
 * whereas the LHS will be iterated only once.
 *
 * @author Alex, 4/17/2015
 */
abstract class NestedLoopJoinIterator<J extends Join<? extends Join.Params>> extends UnmodifiableIterator<Row> {

  protected final J joinOp;
  protected final Join.Type joinType;
  protected final Iterator<Row> leftIter;
  protected final MaterializedRelation rightInputRelation;
  private Iterator<Row> rightIter;
  protected Row nextLeft;
  private Row nextRight;
  private Row nextResult;
  private LinkedHashSet<Row> matchedRightRows;
  private Iterator<Row> unmatchedRightIter;
  private boolean reachedEnd;  // NOTE: might want to extract a superclass that handles this logic (could replace solutions.trsoftware.commons.shared.util.iterators.AdvancingIterator with this type of implementation)

  // verbose output support:
  protected boolean verbose = true;  // TODO: use this in all nontrivial evaluators and provide a way to set its value
  protected Duration processingDuration;
  protected int rowCount;

  public NestedLoopJoinIterator(J joinOp, Iterator<Row> leftIter, MaterializedRelation rightInputRelation) {
    this.leftIter = leftIter;
    this.joinOp = joinOp;
    this.rightInputRelation = rightInputRelation;
    joinType = joinOp.getParams().getType();
    // for joins that need to return all RHS tuples, we have to keep track of which ones have been returned so we can later add those that never got matched
    if (joinType == Join.Type.RIGHT_OUTER || joinType == Join.Type.FULL_OUTER)
      matchedRightRows = new LinkedHashSet<Row>();
  }

  @Override
  public boolean hasNext() {
    if (reachedEnd)
      return false;
    if (nextResult != null)
      return true;
    computeNext();
    return hasNext(); // try again
  }

  @Override
  public Row next() {
    if (reachedEnd)
      throw new NoSuchElementException();
    if (nextResult != null) {
      Row ret = nextResult;
      nextResult = null;
      return ret;
    }
    computeNext();
    return next();  // try again
  }

  private void computeNext() {
    if (unmatchedRightIter != null) {
      // we have finished emitting all the matches, and now we just have to emit all the unmatched RHS rows
      if (unmatchedRightIter.hasNext())
        nextResult = joinOp.call(RowFactory.getInstance().newRow(joinOp.getLHSchema()), unmatchedRightIter.next());
      else {
        reachedEnd = true;
        unmatchedRightIter = null;
      }
    }
    else {
      // keep advancing the LHS cursor until a match is found on the RHS
      while (advanceLeft()) {
        if (verbose) {
          if (processingDuration == null)
            processingDuration = new Duration(getClass().getSimpleName(), "");
          rowCount++;
          if (rowCount % 100000 == 0)
            System.out.printf("      %s: %,d LHS rows processed, time elapsed: %s %n",
                getClass().getSimpleName(), rowCount, processingDuration.formatAsClockTime(false));
        }

        if (advanceRight()) {
          // found a match
          nextResult = joinOp.call(nextLeft, nextRight);
          // for joins that need to return all RHS tuples, we have to keep track of which ones have been returned so we can later add those that never got matched
          if (matchedRightRows != null)
            // NOTE: although matchedRightRows is a HashSet and RowImpl doesn't implement equals/hashCode, this still works because we only care about identity equality here
            matchedRightRows.add(nextRight);
          return;
        }
      }
      maybeReachedEnd();
    }
  }

  private void maybeReachedEnd() {
    if (matchedRightRows != null && !matchedRightRows.isEmpty()) {
      // now return all the unmatched RHS rows
      unmatchedRightIter = new FilteringIterator<Row>(rightInputRelation.iterator()) {
        @Override
        protected boolean filter(Row elt) {
          return elt != null && !matchedRightRows.contains(elt);
        }
      };
    }
    else {
      reachedEnd = true;
    }
  }

  /**
   * Advances the outer cursor.
   * @return true if success; false if we've reached the end of the input
   */
  private boolean advanceLeft() {
    if (rightIter != null)
      return true;  // we still have RHS inputs to process; it's not time to advance the LHS yet
    if (!leftIter.hasNext())
      return false; // reached the end of input
    nextLeft = leftIter.next();
    return true;
  }

  /**
   * Advances the inner cursor to the next matching row.
   * @return true if success; false if we've reached the end of the input
   */
  private boolean advanceRight() {
    if (rightIter == null) {
      rightIter = getRightMatchesIterator();
      // if there are no matches on the right, and this is left or full outer join, we use a singleton iterator with a row of nulls
      if (!rightIter.hasNext() && (joinType == Join.Type.LEFT_OUTER || joinType == Join.Type.FULL_OUTER)) {
        rightIter = new SingletonIterator<Row>(RowFactory.getInstance().newRow(rightInputRelation.getSchema()));
      }
    }
    if (!rightIter.hasNext()) {
      nextRight = null;
      rightIter = null;
      return false;
    }
    nextRight = rightIter.next();
    return true;
  }

  /**
   * @return an iterator over a subset of the rows in the RHS relation that match the current {@link #nextLeft}
   */
  protected abstract Iterator<Row> getRightMatchesIterator();

}
