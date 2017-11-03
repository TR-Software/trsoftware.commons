package solutions.trsoftware.commons.client.util.iterators;

import java.util.Iterator;

/**
 * An iterator that counts the number of elements generated from the delegate, and optionally limits that number.
 * @author Alex, 4/20/2015
 */
public class CountingIterator<T> extends DelegatedNonMutatingIterator<T> {

  /** The number of successful invocations of {@link #next()} */
  private int count;
  private int limit = Integer.MAX_VALUE;

  public CountingIterator(Iterator<T> delegate) {
    super(delegate);
  }

  public CountingIterator(Iterator<T> delegate, int limit) {
    super(delegate);
    this.limit = limit;
  }

  @Override
  public boolean hasNext() {
    return super.hasNext() && count < limit;
  }

  @Override
  public T next() {
    T ret = super.next();
    count++;
    return ret;
  }

  public int getCount() {
    return count;
  }
}
