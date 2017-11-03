package solutions.trsoftware.commons.server.util.codec;

/**
 * Jan 28, 2009
 *
 * @author Alex
 */
public interface Alphabet {
  /**
   * @param plainInt Must be in range 0..radix (exclusive)
   * @return The character representing the int
   */
  byte encode(int plainInt);

  /**
   * @param codedByte A character representing an int in the range 0..radix (exclusive)
   * @return The int in the range 0..radix (exclusive)
   */
  int decode(byte codedByte);

  /**
   * The character used to encode a minus sign (for negative numbers)
   * @return
   */
  byte sign();

  /** The base of the alphabet, e.g. 2, 10, 16, 36, 62, 64 */
  int base();
}
