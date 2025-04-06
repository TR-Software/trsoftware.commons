/*
 * Copyright 2025 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util.callables;

/**
 * Wraps a {@link Runnable}, allowing execution of some additional code before (override {@link #doBefore()})
 * and after (override {@link #doAfter(Throwable)}) the delegate's {@link Runnable#run() run()} method is invoked.
 * <p>
 * Also, {@link java.util.concurrent.ExecutorService} implementations don't log exceptions arising from a {@link Runnable}
 * they execute.  This class wraps the {@link #run()} method in a try/catch block to log
 * and rethrow any {@link RuntimeException} or {@link Error} that might arise.
 *
 *
 * @see #doRun()
 * @see SafeRunnable
 * @author Alex
 * @since 11/29/2017
 */
public class DelegatedRunnable implements Runnable {

  private final Runnable delegate;

  /**
   * @param delegate will be invoked by {@link #doRun()}; could pass {@code null}, but in that case should also
   * override {@link #doRun()} to avoid a {@link NullPointerException}
   */
  public DelegatedRunnable(Runnable delegate) {
    this.delegate = delegate;
  }

  /**
   * Subclasses can override if they wish to do something before {@link #doRun()}.
   */
  protected void doBefore() {}
  
  /**
   * Subclasses can override if they wish to do something after {@link #doRun()}.
   * This method is guaranteed to execute even if {@link #doRun()} threw an exception.
   *
   * @param ex an unchecked exception that was thrown by {@link #doRun()} (if any),
   *   or {@code null} if no exception was thrown
   */
  protected void doAfter(Throwable ex) {
    if (ex != null)
      ex.printStackTrace();
  }

  @Override
  public final void run() {
    doBefore();
    try {
      doRun();
    }
    catch (RuntimeException | Error e) {
      doAfter(e);
      throw e;
    }
    doAfter(null);
  }

  /**
   * Invokes {@link #delegate}{@code .}{@link Runnable#run() run()}. Subclasses should override if they don't
   * pass a delegate instance to the constructor, otherwise will throw a {@link NullPointerException}.
   *
   * @see #DelegatedRunnable(Runnable)
   * @throws NullPointerException if a delegate instance wasn't passed to the constructor; subclasses can override
   * this method to avoid this exception
   */
  protected void doRun() {
    delegate.run();
  }
}
