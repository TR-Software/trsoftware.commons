package solutions.trsoftware.commons.shared.util.collections;

import com.google.common.collect.ForwardingList;
import com.google.gwt.thirdparty.guava.common.annotations.VisibleForTesting;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * A decorator that wraps a {@link List} to allow using negative indices, which are interpreted as
 * offsets from the end of the list, for all {@link List} operations involving element indices.
 * <p>
 * Additionally, provides the following convenience methods that are missing from the {@link List} interface:
 * <ul>
 *   <li>{@link #subList(int)}: returns a sublist starting at the given index
 *   (as a shortcut for {@link #subList(int, int)} where the 2nd arg defaults to the list size)
 *   </li>
 * </ul>
 * <p>
 * Examples:
 * <pre>{@code
 *   ArrayList<Integer> original = ListUtils.arrayList(0, 1, 2, 3, 4);
 *   FluentList<Integer> fluent = FluentList.from(original);
 *     assertEquals((Integer)4, fluent.get(-1));
 *   fluent.set(-2, 33);
 *     assertEquals(Arrays.asList(0, 1, 2, 33, 4), original);
 *   FluentList<Integer> subList = fluent.subList(-3);
 *     assertEquals(Arrays.asList(2, 33, 4), subList);
 *     assertEquals(original.subList(2, original.size()), subList);
 *   subList.add(-1, 333);
 *     assertEquals(Arrays.asList(0, 1, 2, 33, 333, 4), original);
 * }</pre>
 *
 * @author Alex
 * @since 12/27/2022
 */
@SuppressWarnings("NullableProblems")
public class FluentList<E> extends ForwardingList<E> {

  @Nonnull
  private final List<E> delegate;

  /**
   * Protected constructor for subclassing.
   * Use the {@code public} factory method {@link #from(List)} instead of this constructor.
   */
  protected FluentList(List<E> delegate) {
    this.delegate = Objects.requireNonNull(delegate, "delegate");
  }

  /**
   * @return a {@link FluentList} that wraps the given list or itself if it's already a {@link FluentList}
   */
  public static <E> FluentList<E> from(List<E> list) {
    return (list instanceof FluentList)
        ? (FluentList<E>) list
        : new FluentList<>(list);
  }

  @Override
  public List<E> delegate() {
    return delegate;
  }

  /**
   * Provides support for using negative indices to indicate an offset from the end of the list.
   *
   * @param index a possibly-negative list index
   * @return the given index or {@code size() - index} if it's negative
   * @throws IndexOutOfBoundsException if the resulting index is not out-of-bounds of the underlying list
   */
  @VisibleForTesting
  int normalizeIndex(int index) {
    int normalized = normalizeIndexUnchecked(index);
    if (index < 0 && normalized < 0 || normalized >= size())
      // we check the bounds only for negative args (to generate a proper exception message)
      // otherwise, we defer the bounds checking to the List methods consuming this index
      throw new IndexOutOfBoundsException(String.valueOf(index));
    return normalized;
  }

  /**
   * Same as {@link #normalizeIndex(int)} but doesn't check that the resulting index is within the bounds
   * of this list.
   *
   * @param index a possibly-negative list index
   * @return the given index or {@code size() - index} if it's negative
   */
  private int normalizeIndexUnchecked(int index) {
    return (index < 0) ? size() + index : index;
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> elements) {
    return super.addAll(normalizeIndex(index), elements);
  }

  @Override
  public E get(int index) {
    return super.get(normalizeIndex(index));
  }

  @Override
  public E set(int index, E element) {
    return super.set(normalizeIndex(index), element);
  }

  @Override
  public void add(int index, E element) {
    super.add(normalizeIndex(index), element);
  }

  @Override
  public E remove(int index) {
    return super.remove(normalizeIndex(index));
  }

  @Override
  public ListIterator<E> listIterator(int index) {
    return super.listIterator(normalizeIndex(index));
  }

  @Override
  public FluentList<E> subList(int fromIndex, int toIndex) {
    int size = size();
    int from = normalizeIndexUnchecked(fromIndex);
    int to = normalizeIndexUnchecked(toIndex);
    if (from < 0 || from > size)  // for subList, the indices can be equal to size()
      throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
    if (to < 0 || to > size)
      throw new IndexOutOfBoundsException("toIndex = " + toIndex);
    return from(super.subList(from, to));
  }

  /**
   * Returns a sublist starting at the given index.  This method simply provides a shortcut for calling
   * {@link List#subList(int, int)} with the list size as the 2nd arg.
   *
   * @param fromIndex low endpoint (inclusive) of the subList
   * @return a sublist of the given list starting with the given index (up to the end of the list)
   * @throws IndexOutOfBoundsException for an illegal starting index value
   *         <code>(fromIndex &lt; 0 || fromIndex &gt; list.size())</code>
   */
  public FluentList<E> subList(int fromIndex) {
    return subList(fromIndex, size());
  }
}
