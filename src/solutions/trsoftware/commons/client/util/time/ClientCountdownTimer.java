package solutions.trsoftware.commons.client.util.time;

/**
 * A self-updating timer that shows the time remaining until some absolute time on the client.
 *
 * @author Alex
 */
public class ClientCountdownTimer extends CountdownTimer {

  public ClientCountdownTimer() {
    this(DEFAULT_UPDATE_INTERVAL_MILLIS);
  }

  /**
   * @param refreshInterval The delay, in milliseconds between consecutive firings of the timer.
   */
  public ClientCountdownTimer(final int refreshInterval) {
    super(ClientTime.INSTANCE, refreshInterval);
  }

}
