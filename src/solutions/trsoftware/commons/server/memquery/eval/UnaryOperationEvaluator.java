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

import solutions.trsoftware.commons.client.util.callables.Function1;
import solutions.trsoftware.commons.server.memquery.Relation;
import solutions.trsoftware.commons.server.memquery.algebra.RelationalExpression;

/**
 * Evaluates a single relational algebra operation on an input relation to produce an output relation.
 *
 * @param <I> the input relation type.
 * @param <R> the output relation type.
 *
 * @author Alex, 1/15/14
 */
public abstract class UnaryOperationEvaluator<O extends RelationalExpression, I extends Relation, R extends Relation>
    extends OperationEvaluator<O, R> implements Function1<I, R> {

  protected final RelationalEvaluator<I> inputEvaluator;

  protected UnaryOperationEvaluator(O op, RelationalEvaluator<I> inputEvaluator) {
    super(op);
    this.inputEvaluator = inputEvaluator;
  }


  @Override
  public R call() throws Exception {
    return call(inputEvaluator.call());
  }

  @Override
  public void accept(RelationalEvaluatorVisitor visitor) {
    inputEvaluator.accept(visitor);
    visitor.visit(this);
  }
}
