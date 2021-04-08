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

package solutions.trsoftware.commons.server.util.callables;

import java.util.concurrent.Callable;

/**
 * Wraps a {@link Callable}, allowing execution of some additional code before (override {@link #doBefore()})
 * and after (override {@link #doAfter(Exception)}) the delegate's {@link Callable#call() call()} method is invoked.
 *
 * @see #doCall()
 * @author Alex
 * @since 11/29/2017
 */
public class DelegatedCallable<V> implements Callable<V> {

  private final Callable<V> delegate;

  /**
   * @param delegate will be invoked by {@link #doCall()}; could pass {@code null}, but in that case should also
   * override {@link #doCall()} to avoid a {@link NullPointerException}
   */
  public DelegatedCallable(Callable<V> delegate) {
    this.delegate = delegate;
  }

  /**
   * Subclasses can override if they wish to do something before {@link #doCall()}.
   */
  protected void doBefore() {}
  
  /**
   * Subclasses can override if they wish to do something after {@link #doCall()}.
   * This method is guaranteed to execute even if {@link #doCall()} threw an exception.
   *
   * @param ex any exception that was thrown by {@link #doCall()}, or {@code null} if no exception was thrown.
   */
  protected void doAfter(Exception ex) {}

  @Override
  public final V call() throws Exception {
    doBefore();
    V ret;
    try {
      ret = doCall();
    }
    catch (Exception e) {
      doAfter(e);
      throw e;
    }
    doAfter(null);
    return ret;
  }

  /**
   * Invokes {@link #delegate}{@code .}{@link Callable#call() call()}. Subclasses should override if they don't
   * pass a delegate instance to the constructor, otherwise will throw a {@link NullPointerException}.
   *
   * @see #DelegatedCallable(Callable)
   * @return the result of {@link #delegate}{@code .}{@link Callable#call() call()}
   * @throws NullPointerException if a delegate instance wasn't passed to the constructor; subclasses can avoid
   * this method to avoid this exception
   * @throws Exception any exception that was thrown by {@link #delegate}{@code .}{@link Callable#call() call()}
   */
  protected V doCall() throws Exception {
    return delegate.call();
  }
}
