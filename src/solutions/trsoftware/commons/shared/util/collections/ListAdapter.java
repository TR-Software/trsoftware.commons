package solutions.trsoftware.commons.shared.util.collections;

import javax.annotation.Nonnull;
import java.util.AbstractList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

import static java.util.Objects.requireNonNull;

/**
 * Provides a {@link List java.util.List} view of any data structure that has a size allows access to elements by index.
 *
 * @param <E> the element type
 */
public class ListAdapter<E> extends AbstractList<E> {
  private final IntFunction<E> elementGetter;
  private final IntSupplier sizeGetter;

  /**
   * @param elementGetter returns an element given an integer index (for {@link List#get(int)})
   * @param sizeGetter returns the list size
   */
  public ListAdapter(@Nonnull IntFunction<E> elementGetter, @Nonnull IntSupplier sizeGetter) {
    this.elementGetter = requireNonNull(elementGetter, "elementGetter");
    this.sizeGetter = requireNonNull(sizeGetter, "sizeGetter");
  }

  @Override
  public E get(int index) {
    return elementGetter.apply(index);
  }

  @Override
  public int size() {
    return sizeGetter.getAsInt();
  }
}
