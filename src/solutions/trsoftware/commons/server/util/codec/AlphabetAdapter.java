package solutions.trsoftware.commons.server.util.codec;

/**
 * Jan 28, 2009
 *
 * @author Alex
 */
public abstract class AlphabetAdapter implements Alphabet {

  private int base;
  private final byte[] codingAlphabet;
  private final byte[] decodingAlphabet;
  private final byte sign;


  public AlphabetAdapter(int base, byte[] codingAlphabet, byte sign) {
    this.base = base;
    this.codingAlphabet = codingAlphabet;
    this.sign = sign;

    decodingAlphabet = new byte[Byte.MAX_VALUE];
    for (int j = 0; j < decodingAlphabet.length; j++) {
      if (j < codingAlphabet.length)
        decodingAlphabet[codingAlphabet[j]] = (byte)j;
    }
  }

  /**
   * @param plainInt Must be in range 0..radix (exclusive)
   * @return The character representing the int
   */
  public byte encode(int plainInt) {
    return codingAlphabet[plainInt];
  }

  /**
   * @param codedByte A character representing an int in the range 0..radix (exclusive)
   * @return The int in the range 0..radix (exclusive)
   */
  public int decode(byte codedByte) {
    return decodingAlphabet[codedByte];
  }

  /**
   * The character used to encode a minus sign (for negative numbers)
   * @return
   */
  public byte sign() {
    return sign;
  }

  public int base() {
    return base;
  }
}