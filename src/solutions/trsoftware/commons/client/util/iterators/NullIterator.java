package solutions.trsoftware.commons.client.util.iterators;

import java.util.NoSuchElementException;

/**
 * An iterator that doesn't return any elements.
 *
 * @author Alex, 4/17/2015
 */
public class NullIterator<T> extends NonMutatingIterator<T> {

  private static NullIterator instance;

  public static <T> NullIterator<T> getInstance() {
    if (instance == null)
      instance = new NullIterator();
    return instance;
  }

  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public T next() {
    throw new NoSuchElementException();
  }
}
