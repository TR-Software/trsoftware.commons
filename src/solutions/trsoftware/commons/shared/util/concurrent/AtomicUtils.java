package solutions.trsoftware.commons.shared.util.concurrent;

import java.util.concurrent.atomic.AtomicReference;
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
   * Same as {@link AtomicReference#updateAndGet(UnaryOperator)}.
   * <p>
   * Atomically updates the reference value with the results of
   * applying the given function, returning the updated value. The
   * function should be side-effect-free, since it may be re-applied
   * when attempted updates fail due to contention among threads.
   *
   * @param updateFunction a side-effect-free function
   * @return the updated value
   * @since 1.8
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
}
