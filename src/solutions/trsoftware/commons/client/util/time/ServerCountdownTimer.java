package solutions.trsoftware.commons.client.util.time;

import solutions.trsoftware.commons.bridge.ServerTime;

/**
 * A self-updating timer that shows the time remaining until some absolute time on the server.  Uses
 * {@link ServerTime} to determine the corresponding delay on the client.
 * Every time it fires, the timer checks for changes in the referenced {@link ServerTime}
 * instance and updates itself accordingly.
 *
 * @author Alex
 */
public class ServerCountdownTimer extends CountdownTimer {

  public ServerCountdownTimer() {
    this(DEFAULT_UPDATE_INTERVAL_MILLIS);
  }

  /**
   * @param refreshInterval The delay, in milliseconds between consecutive firings of the timer.
   */
  public ServerCountdownTimer(final int refreshInterval) {
    super(ServerTime.INSTANCE, refreshInterval);
  }

}
