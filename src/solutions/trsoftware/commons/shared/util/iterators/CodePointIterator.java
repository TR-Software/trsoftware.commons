
/*
Additional copyright/license notice:

This code was modified from http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/raw-file/9db1950723f1/src/share/classes/java/lang/CharSequence.java
and therefore the terms of the "GNU General Public License, version 2, with the Classpath Exception"
(https://openjdk.java.net/legal/gplv2+ce.html) apply to this code as well.
*/
package solutions.trsoftware.commons.shared.util.iterators;

import java.util.NoSuchElementException;
import java.util.function.IntConsumer;

/**
 * Based on the implementation of {@link CharSequence#codePoints()}, this class iterates over the Unicode code points
 * of a {@link CharSequence} such as a {@link String} or {@link StringBuilder}.
 * <p>
 * This class is intended as a GWT-compatible substitute for {@link CharSequence#codePoints()} (which is not
 * emulated by GWT 2.8.2)
 *
 * @author Alex
 * @since 5/14/2019
 */
public class CodePointIterator implements IntIterator {

  private int cur = 0;
  private CharSequence chars;

  public CodePointIterator(CharSequence chars) {
    this.chars = chars;
  }

  @Override
  public void forEachRemaining(IntConsumer block) {
    final int length = chars.length();
    int i = cur;
    try {
      while (i < length) {
        char c1 = chars.charAt(i++);
        if (!Character.isHighSurrogate(c1) || i >= length) {
          block.accept(c1);
        }
        else {
          char c2 = chars.charAt(i);
          if (Character.isLowSurrogate(c2)) {
            i++;
            block.accept(Character.toCodePoint(c1, c2));
          }
          else {
            block.accept(c1);
          }
        }
      }
    }
    finally {
      cur = i;
    }
  }

  public boolean hasNext() {
    return cur < chars.length();
  }

  public int nextInt() {
    final int length = chars.length();

    if (cur >= length) {
      throw new NoSuchElementException();
    }
    char c1 = chars.charAt(cur++);
    if (Character.isHighSurrogate(c1) && cur < length) {
      char c2 = chars.charAt(cur);
      if (Character.isLowSurrogate(c2)) {
        cur++;
        return Character.toCodePoint(c1, c2);
      }
    }
    return c1;
  }
}
