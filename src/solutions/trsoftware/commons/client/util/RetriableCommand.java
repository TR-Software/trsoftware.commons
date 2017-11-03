package solutions.trsoftware.commons.client.util;

import com.google.gwt.user.client.Timer;

/**
 * A timer that automatically stops after some condition is met.  Similar
 * to an IncrementalCommand, but takes a user-specified time interval between
 * successive iterations.
 *
 * @author Alex
 */
public abstract class RetriableCommand {

  // TODO: merge this class with Waiter (use one to implement the other)

  private int attemptsRemaining;
  private Timer timer;
  
  private boolean started;
  private boolean stopped;

  /**
   * @param maxAttempts
   */
  public RetriableCommand(int maxAttempts) {
    attemptsRemaining = maxAttempts;
    timer = new Timer() {
      public void run() {
        Boolean result = null;
        if (attemptsRemaining <= 0 || !(result = executeIteration())) {
          stopped = true;
          cancel();
        }
        if (result != null)
          attemptsRemaining--;  // decrement the counter only if the iteration was actually executed
      }
    };
  }

  /**
   * @param delayMillis The time to wait between iterations.
   */
  public final void start(int delayMillis) {
    if (started)
      throw new IllegalStateException("RetriableCommand already started");
    started = true;
    timer.scheduleRepeating(delayMillis);
  }

  public int getAttemptsRemaining() {
    return attemptsRemaining;
  }

  public boolean isStopped() {
    return stopped;
  }

  public boolean isStarted() {
    return started;
  }

  /**
   * Execute the encapsulated behavior.
   *
   * @return <code>true</code> if the command has more work to do,
   *         <code>false</code> otherwise
   */
  protected abstract boolean executeIteration();

}
