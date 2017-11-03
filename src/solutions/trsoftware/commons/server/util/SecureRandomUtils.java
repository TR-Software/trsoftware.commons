package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.server.util.codec.Base64Alphabet;

import java.security.SecureRandom;

/**
 * @author Alex, 9/14/2017
 */
public abstract class SecureRandomUtils {

  public static final SecureRandom rnd = new SecureRandom();

  /**
   * @return A string of {@code length} chars chosen at random from {@link Base64Alphabet#CHARS}
   */
  public static String randString(int length) {
    byte[] chars = Base64Alphabet.CHARS;
    StringBuilder buf = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      buf.append((char)chars[rnd.nextInt(chars.length)]);
    }
    return buf.toString();
  }

  public static void main(String[] args) {
    System.out.println(randString(64));
  }

}
