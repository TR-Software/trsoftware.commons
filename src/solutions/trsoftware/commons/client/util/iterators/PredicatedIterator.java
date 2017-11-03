package solutions.trsoftware.commons.client.util.iterators;

import solutions.trsoftware.commons.client.util.Predicate;

import java.util.Iterator;

/**
 * Implements {@link FilteringIterator#filter(Object)} by invoking the given predicate.
 *
 * @author Alex, 1/9/14
 */
public class PredicatedIterator<T> extends FilteringIterator<T> {

  /** The {@link #next} method will return only those elements from the {@link #delegate} iterator that satisfy this predicate */
  private final Predicate<T> predicate;

  public PredicatedIterator(Iterator<T> delegate, Predicate<T> predicate) {
    super(delegate);
    this.predicate = predicate;
  }

  public PredicatedIterator(boolean includeNullElements, Iterator<T> delegate, Predicate<T> predicate) {
    super(includeNullElements, delegate);
    this.predicate = predicate;
  }

  @Override
  protected boolean filter(T elt) {
    return predicate.apply(elt);
  }

}
