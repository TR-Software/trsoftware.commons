package solutions.trsoftware.commons.client.bridge.text;

/**
 * Oct 30, 2009
 *
 * @author Alex
 */
public interface NumberFormatter {
  /**
   * Formats the given number using the constructor parameters to control how
   * the number will appear.
   */
  String format(double number);
}
