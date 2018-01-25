package solutions.trsoftware.commons.shared.util.callables;

/**
 * A {@code boolean} function that takes no arguments.
 *
 * @author Alex
 * @since 11/29/2017
 */
public interface Condition {
  /**
   * @return {@code true} iff the condition is met
   */
  boolean check();
}
