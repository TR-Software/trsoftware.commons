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

import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.gwt.thirdparty.guava.common.annotations.VisibleForTesting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import static java.util.Objects.requireNonNull;

/**
 * Stores a value that will be periodically refreshed after a certain expiration period.
 * Unless an initial value is provided to the constructor it will be {@linkplain ValueLoader#load() loaded}
 * synchronously using the provided {@link ValueLoader} the first time it's accessed.  The refreshing can be
 * performed asynchronously, at the discretion of the provided {@link ValueLoader} implementation.
 * <p>
 * <em>Note:</em> Although this implementation is thread-safe and designed to provide a high degree of concurrency,
 * the {@link #get()}, {@link #set(Object)}, {@link #remove()}, and {@link #refresh()} operations are potentially blocking.
 *
 * @see LazyReference
 * @see LoadingCache
 * @author Alex
 * @since 10/3/2022
 */
public class CachedValue<V> {

  // cached value state:
  private volatile V value;
  private volatile long lastUpdated;
  private volatile ValueReloader futureValue;

  // config settings:
  /**
   * How long to keep returning the cached values before triggering a {@link ValueLoader#reload(Object)} operation
   */
  private final long maxAgeMillis;
  /**
   * How long to wait for a future result from a {@link ValueLoader#reload(Object)} operation
   * before invoking {@link ValueLoader#onReloadTimeout}
   */
  private final long reloadTimeoutMillis;

  /**
   * The loader implementation responsible for {@linkplain ValueLoader#load() loading} and
   * {@linkplain ValueLoader#reload(Object) reloading} the cached value
   */
  private final ValueLoader<V> valueLoader;


  /**
   * Creates an empty {@link CachedValue} instance.  The value will be initialized on the first invocation of
   * {@link #get()} using the provided loader.
   *
   * @param maxAgeMillis how long to keep returning the cached values before triggering a
   *     {@linkplain ValueLoader#reload(Object) reload}
   * @param valueLoader The loader implementation responsible for {@linkplain ValueLoader#load() loading} and
   *     {@linkplain ValueLoader#reload(Object) reloading} the cached values
   * @see #CachedValue(long, long, ValueLoader)
   */
  public CachedValue(long maxAgeMillis, @Nonnull ValueLoader<V> valueLoader) {
    this(maxAgeMillis, maxAgeMillis, valueLoader);
  }

  /**
   * Creates an empty {@link CachedValue} instance.  The value will be initialized on the first invocation of
   * {@link #get()} using the provided loader.
   *
   * @param maxAgeMillis how long to keep returning the cached values before triggering a
   *     {@linkplain ValueLoader#reload(Object) reload}
   * @param reloadTimeoutMillis how long to wait for a future result from a {@link ValueLoader#reload(Object)} operation
   *     before invoking {@link ValueLoader#onReloadTimeout}
   * @param valueLoader The loader implementation responsible for {@linkplain ValueLoader#load() loading} and
   *     {@linkplain ValueLoader#reload(Object) reloading} the cached values
   */
  public CachedValue(long maxAgeMillis, long reloadTimeoutMillis, @Nonnull ValueLoader<V> valueLoader) {
    this.maxAgeMillis = maxAgeMillis;
    this.valueLoader = requireNonNull(valueLoader, "valueLoader");
    this.reloadTimeoutMillis = reloadTimeoutMillis;
  }

  /**
   * Creates a {@link CachedValue} instance containing the provided value and a {@link #reloadTimeoutMillis}
   * equal to {@link #maxAgeMillis}.
   *
   * @param initialValue the value to be returned by {@link #get()} until it expires
   * @param maxAgeMillis how long to keep returning the cached values before triggering a
   *     {@linkplain ValueLoader#reload(Object) reload}
   * @param valueLoader The loader implementation responsible for {@linkplain ValueLoader#load() loading} and
   *     {@linkplain ValueLoader#reload(Object) reloading} the cached values
   * @see #CachedValue(Object, long, long, ValueLoader)
   */
  public CachedValue(V initialValue, long maxAgeMillis, @Nonnull ValueLoader<V> valueLoader) {
    this(initialValue, maxAgeMillis, maxAgeMillis, valueLoader);
  }

