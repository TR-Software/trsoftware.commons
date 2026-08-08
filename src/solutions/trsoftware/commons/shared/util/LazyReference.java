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

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * A cache for an instance that will be created only once (on the first invocation of {@link #get()}).
 *
 * @param <V> the type of object referred to by this reference
 *
 * @author Alex
 */
@SuppressWarnings("unchecked")
public abstract class LazyReference<V> implements Supplier<V> {

  /**
   * Placeholder representing uninitialized value
   */
  private static final Object EMPTY = new Object() {
    // overriding hashCode and toString, following the example of java.util.EnumMap.NULL
    public int hashCode() { return 0; }
    public String toString() { return "LazyReference.EMPTY"; }
  };

  /**
   * The value computed by {@link #create()}.
   */
  private final AtomicReference<V> ref = new AtomicReference<>((V)EMPTY);

  @Nullable
  public V get(boolean create) {
    if (create)
      return get();
    V value = ref.get();
    // return null if current value is EMPTY (avoid leaking this placeholder object)
    return value != EMPTY ? value : null;
  }

  public V get() {
    // TODO: code dup in AtomicUtils.computeIfAbsent(AtomicReference<V>, Supplier<V>)
    V value = ref.get();
    if (value != EMPTY)
      return value;
    else {
      V newValue = create();
      if (ref.compareAndSet((V)EMPTY, newValue)) {
        // this is now the saved value
        return newValue;
      } else {
        // lost the race for compareAndSet, return the value that was computed by the winning thread
        return ref.get();
      }
    }
  }

  protected void set(V newValue) {  // protected access, to be exposed only by MutableLazyReference
    ref.set(newValue);
  }

  protected void clear() {  // protected access, to be exposed only by MutableLazyReference
    ref.set((V)EMPTY);
  }

  public boolean hasValue() {
    return ref.get() != EMPTY;
  }

  protected abstract V create();

  /**
   * If a value {@linkplain #hasValue() is present}, invoke the specified consumer with the value and return {@code true},
   * otherwise do nothing and return {@code false}.
   *
   * @param consumer block to be executed if a value is present
   * @return {@code true} if consumer was invoked; {@code false} if no value is present
   *
   * @throws NullPointerException if value is present but {@code consumer} is null
   * @see #hasValue()
   * @see java.util.Optional#ifPresent(Consumer)
   * @see #map(Function)
   */
  public boolean ifPresent(Consumer<? super V> consumer) {
    // TODO(7/29/2026): experimental
    V value = ref.get();
    if (value != EMPTY) {
      requireNonNull(consumer, "consumer").accept(value);
      return true;
    }
    return false;
  }

  /**
   * If a value is present, apply the provided mapping function to it,
   * and if the result is non-null, return an {@link Optional} describing the
   * result.  Otherwise return an empty {@link Optional}.
   *
   * @param <U> The type of the result of the mapping function
   * @param mapper a mapping function to apply to the value, if present
   * @return an {@link Optional} describing the result of applying the mapping
   * function to the value of this {@link LazyReference}, if a value is present,
   * otherwise an empty {@link Optional}
   * @throws NullPointerException if value is present but {@code mapper} is null
   * @see #ifPresent(Consumer)
   * @see #flatMap(Function)
   */
  public <U> Optional<U> map(Function<? super V, ? extends U> mapper) {
    // TODO(7/29/2026): experimental
    V value = ref.get();
    if (value != EMPTY) {
      requireNonNull(mapper, "mapper");
      return Optional.ofNullable(mapper.apply(value));
    }
    return Optional.empty();
  }

  /**
   * If a value is present, apply the provided {@code Optional}-bearing
   * mapping function to it, return that result, otherwise return an empty
   * {@code Optional}.  This method is similar to {@link #map(Function)},
   * but the provided mapper is one whose result is already an {@code Optional},
   * and if invoked, {@code flatMap} does not wrap it with an additional
   * {@code Optional}.
   *
   * @param <U> The type parameter to the {@code Optional} returned by
   * @param mapper a mapping function to apply to the value, if present
   * @return the result of applying the {@code Optional}-bearing mapping
   * function to the value of this {@link LazyReference}, if a value is present,
   * otherwise an empty {@code Optional}
   * @throws NullPointerException if value is present but {@code mapper} is null
   * @see #ifPresent(Consumer)
   * @see #map(Function)
   */
  public <U> Optional<U> flatMap(Function<? super V, Optional<U>> mapper) {
    // TODO(7/29/2026): experimental
    V value = ref.get();
    if (value != EMPTY) {
      requireNonNull(mapper, "mapper");
      return mapper.apply(value);
    }
    return Optional.empty();
  }

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