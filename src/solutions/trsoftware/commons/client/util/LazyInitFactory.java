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

package solutions.trsoftware.commons.client.util;

/**
 * A container (i.e. cache) for an instance that will be created only once
 * on first use.
 *
 * TODO: rename this class (and subclasses) to LazyReference
 *
 * @author Alex
 */
public abstract class LazyInitFactory<T> {
  protected volatile T value;

  public T get(boolean create) {
    if (value == null && create) {
      // lazy init using the double-checked locking paradigm
      synchronized (this) {
        if (value == null)
          value = create();
      }
    }
    return value;
  }

  public T get() {
    return get(true);
  }

  public boolean hasValue() {
    return value != null;
  }

  protected abstract T create();
}