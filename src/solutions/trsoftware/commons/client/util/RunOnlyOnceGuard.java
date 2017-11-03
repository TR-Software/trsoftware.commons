package solutions.trsoftware.commons.client.util;

/**
 * Jan 21, 2010
 *
 * @author Alex
 */
public class RunOnlyOnceGuard {
  private boolean locked = false;

  /**
   * @throws IllegalStateException if this method has already been called.
   */
  public void check(String failureMessage) throws IllegalStateException {
    if (locked)  // this method should only be called once per lifetime of this instance
      throw new IllegalStateException(failureMessage);
    locked = true;
  }

  public boolean isLocked() {
    return locked;
  }
}
