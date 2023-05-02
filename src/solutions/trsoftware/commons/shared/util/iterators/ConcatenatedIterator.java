package solutions.trsoftware.commons.shared.util.iterators;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;

import javax.annotation.Nonnull;
import java.util.Iterator;

import static java.util.Objects.requireNonNull;

/**
 * Provides a more-efficient implementation of {@link Iterators#concat} for
 * the case of only 2 source iterators, which can be implemented without constructing any intermediate
 * objects (such as {@code Iterator[]} or {@code Iterator<Iterator>})
 *
 * @author Alex
 * @since 4/24/2023
 */
public class ConcatenatedIterator<T> extends AbstractIterator<T> {

  protected Iterator<T> current;
  protected final Iterator<T> it2;

  public ConcatenatedIterator(@Nonnull Iterator<T> it1, @Nonnull Iterator<T> it2) {
    this.current = requireNonNull(it1, "it1");
    this.it2 = requireNonNull(it2, "it2");
  }

  /**
   * @return a {@link ConcatenatedIterator} over the elements contained in the given iterables.
   */
  @Nonnull
  public static <T> ConcatenatedIterator<T> of(@Nonnull Iterable<T> a, @Nonnull Iterable<T> b) {
    return new ConcatenatedIterator<>(a.iterator(), b.iterator());
  }

  @Override
  protected T computeNext() {
    if (!current.hasNext()) {
      if (current == it2)
        return endOfData();
      current = it2;
      return computeNext();
    }
    return current.next();
  }

}
