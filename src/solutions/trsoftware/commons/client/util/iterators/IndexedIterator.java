package solutions.trsoftware.commons.client.util.iterators;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Base class to help implement iterators over indexed sequences.
 *
 * @author Alex, 4/27/2016
 */
public abstract class IndexedIterator<T> implements Iterator<T> {

  /** The lowest index value (iteration will stop when {@link #i} is less than this value) */
  protected final int min;
  /** The largest index value (iteration will stop when {@link #i} is greater than or equal to this value) */
  protected final int limit;
  /** The next index value to be returned */
  protected int i;

  /**
   * Creates a new instance to iterate indices in the range {@code [0, limit[}.
   * @param limit the value for {@link #limit}
   */
  public IndexedIterator(int limit) {
   this(0, limit);
  }

  /**
   * Creates a new instance to iterate indices in the range {@code [start, limit[}
   *
   * @param start the value for {@link #min}
   * @param limit the value for {@link #limit}
   */
  public IndexedIterator(int start, int limit) {
    this.min = start;
    this.limit = limit;
    i = start;
  }

  protected abstract T get(int idx);

  @Override
  public boolean hasNext() {
    return i < limit;
  }

  @Override
  public T next() {
    if (!hasNext())
      throw new NoSuchElementException();  // to comply with the Iterator interface
    return get(i++);
  }

  /**
   * @see ListIterator#nextIndex()
   */
  public int nextIndex() {
    return i;
  }

  /**
   * Not supported.
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
