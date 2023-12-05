package solutions.trsoftware.commons.server.util.callables;

import javax.annotation.Nullable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

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
        logger.log(Level.SEVERE, e, () -> format("%s uncaught exception thrown by %s",
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
