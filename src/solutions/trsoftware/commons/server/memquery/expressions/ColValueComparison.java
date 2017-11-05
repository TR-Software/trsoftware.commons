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

package solutions.trsoftware.commons.server.memquery.expressions;

import solutions.trsoftware.commons.shared.util.ComparisonOperator;

/**
 * @author Alex, 4/17/2015
 */
public class ColValueComparison<T extends Comparable> extends ColValuePredicate<T> {

  private final ComparisonOperator op;
  private final T operand;

  public ColValueComparison(String colName, ComparisonOperator op, T operand) {
    super(colName);
    this.op = op;
    this.operand = operand;
  }

  @Override
  public boolean eval(T value) {
    return op.compare(value, operand);
  }

  @Override
  public String toString() {
    return colName + ' ' + op + ' ' + operand;
  }
}
