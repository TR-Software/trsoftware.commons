package solutions.trsoftware.commons.server.util.codec;

/**
 * This is the standardized base64 alphabet.  It differs from typical
 * number base encodings (like hex) because it doesn't start with '0' - '9'
 *
 * @author Alex
 */
public class Base64Alphabet extends AlphabetAdapter {

  private static final int BASE = 64;
  public static final byte[] CHARS = {
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
      'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
      'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
  };

  public Base64Alphabet() {
    super(BASE, CHARS, (byte)'-');
  }
}
