package solutions.trsoftware.commons.client.util.iterators;

import java.util.Iterator;

/**
 * Base class for iterators that don't support the {@link #remove()} operation.
 * 
 * Jan 15, 2010
 * @author Alex
 */
public abstract class NonMutatingIterator<T> implements Iterator<T> {

  @Override
  public final void remove() {
    throwRemoveNotSupported(getClass());
  }

  public static void throwRemoveNotSupported(Class cls) {
    throw new UnsupportedOperationException(cls.getName() + " does not support Iterator.remove");
  }
}
