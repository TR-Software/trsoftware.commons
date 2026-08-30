package solutions.trsoftware.commons.server.util;

import com.google.gwt.core.shared.GwtIncompatible;
import solutions.trsoftware.commons.shared.util.function.ThrowingFunction;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * A thread-safe pool of cached instances that can be used to provide thread-safety or greater speed to
 * operations that are either not thread-safe or slow if invoked concurrently on the same instance of {@link T}.
 * <p>
 * The pool grows as needed, depending on thread contention, ultimately reaching a {@linkplain #size() size} equal
 * to the highest number of threads concurrently operating on it.
 * <p>
 * The {@link #apply(Function)} method is provided for performing a computation using the next available
 * worker instance from the pool.
 *
 * @author Alex
 * @since 8/17/2026
 */
@GwtIncompatible // ConcurrentLinkedQueue
public class ResourcePool<T> {
  // idea borrowed from the SecureRandom pool in org.apache.catalina.util.SessionIdGeneratorBase.randoms

  protected final Queue<T> pool = new ConcurrentLinkedQueue<>();

  private final Supplier<T> newInstanceSupplier;


  /**
   * @param newInstanceSupplier creates a new worker instance when the pool needs to grow
   */
  public ResourcePool(Supplier<T> newInstanceSupplier) {
    this.newInstanceSupplier = requireNonNull(newInstanceSupplier, "newInstanceSupplier");
  }

  /**
   * Returns the next available worker instance from the pool. If no instances are available,
   * creates a new worker, which will later be added to the pool by {@link #checkIn(Object)}.
   * The caller must ensure that the reference is not retained past the operation being performed on the instance.
   * <p>
   * <em>Caution:</em> directly using the {@link #checkOut()} / {@link #checkIn(Object)} methods is highly discouraged
   * for any use-case that can be satisfied by {@link #apply(Function)}, because the functionality of this class
   * could break if the same worker instance is {@linkplain #checkIn(Object) checked in} more than once.
   *
   * @return the next available worker from the pool or a new instance if no workers are available
   */
  public T checkOut() {
    // TODO: temp public; this leaks internal data and could be unsafe (see comment for checkIn)
    T next = pool.poll();
    if (next == null)
      next = newInstance();
    return next;
  }

  /**
   * Adds a {@linkplain #checkOut() checked-out} instance back into to the pool, making it available for future operations.
   * The caller must ensure that the same instance is never added more than once.
   * <p>
   * <em>Caution:</em> directly using the {@link #checkOut()} / {@link #checkIn(Object)} methods is highly discouraged
   * for any use-case that can be satisfied by {@link #apply(Function)}, because the functionality of this class
   * could break if the same worker instance is {@linkplain #checkIn(Object) checked in} more than once.
   */
  public void checkIn(T instance) {
    /* TODO: temp public; warn that this could be unsafe (i.e. must be careful not to re-add same instance multiple times)
         - could have a public version of this method that uses a ConcurrentHashMap.newKeySet() to reject duplicates
         - maybe just make these methods protected and create a subclass that does the above checks
     */
    pool.offer(instance);
  }

  /**
   * Invokes the given function on the next available worker instance from the pool.
   * If no instances are available, creates a new worker and adds it to the pool after applying the function.
   *
   * @param func should be stateless and must not retain a reference to the given worker instance
   * @param <R> the function result type
   * @return the result of applying the given function
   */
  public <R> R apply(Function<T, R> func) {
    T next = checkOut();
    R ret = func.apply(next);
    checkIn(next);  // done with this instance, return it to the pool
    return ret;
  }

  /**
   * Invokes the given function on the next available worker instance from the pool.
   * If no instances are available, creates a new worker and adds it to the pool after applying the function.
   *
   * @param func should be stateless and must not retain a reference to the given worker instance
   * @param <R> the function result type
   * @param <E> checked exception that could be thrown by the function
   * @return the result of applying the given function
   * @throws E if {@link ThrowingFunction#applyThrowing(Object)} threw an exception
   */
  public <R, E extends Exception> R apply(ThrowingFunction<T, R, E> func) throws E {
    T next = checkOut();
    R ret = func.applyThrowing(next);
    checkIn(next);  // done with this instance, return it to the pool
    return ret;
  }

  protected T newInstance() {
    return newInstanceSupplier.get();
  }

  /**
   * @return the number of {@link T} instances currently contained by the pool
   * @see ConcurrentLinkedQueue#size()
   */
  public int size() {
    // TODO: maybe instead of slow pool.size() call, keep a local count of instances created by newInstance
    return pool.size();
  }
}
