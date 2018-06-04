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

package solutions.trsoftware.commons.server.memquery;

/**
 * A sort order defined on a column.
 *
 * @author Alex, 1/12/14
 */
public class SortOrder implements HasName {

  private final String name;
  private final boolean reversed;

  public SortOrder(String name, boolean reversed) {
    this.name = name;
    this.reversed = reversed;
  }

  public String getName() {
    return name;
  }

  public boolean isReversed() {
    return reversed;
  }

  @Override
  public String toString() {
    String ret = name;
    if (reversed)
      ret += " DESC";
    return ret;
  }
}
