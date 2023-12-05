package solutions.trsoftware.commons.shared.util.text;

import com.google.common.base.Preconditions;
import solutions.trsoftware.commons.shared.util.iterators.CharIterator;
import solutions.trsoftware.commons.shared.util.iterators.CharSequenceIterator;

import javax.annotation.Nonnull;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;

/**
 * Adapter that allows using a {@code char[]} as a {@link CharSequence}.
 *
 * @author Alex
 * @since 11/1/2023
 */
public class ArrayCharSequence implements CharSequence {

  @Nonnull
  protected final char[] chars;

  public ArrayCharSequence(@Nonnull char[] chars) {
    this.chars = requireNonNull(chars, "chars");
  }

  @Override
  public int length() {
    return chars.length;
  }

  @Override
  public char charAt(int index) {
    return chars[index];
  }

  @Override
  public ArrayCharSequence subSequence(int start, int end) {
    Preconditions.checkPositionIndexes(start, end, chars.length);
    return (start == 0 && end == chars.length) ? this :
        new ArrayCharSequence(Arrays.copyOfRange(chars, start, end));
  }

  @Override
  public String toString() {
    return new String(chars);
  }

  public CharIterator iterator() {
    return new CharSequenceIterator(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ArrayCharSequence that = (ArrayCharSequence)o;
    return Arrays.equals(chars, that.chars);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(chars);
  }

}
