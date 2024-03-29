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

import solutions.trsoftware.commons.server.memquery.Row;

/**
 * Delegates all methods to the encapsulated ColSpec.
 *
 * @author Alex, 1/5/14
 */
public class DelegatedColSpec<T> extends ColSpec<T> {

  // TODO: remove this class if it's no longer needed

  private final ColSpec<T> delegate;

  public DelegatedColSpec(ColSpec<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public T getValue(Row row) {
    return delegate.getValue(row);
  }

  @Override
  public Class<T> getType() {
    return delegate.getType();
  }
}
