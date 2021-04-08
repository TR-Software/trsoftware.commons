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

package solutions.trsoftware.commons.server.memquery.expressions;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Evaluates to true if the value of the given column is one of the given choices.
 *
 * @author Alex, 4/17/2015
 */
public class ColValueIn<T extends Comparable> extends ColValuePredicate<T> {

  private Set<T> choices;

  public ColValueIn(String colName, Collection<T> choices) {
    super(colName);
    this.choices = new LinkedHashSet<T>(choices);
  }

  @Override
  public boolean eval(T value) {
    return choices.contains(value);
  }

  @Override
  public String toString() {
    return colName + " IN " + choices;
  }
}
