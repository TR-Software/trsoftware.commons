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

package solutions.trsoftware.commons.server.memquery.schema;

import solutions.trsoftware.commons.server.memquery.HasName;
import solutions.trsoftware.commons.server.memquery.HasType;
import solutions.trsoftware.commons.server.memquery.ValueAccessor;

/**
 * A schema for a column in a table: has a name and a type, and is able to access its value for any row in the table.
 *
 * @author Alex, 1/5/14
 */
public abstract class ColSpec<T> implements ValueAccessor<T>, HasName, HasType<T> {


  @Override
  public String toString() {
    return String.format("('%s', %s)", getName(), getType().getSimpleName());
  }
}
