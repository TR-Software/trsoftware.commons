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

import solutions.trsoftware.commons.client.util.StringUtils;
import solutions.trsoftware.commons.server.memquery.algebra.BinaryOperation;
import solutions.trsoftware.commons.server.memquery.algebra.RelationalExpressionVisitor;
import solutions.trsoftware.commons.server.memquery.algebra.RelationalValue;
import solutions.trsoftware.commons.server.memquery.algebra.UnaryOperation;

/**
* @author Alex, 10/10/2016
*/
public class EvalPipelinePrinter implements RelationalEvaluatorVisitor, RelationalExpressionVisitor {

  private int indentLevel;

  public EvalPipelinePrinter(int indentLevel) {
    this.indentLevel = indentLevel;
  }

  private String indent() {
    return StringUtils.repeat(' ', indentLevel*2);
  }

  // methods inherited from RelationalEvaluatorVisitor

  @Override
  public void visit(ValueEvaluator evaluator) {
    indentLevel++;
    {
      System.out.printf("%s%s(%s)%n", indent(), evaluator.getClass().getSimpleName(), evaluator.getValue());
    }
    indentLevel--;
  }

  @Override
  public void visit(BinaryOperationEvaluator evaluator) {
    indentLevel++;
    {
      System.out.printf("%s%s(%n", indent(), evaluator.getClass().getSimpleName());
      evaluator.op.accept(this);
      System.out.printf("%s)%n", indent());
    }
    indentLevel--;
  }

  @Override
  public void visit(UnaryOperationEvaluator evaluator) {
    indentLevel++;
    {
      System.out.printf("%s%s(%n", indent(), evaluator.getClass().getSimpleName());
      evaluator.op.accept(this);
      System.out.printf("%s)%n", indent());
    }
    indentLevel--;
  }

  // methods inherited from RelationalExpressionVisitor

  @Override
  public void visit(RelationalValue val) {
    indentLevel++;
    {
      System.out.printf("%s%s%n", indent(), val);
    }
    indentLevel--;
  }

  @Override
  public void visit(BinaryOperation op) {
    indentLevel++;
    {
      System.out.printf("%s%s%n", indent(), op);
    }
    indentLevel--;
  }

  @Override
  public void visit(UnaryOperation op) {
    indentLevel++;
    {
      System.out.printf("%s%s%n", indent(), op);
    }
    indentLevel--;
  }
}
