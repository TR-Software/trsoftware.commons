package solutions.trsoftware.commons.server.util;

/**
 * An example of a thread that will not be considered as running in JUnit
 * because it's not defined inside a test class.
 *
 * @author Alex
 */
class ServerRuntimeUtilsTestThread extends Thread {
  private volatile Boolean ranInJUnit;

  @Override
  public void run() {
    ranInJUnit = ServerRuntimeUtils.runningInJUnit();
  }

  public Boolean getRanInJUnit() {
    return ranInJUnit;
  }
}
