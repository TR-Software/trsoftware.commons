package solutions.trsoftware.commons.client.util.iterators;

import java.util.Iterator;

/**
 * Returns an output element after applying a transformation on the corresponding input element.
 * @author Alex, 1/12/14
 */
public abstract class TransformingIterator<I, O> implements Iterator<O> {

  protected Iterator<I> delegate;

  public TransformingIterator(Iterator<I> delegate) {
    this.delegate = delegate;
  }

  /** Transforms an input element into the corresponding output element */
  protected abstract O transform(I input);

  public boolean hasNext() {
    return delegate.hasNext();
  }

  public O next() {
    return transform(delegate.next());
  }

  @Override
  public void remove() {
    delegate.remove();
  }
}
