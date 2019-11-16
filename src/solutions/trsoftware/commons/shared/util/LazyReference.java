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

package solutions.trsoftware.commons.shared.util;

import java.util.function.Supplier;

/**
 * A cache for an instance that will be created only once (on the first invocation of {@link #get()}).
 *
 * @param <T> the type of object referred to by this reference
 *
 * @author Alex
 */
public abstract class LazyReference<T> {

  /**
   * The value computed by {@link #create()}.
   * <p style="color: #6495ed; font-weight: bold;">
   *   TODO: perhaps init the value to some dummy object instead of {@code null}, to
   *   disambiguate between a {@code null} value returned by {@link #create()} and the "not initialized" state?
   *   (can also use an {@link java.util.concurrent.atomic.AtomicBoolean} for this purpose).
   * </p>
   */
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

  /**
   * Factory method that uses the given supplier function to implement the {@link #create()} method.
   */
  public static <T> LazyReference<T> fromSupplier(Supplier<T> supplier) {
    return new LazyReference<T>() {
      @Override
      protected T create() {
        return supplier.get();
      }
    };
  }
}