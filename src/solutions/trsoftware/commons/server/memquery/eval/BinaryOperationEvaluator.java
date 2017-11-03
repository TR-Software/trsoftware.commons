/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.server.memquery.eval;

import solutions.trsoftware.commons.client.util.callables.Function2;
import solutions.trsoftware.commons.server.memquery.Relation;
import solutions.trsoftware.commons.server.memquery.algebra.RelationalExpression;

/**
 * Evaluates a single relational algebra operation on two input relations to produce an output relation.
 *
 * @param <O> the operation type.
 * @param <L> the LHS input relation type.
 * @param <R> the RHS input relation type.
 * @param <T> the output relation type.
 *
 * @author Alex, 1/15/14
 */
public abstract class BinaryOperationEvaluator<O extends RelationalExpression, L extends Relation, R extends Relation, T extends Relation>
    extends OperationEvaluator<O, T> implements Function2<L, R, T> {

  protected final RelationalEvaluator<L> lhsEvaluator;
  protected final RelationalEvaluator<R> rhsEvaluator;

  protected BinaryOperationEvaluator(O op, RelationalEvaluator<L> lhsEvaluator, RelationalEvaluator<R> rhsEvaluator) {
    super(op);
    this.lhsEvaluator = lhsEvaluator;
    this.rhsEvaluator = rhsEvaluator;
  }

  @Override
  public T call() throws Exception {
    return call(lhsEvaluator.call(), rhsEvaluator.call());
  }

  @Override
  public void accept(RelationalEvaluatorVisitor visitor) {
    lhsEvaluator.accept(visitor);
    rhsEvaluator.accept(visitor);
    visitor.visit(this);
  }
}
