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

import solutions.trsoftware.commons.server.memquery.Row;

/**
 * @author Alex, 4/17/2015
 */
public abstract class ColValuePredicate<T> extends RowPredicate {

  protected final String colName;

  protected ColValuePredicate(String colName) {
    this.colName = colName;
  }

  @Override
  public final Boolean call(Row arg) {
    return eval((T)arg.getValue(colName));
  }

  public abstract boolean eval(T value);


  /** Factory method */
  public static IsNull isNull(String colName) {
    return new IsNull(colName);
  }

  /** Factory method */
  public static IsNotNull isNotNull(String colName) {
    return new IsNotNull(colName);
  }
}
