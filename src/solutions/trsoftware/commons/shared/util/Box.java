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

package solutions.trsoftware.commons.shared.util;

/**
 * A container for a value, useful for inside closures, where all references
 * must be final.
 *
 * @author Alex
 */
public class Box<V> implements TakesValue<V> {
  private V value;
  private boolean initialized;

  public Box() {
  }

  public Box(V value) {
    setValue(value);
  }

  public V getValue() {
    return value;
  }

  public void setValue(V value) {
    this.value = value;
    initialized = true;
  }

  /**
   * Sets a new value and returns the old one.
   *
   * @param value the new value
   * @return the old {@link #value}
   */
  public V getAndSet(V value) {
    V oldValue = this.value;
    setValue(value);
    return oldValue;
  }

  /**
   * @return {@code true} iff the value has ever been set (even if it's {@code null})
   */
  public boolean hasValue() {
    return initialized;
  }
}
