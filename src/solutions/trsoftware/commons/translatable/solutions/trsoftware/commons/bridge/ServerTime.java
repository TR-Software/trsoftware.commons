package solutions.trsoftware.commons.bridge;

import solutions.trsoftware.commons.client.util.time.ServerTimeClientImpl;
import solutions.trsoftware.commons.client.util.time.Time;

/**
 * Client-side version of {@link solutions.trsoftware.commons.bridge.ServerTime}.
 *
 *  @author Alex
 */
public abstract class ServerTime extends Time {

  // GWT Version -- GWT Version -- GWT Version -- GWT Version -- GWT Version

  public static final ServerTime INSTANCE = new ServerTimeClientImpl();

  public abstract double getAccuracy();

  public abstract void update(double serverTimestamp, double requestStartLocalTime, double requestEndLocalTime);
}