  /**
   * Creates a {@link CachedValue} instance containing the provided value.
   *
   * @param initialValue the value to be returned by {@link #get()} until it expires
   * @param maxAgeMillis how long to keep returning the cached values before triggering a
   *     {@linkplain ValueLoader#reload(Object) reload}
   * @param reloadTimeoutMillis how long to wait for a future result from a {@link ValueLoader#reload(Object)} operation
   *     before invoking {@link ValueLoader#onReloadTimeout}
   * @param valueLoader The loader implementation responsible for {@linkplain ValueLoader#load() loading} and
   *     {@linkplain ValueLoader#reload(Object) reloading} the cached values
   */
  public CachedValue(V initialValue, long maxAgeMillis, long reloadTimeoutMillis, @Nonnull ValueLoader<V> valueLoader) {
    this.maxAgeMillis = maxAgeMillis;
    this.valueLoader = requireNonNull(valueLoader, "valueLoader");
    this.reloadTimeoutMillis = reloadTimeoutMillis;
    value = Objects.requireNonNull(initialValue, "initialValue");
    lastUpdated = currentTimeMillis();
  }

  /**
   * Returns the cached value, potentially obtaining it from {@link ValueLoader#load()} or
   * a pending {@link ValueLoader#reload} operation.
   */
  @Nonnull
  public V get() {
    if (!hasValue()) {
      // if the value hasn't been set yet, retrieve it now, blocking on the result
      synchronized (this) {
        if (!hasValue()) {
          return set(valueLoader.load());
        }
        // the loader must've been invoked from a competing thread, since we have a value now
        assert value != null;
        return value;
      }
    }
    else {  // has value
      V ret = value;
      assert ret != null;
      // see if a fresher value is available
      // TODO: doing this check every time potentially degrades perf; maybe better to use a ListenableFuture?
      if (newValueAvailable()) {
        V newValue = getFuture();
        // Note: the getFuture() call might have set the value to null (if the future failed and the loader chose "remove" from onReloadFailure)
        if (newValue != null) {
          ret = newValue;
          // instead of returning null, we return the old value for now; next read will call valueLoader.load() to get a new value
        }
      }
      // handle reloading the value (if expired)
      maybeReloadValue();
      return ret;
    }
  }

  private void maybeReloadValue() {
    // TODO: maybe move the expired future check to the getFuture() method?
    if (isExpired() && (futureValue == null || futureValue.isExpired())) {
      synchronized (this) {
        if (isExpired())
          if (futureValue == null) {
            futureValue = new ValueReloader(valueLoader.reload(value));
          }
          else if (futureValue.isExpired()) {
            handleExpiredFuture();
          }
      }
    }
  }

  private boolean newValueAvailable() {
    return futureValue != null && futureValue.isDone();
  }

  public boolean hasValue() {
    return value != null;
  }


  /**
   * @return {@code true} iff the currently-cached value has {@linkplain #maxAgeMillis expired}
   */
  public boolean isExpired() {
    return hasValue() && getAge() > maxAgeMillis;
  }

  /**
   * @return the time elapsed (in millis) since the cached valued was created or updated; 0 if there's no value present
   */
  public long getAge() {
    return hasValue() ? elapsedMillis(lastUpdated) : 0;
  }

  public long getMaxAgeMillis() {
    return maxAgeMillis;
  }

  public long getReloadTimeoutMillis() {
    return reloadTimeoutMillis;
  }

  public ValueLoader<V> getValueLoader() {
    return valueLoader;
  }

  private long elapsedMillis(long since) {
    return currentTimeMillis() - since;
  }

  // TODO: maybe move the getFuture / handle[Failed|Expired]Future methods into ValueReloader ?

  private synchronized V getFuture() {
    /*
    NOTE: if user changed the value (by calling set(V)) before the future is done,
      the future would've been cancelled by the set(V) call,
      so there's not much risk of the future overwriting a fresher value
    */
    if (newValueAvailable()) {
      try {
        V newValue = Futures.getDone(futureValue.future);
        futureValue = null;
        Objects.requireNonNull(newValue, () -> "Value returned from future " + futureValue.future);
        return set(newValue);
      } catch (ExecutionException e) {
        handleFailedFuture(e.getCause());
      } catch (RuntimeException | Error e) {
        handleFailedFuture(e);
      }
    }
    return value;  // fall back to the existing value (which might have been set to null by handleFailedFuture)
  }

