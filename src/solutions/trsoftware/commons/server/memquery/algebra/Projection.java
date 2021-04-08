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

package solutions.trsoftware.commons.server.memquery.algebra;

import solutions.trsoftware.commons.server.memquery.schema.ColSpec;
import solutions.trsoftware.commons.server.memquery.schema.NameAccessorColSpec;

import java.util.Collections;
import java.util.List;

/**
 * A simple relational algebra <em>project</em> operation (&pi;): projects a subset of
 * attribute names from the input relation (similar to the {@code SELECT} clause in SQL).
 *
 * @author Alex, 1/14/14
 */
public class Projection extends StreamableUnaryOperation<List<String>> {

  /**
   * The output relation will contain the given attributes from the input relation.
   */
  public Projection(RelationalExpression input, List<String> attrs) {
    super(input, Collections.unmodifiableList(attrs));
  }

  @Override
  protected Iterable<String> getOutputColNames() {
    return this.params;
  }

  @Override
  protected ColSpec createColSpec(String name) {
    return new NameAccessorColSpec(getInputSchema().get(name));
  }

}
