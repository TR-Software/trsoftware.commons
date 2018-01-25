package solutions.trsoftware.commons.shared.util.text;


import solutions.trsoftware.commons.shared.util.iterators.CharSequenceIterator;

import java.util.Iterator;

/**
 * Represents a range of consecutive chars.
 *
 * @author Alex
 * @since 12/24/2017
 */
public class CharRange implements CharSequence, Iterable<Character> {

  public final char min;
  public final char max;

  /** Will be lazily initialized on the first invocation of {@link #toString()} */
  private String str;

  /**
   * Creates a range of consecutive chars in the range {@code [min, max]}
   * @param min the lowest char to be included in the range
   * @param max the highest char to be included in the range (inclusive)
   */
  public CharRange(char min, char max) {
    if (min > max)
      throw new IllegalArgumentException(min + " > " + max);
    this.min = min;
    this.max = max;
  }

  @Override
  public int length() {
    return max - min + 1;
  }

  @Override
  public char charAt(int index) {
    if (index < 0 || index >= length())
      throw new IndexOutOfBoundsException(String.valueOf(index));
    return (char)(min + index);
  }

  @Override
  public String toString() {
    if (str == null)
      str = toString(this);
    return str;
  }

  public static String toString(CharSequence charSequence) {
    if (charSequence != null) {
      int len = charSequence.length();
      if (len > 0) {
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) {
          chars[i] = charSequence.charAt(i);
        }
        return new String(chars);
      }
    }
    return "";
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return toString().subSequence(start, end);
  }

  @Override
  public Iterator<Character> iterator() {
    return new CharSequenceIterator(this);
  }
}
