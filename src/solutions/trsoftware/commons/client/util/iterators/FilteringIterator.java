package solutions.trsoftware.commons.client.util.iterators;

import java.util.Iterator;

/**
 * Filters a delegated iterator by returning only those elements for which the {@link #filter(Object)} method is true.
 *
 * @author Alex, 1/9/14
 */
public abstract class FilteringIterator<T> extends DelegatingAbstractIterator<T> {

  private boolean includeNullElements;

  public FilteringIterator(boolean includeNullElements, Iterator<T> delegate) {
    super(delegate);
    this.includeNullElements = includeNullElements;
  }

  public FilteringIterator(Iterator<T> delegate) {
    this(false, delegate);
  }

  @Override
  protected T computeNext() {
    while (delegate.hasNext()) {
      T elt = delegate.next();
      if (elt == null && !includeNullElements)
        continue;
      if (filter(elt)) {
        return elt;
      }
    }
    return endOfData();
  }

  /**
   * @return true iff the given element should be returned.
   */
  protected abstract boolean filter(T elt);

}
