package solutions.trsoftware.commons.client.util.iterators;

import java.util.Iterator;

/**
 * An iterator that delegates all operations to the underlying iterator.  Useful for intercepting and overriding
 * methods of another iterator.
 * 
 * Jan 15, 2010
 * @author Alex
 */
public class DelegatedIterator<T> implements Iterator<T> {
  protected Iterator<T> delegate;

  public DelegatedIterator(Iterator<T> delegate) {
    this.delegate = delegate;
  }

  public boolean hasNext() {
    return delegate.hasNext();
  }

  public T next() {
    return delegate.next();
  }

  @Override
  public void remove() {
    delegate.remove();
  }
}
