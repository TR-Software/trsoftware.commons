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

package solutions.trsoftware.commons.server.testutil.concurrent;

import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Skeletal implementation of the {@link Future} interface.  Subclasses just have to implement {@link #doGet()}.
 * @author Alex
 * @since 9/16/2022
 */
public abstract class MockFuture<V> implements Future<V> {
  private boolean cancelled;
  private boolean done;
  private V result;
  /**
   * True iff {@link #get()} returned a result without throwing an exception.
   */
  private boolean hasResult;


  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    if (!done)
      return cancelled = true;
    return false;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public boolean isDone() {
    return done || cancelled;
  }

  /**
   * Implementation of {@link Future#get()}.
   */
  protected abstract V doGet() throws Exception;

  /**
   * {@inheritDoc}
   * <p>
   * Delegates to {@link #doGet()}.
   */
  @Override
  public V get() throws ExecutionException {
    if (hasResult)
      return result;
    if (cancelled)
      throw new CancellationException();
    try {
      result = doGet();
      hasResult = true;
      done = true;
    }
    catch (Exception e) {
      done = true;
      throw new ExecutionException(e);
    }
    return result;
  }

  @Override
  public V get(long timeout, TimeUnit unit) throws ExecutionException {
    return get();
  }

  /**
   * Constructs a mock {@link Future} whose {@link #get()} method delegates to the given {@link Callable}.
   */
  public static <V> MockFuture<V> fromCallable(Callable<V> delegate) {
    return new MockFuture<V>() {
      @Override
      protected V doGet() throws Exception {
        return delegate.call();
      }
    };
  }

  /**
   * Constructs a mock {@link Future} whose {@link #get()} method delegates to the given {@link Supplier}.
   */
  public static <V> MockFuture<V> fromSupplier(Supplier<V> supplier) {
    return new MockFuture<V>() {
      @Override
      protected V doGet() {
        return supplier.get();
      }
    };
  }

  /**
   * Constructs a mock {@link Future} whose {@link #get()} always returns the given value and never throws an exception.
   */
  public static <V> MockFuture<V> fromConstant(V result) {
    return new MockFuture<V>() {
      @Override
      protected V doGet() {
        return result;
      }
    };
  }
}
