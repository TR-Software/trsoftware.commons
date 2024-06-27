package solutions.trsoftware.commons.shared.testutil;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * Facade for a collection, exposing only its {@link #iterator()} method.
 *
 * @author Alex
 * @since 8/15/2019
 */
public class WrappedIterable<T> implements Iterable<T> {

  private final Iterable<T> delegate;

  public WrappedIterable(Iterable<T> delegate) {
    this.delegate = delegate;
  }

  @Nonnull
  @Override
  public Iterator<T> iterator() {
    return delegate.iterator();
  }
}