  private synchronized void handleFailedFuture(Throwable cause) {
    ValueLoader.FailedReloadAction<V> onFail = valueLoader.onReloadFailure(cause, getAge(), ++futureValue.failCount);
    requireNonNull(onFail, "onReloadFailure must return an instance of FailedReloadAction");
    finishFailedReload(onFail);
  }

  private synchronized void handleExpiredFuture() {
    ValueLoader.FailedReloadAction<V> onFail = valueLoader.onReloadTimeout(getAge(), ++futureValue.failCount);
    requireNonNull(onFail, "onReloadTimeout must return an instance of FailedReloadAction");
    finishFailedReload(onFail);
  }

  private void finishFailedReload(ValueLoader.FailedReloadAction<V> action) {
    switch (action.action) {
      case IGNORE:
        // mark the current value as "fresh" instead of retrying the reload
        lastUpdated = currentTimeMillis();
        futureValue = null;
        break;
      case RETRY:
        assert futureValue != null;
        futureValue.setFuture(requireNonNull(action.retryFuture, "retryFuture"));
        break;
      case REMOVE:
        remove();
        break;
    }
  }

  /**
   * Sets a new value.
   * @return the new value
   */
  public synchronized V set(@Nonnull V newValue) {
    value = Objects.requireNonNull(newValue, "newValue");
    lastUpdated = currentTimeMillis();
    clearFuture();
    return newValue;
  }

  private synchronized void clearFuture() {
    if (futureValue != null && !futureValue.isDone()) {
      futureValue.cancel();
    }
    futureValue = null;
  }

  /**
   * Removes the cached value, if any.
   * It will be loaded again on the next {@link #get()} call via {@link ValueLoader#load()}
   *
   * @return the removed value ({@code null} if not set)
   */
  public synchronized V remove() {
    V ret = value;
    value = null;
    lastUpdated = 0;
    clearFuture();
    return ret;
  }

