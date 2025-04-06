/*
 * Copyright 2024 TR Software Inc.
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

import javax.annotation.Nullable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Strings.lenientFormat;

/**
 * A Runnable that traps uncaught exceptions so they can handled as needed (e.g. logged and suppressed).
 * <p>
 * Intended to be used with {@link ScheduledExecutorService}, which <em>silently</em> cancels recurring runnables
 * when they throw an unchecked exception.
 *
 * @author Alex
 * @since 9/1/2023
 * @see DelegatedRunnable
 * @see ScheduledExecutorService#scheduleWithFixedDelay
 * @see ScheduledExecutorService#scheduleAtFixedRate
 */
public interface SafeRunnable extends Runnable {
  /*
    @see:
      - https://stackoverflow.com/questions/6894595/scheduledexecutorservice-exception-handling
      - http://web.archive.org/web/20190307143204/http://code.nomad-labs.com:80/2011/12/09/mother-fk-the-scheduledexecutorservice
   */


  /**
   * Invokes {@link #doRun()} in a {@code try / catch} block, to allow logging and handling unchecked exceptions
   * instead of letting them escape.
   */
  @Override
  default void run() {
    try {
      doRun();
    }
    catch (RuntimeException | Error e) {
      boolean rethrow = !onError(e);
      Logger logger = getLogger();
      if (logger != null) {
        logger.log(Level.SEVERE, e, () -> lenientFormat("%s uncaught exception thrown by %s",
            rethrow ? "Re-throwing" : "Suppressing", this));
      }
      if (rethrow)
        throw e;
    }
  }

  /**
   * The code to run.  Can safely throw unchecked exceptions, which will be passed to {@link #onError(Throwable)}.
   */
  void doRun();

  /**
   * Invoked from the {@code catch} statement in {@link #run()} if the invocation of {@link #doRun()} threw
   * an unchecked exception.
   *
   * @param e the {@link RuntimeException} or {@link Error}
   * @return {@code true} to ignore the exception and continue or {@code false} to rethrow the exception, thereby
   *   potentially cancelling any scheduled repeat executions if running from a {@link ScheduledExecutorService}
   */
  default boolean onError(Throwable e) {
    return true;
  }

  /**
   * Returns the logger for uncaught exceptions.
   * Can be overridden to return a different logger, or {@code null} to suppress logging of such exceptions.
   */
  @Nullable
  default Logger getLogger() {
    return Logger.getLogger(getClass().getName());
  }
}
