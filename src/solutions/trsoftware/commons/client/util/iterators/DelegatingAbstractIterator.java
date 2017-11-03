package solutions.trsoftware.commons.client.util.iterators;

import com.google.common.collect.AbstractIterator;

import java.util.Iterator;

/**
 * @author Alex, 10/15/2016
 */
public abstract class DelegatingAbstractIterator<T> extends AbstractIterator<T> {

  protected final Iterator<T> delegate;

  public DelegatingAbstractIterator(Iterator<T> delegate) {
    this.delegate = delegate;
  }
}
