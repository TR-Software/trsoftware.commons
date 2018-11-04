/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.server.memquery.eval;

import solutions.trsoftware.commons.server.memquery.Relation;

/**
 * Simply returns the relation that was passed to its constructor.
 *
 * @author Alex, 1/16/14
 */
public class ValueEvaluator<R extends Relation> implements RelationalEvaluator<R> {

  private R value;

  public ValueEvaluator(R value) {
    this.value = value;
  }

  public R getValue() {
    return value;
  }

  @Override
  public R call() throws Exception {
    return value;
  }

  @Override
  public void accept(RelationalEvaluatorVisitor visitor) {
    visitor.visit(this);
  }
}
