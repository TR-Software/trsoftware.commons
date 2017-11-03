package solutions.trsoftware.commons.server.util;

/**
 * @author Alex, 1/7/14
 */
public abstract class ThreadUtils {

  /** Sleeps the current thread for the given duration, ignoring all {@link InterruptedException}s */
  public static void sleepUnchecked(long millis) {
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException e) {
    }
  }

}
