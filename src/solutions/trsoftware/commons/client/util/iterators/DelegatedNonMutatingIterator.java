package solutions.trsoftware.commons.client.util.iterators;

import java.util.Iterator;

/**
 * Delegates to the underlying iterator, but {@link #remove()} throws an exception.
 * 
 * Jan 15, 2010
 * @author Alex
 */
public class DelegatedNonMutatingIterator<T> extends DelegatedIterator<T> {

  public DelegatedNonMutatingIterator(Iterator<T> delegate) {
    super(delegate);
  }

  public final void remove() {
    throw new UnsupportedOperationException(getClass().getName() + " does not support Iterator.remove");
  }
}
