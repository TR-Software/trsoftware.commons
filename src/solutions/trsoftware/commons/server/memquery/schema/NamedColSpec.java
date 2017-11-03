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

package solutions.trsoftware.commons.server.memquery.schema;

/**
 * A partially-specified ColSpec (provides just the name of the column, but not its type or value for a row).
 *
 * @author Alex, 1/5/14
 */
public abstract class NamedColSpec<T> extends ColSpec<T> {

  private final String name;

  protected NamedColSpec(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

}
