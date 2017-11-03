package solutions.trsoftware.commons.client.util;

/**
 * A simple immutable container (i.e. box) for a boolean that cannot be modified
 * after being set.  Similar to a final field, with the only difference being
 * that the value can be initialized at any time.
 *
 * This class makes it easy to ensure that something happens only once:
 *
 * if (readOnlyFlag.set()) {
 *   // do something that should only happen once
 * }
 *
 * @author Alex
 */
public final class ReadOnlyFlag {

  private volatile boolean set = false;

  public ReadOnlyFlag() {
  }

  /**
   * Sets the flag if it has never been set before.
   *
   * @return true if successful. False return indicates that the value has already been set.
   */
  public synchronized boolean set() {
    return !set && (set = true);
  }

  public boolean isSet() {
    return set;
  }
}