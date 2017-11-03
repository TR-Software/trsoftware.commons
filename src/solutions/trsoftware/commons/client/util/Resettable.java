package solutions.trsoftware.commons.client.util;

/**
 * Defines an object whose state can be reset to the initial state it had just after construction.
 *
 * @author Alex, 10/27/2016
 */
public interface Resettable {
  /** Resets the object's state back to the point at which it was created. */
  void reset();
}
