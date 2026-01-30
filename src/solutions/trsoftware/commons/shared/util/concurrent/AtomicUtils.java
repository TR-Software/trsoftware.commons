package solutions.trsoftware.commons.shared.util.concurrent;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;

/**
 * Fills in some of the {@link java.util.concurrent.atomic} functionality that isn't
 * <a href="https://www.gwtproject.org/doc/latest/RefJreEmulation.html#Package_java_util_concurrent_atomic">
 *   emulated by GWT</a>.
 *
 * @author Alex
 * @since 1/18/2023
 */
public abstract class AtomicUtils {

  /**
   * Emulates {@link AtomicReference#updateAndGet(UnaryOperator)}.
   * <p>
   * Atomically updates the reference value with the results of
   * applying the given function, returning the updated value. The
   * function should be side-effect-free, since it may be re-applied
   * when attempted updates fail due to contention among threads.
   *
   * @param updateFunction a side-effect-free function
   * @return the updated value
   */
  public static <V> V updateAndGet(AtomicReference<V> ref, UnaryOperator<V> updateFunction) {
    V prev, next;
    do {
      prev = ref.get();
      next = updateFunction.apply(prev);
    }
    while (!ref.compareAndSet(prev, next));
    return next;
  }

  /**
   * Emulates {@link AtomicReferenceArray#updateAndGet(int, UnaryOperator)}.
   * <p>
   * Atomically updates the element at index {@code i} with the results
   * of applying the given function, returning the updated value. The
   * function should be side-effect-free, since it may be re-applied
   * when attempted updates fail due to contention among threads.
   * <p>
   * <i>Note:</i> this implementation is not as fast as the original, which is able to avoid checking
   * the index on each iteration of the {@code compareAndSet} loop.
   *
   * @param i the index
   * @param updateFunction a side-effect-free function
   * @return the updated value
   */
  public static <E> E updateAndGet(AtomicReferenceArray<E> arr, int i, UnaryOperator<E> updateFunction) {
    E prev, next;
    do {
      prev = arr.get(i);
      next = updateFunction.apply(prev);
    }
    while (!arr.compareAndSet(i, prev, next));
    return next;
  }

  /**
   * Returns {@link AtomicReferenceArray#get(int) arr.get(i)} if non-null,
   * otherwise returns the value computed by the given function after entering it into the array.
   *
   * @param i array index (and arg for producer)
   * @param producer a side-effect-free function that computes the value for {@code arr.get(i)} if it's absent
   * @return the current (existing or computed) value at the specified array index
   */
  public static <E> E computeIfAbsent(AtomicReferenceArray<E> arr, int i, IntFunction<E> producer) {
    E existing = arr.get(i);
    if (existing != null)
      return existing;
    else {
      E newValue = producer.apply(i);
      if (arr.compareAndSet(i, null, newValue)) {
        // this is now the saved value
        return newValue;
      } else {
        // lost the race for compareAndSet, return the value that was computed by the winning thread
        return arr.get(i);
      }
    }
    // TODO: unit test?
  }
}
