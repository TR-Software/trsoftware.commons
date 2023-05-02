package solutions.trsoftware.commons.shared.util.iterators;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * A {@link ConcatenatedIterator} with the ability to filter elements using a {@link Predicate}.
 *
 * @author Alex
 * @since 4/24/2023
 */
public class ConcatenatedFilteringIterator<T> extends ConcatenatedIterator<T> {

  private final Predicate<? super T> predicate;

  /**
   * @param predicate matches the elements to retain
   */
  public ConcatenatedFilteringIterator(@Nonnull Iterator<T> it1, @Nonnull Iterator<T> it2, Predicate<? super T> predicate) {
    super(it1, it2);
    this.predicate = predicate;
  }

  /**
   * @param predicate matches the elements to retain
   * @return a {@link ConcatenatedFilteringIterator} over the elements contained in the given iterables that match
   *   the given predicate
   */
  @Nonnull
  public static <T> ConcatenatedFilteringIterator<T> of(@Nonnull Iterable<T> a, @Nonnull Iterable<T> b, Predicate<? super T> predicate) {
    return new ConcatenatedFilteringIterator<>(a.iterator(), b.iterator(), predicate);
  }

  @Override
  protected T computeNext() {
    while (current.hasNext()) {
      T element = current.next();
      if (predicate.test(element))
        return element;
    }
    if (current == it2)
      return endOfData();
    current = it2;
    return computeNext();
  }

}