  /**
   * Triggers a (potentially asynchronous) {@link ValueLoader#reload(Object) reload} operation,
   * even if the cached value is not yet expired.
   * @return {@code true} if the reload was started or {@code false} if already waiting on a pending reload
   * or there's no current value
   */
  public boolean refresh() {
    if (hasValue() && futureValue == null) {
      synchronized (this) {
        if (hasValue() && futureValue == null) {
          futureValue = new ValueReloader(valueLoader.reload(value));
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns {@link System#currentTimeMillis()}, but unit tests can override to provide a mocked time.
   */
  @VisibleForTesting
  protected long currentTimeMillis() {
    return System.currentTimeMillis();
  }

  /**
   * Helper for managing {@link ValueLoader#reload(Object)} operations.
   */
  private class ValueReloader {
    private volatile Future<V> future;
    private volatile long futureStarted;
    /**
     * The clock time (epoch millis) when the current {@linkplain ValueLoader#reload(Object) reload} operation began.
     */
    private final long startTime;
    /**
     * The number of failed attempts (so far) to {@linkplain ValueLoader#reload(Object) reload} the cached value.
     * <p>
     * This counter is incremented for every invocation of {@link ValueLoader#onReloadFailure}
     * and {@link ValueLoader#onReloadTimeout}
     */
    private volatile int failCount;

    private ValueReloader(@Nonnull Future<V> future) {
      startTime = currentTimeMillis();
      setFuture(future);
    }

    long getStartTime() {
      return startTime;
    }

    int getFailCount() {
      return failCount;
    }

    private void setFuture(@Nonnull Future<V> future) {
      this.future = future;
      futureStarted = currentTimeMillis();
    }

    boolean isExpired() {
      return elapsedMillis(futureStarted) > reloadTimeoutMillis;
    }

    boolean cancel() {
      return future.cancel(false);
    }

    boolean isDone() {
      return future.isDone();
    }
  }

  /**
   * Computes or retrieves the values to be stored in a {@link CachedValue}.
   * <p>
   * The {@link #load()} method will be used to provide the initial value and
   * {@link #reload(Object)} will be used to provide a new value after it {@linkplain #getMaxAgeMillis() expires}.
   * <p>
   * The other methods ({@link #onReloadFailure} and {@link #onReloadTimeout}) are used to specify the desired action
   * if the {@link Future} returned by {@link #reload(Object)} failed or didn't finish {@linkplain #getReloadTimeoutMillis() in time}.
   *
   * @see SimpleValueLoader
   * @see com.google.common.cache.CacheLoader
   */
  public interface ValueLoader<V> {

    /**
     * Computes or retrieves the initial value to be stored in a {@link CachedValue}.
     * <p>
     * This method will be invoked on the first {@link #get()} (unless an initial value was provided to the
     * {@link CachedValue} constructor), and on any subsequent {@link #get()} operations following a
     * {@linkplain #remove() removal}.
     *
     * @return the new value (must not be null)
     */
    @Nonnull V load();

    /**
     * Computes or retrieves a replacement value for a {@link CachedValue} after it has expired.
     * This operation is designed to be performed asynchronously using an {@link Executor}, but
     * can also be performed immediately by delegating to {@link #load()} and wrapping its result with
     * {@link Futures#immediateFuture(Object)}.
     *
     * @param oldValue the expired value
     * @return the future value (<em>must not be null, must not return null</em>)
     */
    @Nonnull Future<V> reload(V oldValue);

    /**
     * Invoked if any {@link Future} future returned by this loader (e.g. from {@link #reload(Object)}) threw an exception.
     * <p>
     * The implementation can choose to {@linkplain FailedReloadAction.Action#RETRY retry} the computation
     * (by providing a new {@link Future}), {@linkplain FailedReloadAction.Action#REMOVE remove} the expired value,
     * or {@linkplain FailedReloadAction.Action#IGNORE ignore} the failure (thereby keeping the expired value and
     * marking it fresh).
     * <p>
     * The arguments passed to this method can be used to decide whether or not to retry reloading the cached value.
     *
     * @param cause the exception thrown by {@link Future#get()}
     * @param valueAge the time elapsed (in millis) since the cached valued was created or successfully updated
     * @param failCount the number of failed reload attempts since the original failed invocation of {@link #reload(Object)};
     *   this counter is incremented for every invocation of {@link #onReloadFailure} and {@link #onReloadTimeout}
     *   that chooses to {@linkplain FailedReloadAction.Action#RETRY retry} the failed operation.
     * @return action to take in response to the failure
     * 
     * @see FailedReloadAction#retry(Future)
     * @see FailedReloadAction#remove()
     * @see FailedReloadAction#ignore()
     */
    @Nonnull FailedReloadAction<V> onReloadFailure(Throwable cause, long valueAge, int failCount);

    /**
     * Invoked if any {@link Future} future returned by this loader (e.g. from {@link #reload(Object)})
     * did not complete within {@link #reloadTimeoutMillis}.
     * <p>
     * The semantics and purpose of this method is essentially the same as {@link #onReloadFailure}.
     *
     * @param valueAge the time elapsed (in millis) since the cached valued was created or successfully updated
     * @param failCount the number of failed reload attempts since the original failed invocation of {@link #reload(Object)};
     *   this counter is incremented for every invocation of {@link #onReloadFailure} and {@link #onReloadTimeout}
     *   that chooses to {@linkplain FailedReloadAction.Action#RETRY retry} the failed operation.
     * @return action to take in response to the timout
     *
     * @see FailedReloadAction#retry(Future)
     * @see FailedReloadAction#remove()
     * @see FailedReloadAction#ignore()
     */
    @Nonnull FailedReloadAction<V> onReloadTimeout(long valueAge, int failCount);

    /**
     * Specifies the approach for handing a {@linkplain #reload reload} failure.
     *
     * @see #onReloadFailure(Throwable, long, int)
     * @see #onReloadTimeout(long, int)
     */
    class FailedReloadAction<V> {
      public enum Action {
        /**
         * Will retry reloading the value using the provided future as soon as possible.
         * In the meantime, {@link #get()} will keep returning the current value.
         */
        RETRY,
        /**
         * Will not retry reloading the value.  It will be marked as "fresh" and will not attempt reloading until the
         * next time it expires.  In the meantime, {@link CachedValue#get()} will keep returning the current value.
         */
        IGNORE,
        /**
         * Will remove the cached value.  It will be loaded on the next {@link #get()} operation.
         * This is equivalent to calling {@link CachedValue#remove()}.
         */
        REMOVE;
      }
      private final Action action;
      private final Future<V> retryFuture;

      /**
       * Private constructor: use one of the provided factory methods instead
       * ({@link #retry(Future)}, {@link #ignore()}, {@link #remove()})
       */
      private FailedReloadAction(@Nonnull Action action, @Nonnull Future<V> retryFuture) {
        this.action = requireNonNull(action, "action");
        this.retryFuture = requireNonNull(retryFuture, "retryFuture");
      }

      /**
       * Private constructor: use one of the provided factory methods instead
       * ({@link #retry(Future)}, {@link #ignore()}, {@link #remove()})
       */
      private FailedReloadAction(@Nonnull Action action) {
        this.action = requireNonNull(action, "action");
        retryFuture = null;
      }

      @Nonnull
      public Action getAction() {
        return action;
      }

      @Nullable
      public Future<V> getRetryFuture() {
        return retryFuture;
      }

      /**
       * Retry the {@linkplain #reload(Object) reloading} the cached value using the provided future.
       * @see ValueLoader#onReloadFailure(Throwable, long, int)
       * @see ValueLoader#onReloadTimeout(long, int)
       * @return an object that can be returned from {@link ValueLoader#onReloadFailure(Throwable, long, int)}
       * and {@link ValueLoader#onReloadTimeout(long, int)} to indicate that the failed or timed-out 
       * {@link #reload} operation should be retried as soon as possible
       */
      public static <V> FailedReloadAction<V> retry(Future<V> future) {
        return new FailedReloadAction<>(Action.RETRY, requireNonNull(future, "future"));
      }

      /**
       * See {@link Action#IGNORE Action.IGNORE}
       * @see ValueLoader#onReloadFailure(Throwable, long, int)
       * @see ValueLoader#onReloadTimeout(long, int)
       * @return an object that can be returned from {@link ValueLoader#onReloadFailure(Throwable, long, int)}
       * and {@link ValueLoader#onReloadTimeout(long, int)} to indicate that the failed or timed-out 
       * {@link #reload} operation should not be retried and the cached value considered "fresh" until it
       * expires again
       */
      public static <V> FailedReloadAction<V> ignore() {
        return new FailedReloadAction<>(Action.IGNORE);
      }
      
      /**
       * See {@link Action#REMOVE Action.REMOVE}
       * @see ValueLoader#onReloadFailure(Throwable, long, int)
       * @see ValueLoader#onReloadTimeout(long, int)
       * @return an object that can be returned from {@link ValueLoader#onReloadFailure(Throwable, long, int)}
       * and {@link ValueLoader#onReloadTimeout(long, int)} to indicate that the failed or timed-out 
       * {@link #reload} operation should not be retried and the cached value be removed
       */
      public static <V> FailedReloadAction<V> remove() {
        return new FailedReloadAction<>(Action.REMOVE);
      }
    }
  }

  /**
   * A basic {@link ValueLoader} implementation that can be used when the asynchronous reloading behavior offered
   * by {@link #reload(Object)} is not needed.
   * <p>
   * This implementation's {@link #reload(Object)} method simply delegates to {@link #load()}
   * and returns an {@linkplain Futures#immediateFuture immediate future} containing the produced value.
   * <p>
   * In this case there is no need to override  either {@link #onReloadTimeout} or {@link #onReloadFailure},
   * since any unchecked exceptions thrown by {@link #load()} will propagate to the caller.
   *
   * @author Alex
   * @since 10/17/2022
   */
  public static abstract class SimpleValueLoader<V> implements ValueLoader<V> {
    @Nonnull
    @Override
    public Future<V> reload(V oldValue) {
      return Futures.immediateFuture(load());
    }

    @Nonnull
    @Override
    public FailedReloadAction<V> onReloadFailure(Throwable cause, long valueAge, int failCount) {
      return FailedReloadAction.ignore();
    }

    @Nonnull
    @Override
    public FailedReloadAction<V> onReloadTimeout(long valueAge, int failCount) {
      return FailedReloadAction.ignore();
    }
  }
}
