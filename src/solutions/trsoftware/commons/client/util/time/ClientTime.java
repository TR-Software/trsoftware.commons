package solutions.trsoftware.commons.client.util.time;

import com.google.gwt.core.client.Duration;

/**
 * @author Alex, 3/24/2015
 */
public class ClientTime extends Time {

  public static final ClientTime INSTANCE = new ClientTime();

  @Override
  public double currentTimeMillis() {
    return Duration.currentTimeMillis();
  }
}
