/*
 * Copyright 2022 TR Software Inc.
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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * A cache for an instance that will be created only once (on the first invocation of {@link #get()}).
 *
 * @param <V> the type of object referred to by this reference
 *
 * @author Alex
 */
public abstract class LazyReference<V> implements Supplier<V> {

  /**
   * The value computed by {@link #create()}.
   */
  protected volatile V value;

  protected final AtomicBoolean hasValue = new AtomicBoolean();

  public V get(boolean create) {
    // TODO: unit test this new lock-free implementation; document these methods
    if (create)
      return get();
    return value;
  }

  public V get() {
    if (hasValue.compareAndSet(false, true)) {
      try {
        return value = create();
      }
      catch (RuntimeException ex) {
        // should revert hasValue back to false if create() throws exception
        hasValue.set(value != null);  // TODO: maybe compareAndSet would be safer?
        throw ex; // rethrow
      }
    }
    return value;
  }

  public boolean hasValue() {
    return hasValue.get();
  }

  protected abstract V create();

  /**
   * Creates a {@link LazyReference} using the given function to implement the {@link #create()} method
   */
  public static <T> LazyReference<T> fromSupplier(Supplier<T> supplier) {
    return new LazyReferenceFromSupplier<>(supplier);
  }

  private static class LazyReferenceFromSupplier<T> extends LazyReference<T> {
    private final Supplier<T> supplier;

    LazyReferenceFromSupplier(Supplier<T> supplier) {
      this.supplier = requireNonNull(supplier, "supplier");
    }

    @Override
    protected T create() {
      return supplier.get();
    }
  }
}