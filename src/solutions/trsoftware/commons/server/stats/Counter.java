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

package solutions.trsoftware.commons.server.stats;

import solutions.trsoftware.commons.shared.util.JsonBuilder;

/**
 * A named counter data type.
 *
 * @author Alex
 */
public abstract class Counter {
  protected final String name;

  public Counter(String name) {
    this.name = name;
  }

  /** @return The name of the counter. */
  public String getName() {
    return name;
  }

  /** Increments the counter */
  public final void incr() {
    add(1);
  }

  /** Decrements the counter */
  public final void decr() {
    add(-1);
  }

  /** Adds the given value to the counter */
  public abstract void add(int delta);

  /** @return The value of the counter. */
  public abstract int getCount();

  @Override
  public String toString() {
    return new JsonBuilder().beginObject().key(name).value(getCount()).endObject().toString();
  }

}
