package solutions.trsoftware.commons.client.logging;

/**
 * Prints out real logging information, intended for deferred binding in
 * a debugging permutation of the app.  Delegates to the Console instance,
 * which logs the output to window.console (or to a UI element if window.console
 * is missing).
 *
 * @author Alex
 */
public class LogImplFull extends LogImpl {

  @Override
  protected boolean isLoggingEnabled() {
    return true;
  }

  @Override
  public void log(String msg) {
    Console.instance.log(msg);
  }

}
