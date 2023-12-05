package solutions.trsoftware.commons.shared.util.iterators;

import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Base class for primitive {@code char} iterators that can be used to iterate over {@code char[]} arrays,
 * instances of {@link CharSequence}, {@link String}, etc.
 * Does not support {@linkplain #remove() removal}.
 *
 *
 * @see java.text.CharacterIterator
 *
 * @author Alex
 * @since 10/31/2023
 */
public abstract class IndexedCharIterator implements CharIterator {

  /** The starting index value (iteration will start with <code>{@link #i} = {@link #start}</code>) */
  protected final int start;
  /** Upper limit for the index value (iteration will stop when {@link #i} is greater than or equal to this value) */
  protected final int limit;
  /** The next index value to be returned */
  protected int i;

  /**
   * Creates a new instance to iterate indices in the range {@code [0, limit[}.
   * @param limit upper bound for the index value (exclusive)
   */
  public IndexedCharIterator(int limit) {
   this(0, limit);
  }

  /**
   * Creates a new instance to iterate indices in the range {@code [start, limit[}
   *
   * @param start initial index value (inclusive)
   * @param limit upper bound for the index value (exclusive)
   */
  public IndexedCharIterator(int start, int limit) {
    this.start = start;
    this.limit = limit;
    i = start;
  }

  protected abstract char get(int idx);

  @Override
  public boolean hasNext() {
    return i < limit;
  }

  @Override
  public char nextChar() {
    maybeThrowNoSuchElement();
    return get(i++);
  }

  /**
   * @throws NoSuchElementException if {@link #hasNext()} returns {@code false}, to comply with the Iterator interface
   */
  private void maybeThrowNoSuchElement() {
    if (!hasNext())
      throw new NoSuchElementException();  // to comply with the Iterator interface
  }

  /**
   * @see ListIterator#nextIndex()
   */
  public int nextIndex() {
    return i;
  }

}
