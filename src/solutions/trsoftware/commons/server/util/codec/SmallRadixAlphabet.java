package solutions.trsoftware.commons.server.util.codec;

import java.util.Arrays;

/**
 * Any alphabet base 62 or less
 *
 * @author Alex
 */
public class SmallRadixAlphabet extends AlphabetAdapter {
  static final byte[] codingAlphabet = {
      '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' ,
      '8' , '9' , 'a' , 'b' , 'c' , 'd' , 'e' , 'f' ,
      'g' , 'h' , 'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
      'o' , 'p' , 'q' , 'r' , 's' , 't' , 'u' , 'v' ,
      'w' , 'x' , 'y' , 'z' , 'A' , 'B' , 'C' , 'D' ,
      'E' , 'F' , 'G' , 'H' , 'I' , 'J' , 'K' , 'L' ,
      'M' , 'N' , 'O' , 'P' , 'Q' , 'R' , 'S' , 'T' ,
      'U' , 'V' , 'W' , 'X' , 'Y' , 'Z'
  };
  public SmallRadixAlphabet(int base) {
    super(base, Arrays.copyOfRange(codingAlphabet, 0, base), (byte)'-');
  }
}