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

import solutions.trsoftware.commons.server.memquery.MemQuery;
import solutions.trsoftware.commons.server.memquery.Relation;
import solutions.trsoftware.commons.server.memquery.algebra.*;

import java.util.Map;

/**
* @author Alex, 10/10/2016
*/
public class EvalPipelineBuilder {  // TODO: rewrite using a proper Visitor pattern
  private MemQuery query;
  private final Map<String, Relation> inputRelations;

  public EvalPipelineBuilder(MemQuery query, Map<String, Relation> inputRelations) {
    this.query = query;
    this.inputRelations = inputRelations;
  }

  public RelationalEvaluator visit(RelationalExpression expr) {
    if (expr instanceof RelationalOperation) {
      RelationalOperation op = (RelationalOperation)expr;
      if (expr instanceof UnaryOperation) {
        UnaryOperation unOp = (UnaryOperation)expr;
        RelationalEvaluator inputEvaluator = visit(unOp.getInput());
        if (op instanceof Selection)
          return new StreamingSelectionEvaluator(((Selection)op), inputEvaluator);
        else if (op instanceof StreamableUnaryOperation) {
          if (op instanceof Rename) {
            // make sure the output schema always has a name
            Rename.Params renameParams = ((Rename)op).getParams();
            if (renameParams.getNewRelationName() == null)
              renameParams.setNewRelationName(String.format("ResultSetOf{%s}", query));
          }
          return new StreamingUnaryEvaluator(((StreamableUnaryOperation)op), inputEvaluator);
        }
        else
          return new AggregationEvaluator((AggregationOperation)op, inputEvaluator);
      }
      else {
        BinaryOperation binExpr = (BinaryOperation)expr;
        return new StreamingJoinEvaluator((Join)op, visit(binExpr.getLHS()), visit(binExpr.getRHS()));
      }
    }
    else {
      return new ValueEvaluator(inputRelations.get(expr.getOutputSchema().getName()));
    }
  }
}
