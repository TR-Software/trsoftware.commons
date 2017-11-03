package solutions.trsoftware.commons.client.util.iterators;

import java.util.NoSuchElementException;

/**
 * An iterator that contains exactly one element.
 *
 * @author Alex, 4/17/2015
 */
public class SingletonIterator<T> extends NonMutatingIterator<T> {

  private T next;

  public SingletonIterator(T elt) {
    next = elt;
  }

  @Override
  public boolean hasNext() {
    return next != null;
  }

  @Override
  public T next() {
    if (next == null)
      throw new NoSuchElementException();
    T ret = next;
    next = null;
    return ret;
  }
}
