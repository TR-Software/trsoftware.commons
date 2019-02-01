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

package solutions.trsoftware.commons.server.memquery.schema;

import solutions.trsoftware.commons.server.memquery.Row;

/**
 * @author Alex, 6/2/2014
 */
public class RenamedColSpec<T> extends NameAccessorColSpec<T> {  // TODO: get rid of this class

  private final ColSpec<T> delegate;

  @SuppressWarnings("unchecked")
  public RenamedColSpec(String newName, ColSpec delegate) {
    super(newName, delegate.getType());
    this.delegate = delegate;
  }

  @Override
  protected Object doGetValue(Row row) {
    return row.getValue(delegate.getName());
  }
}
