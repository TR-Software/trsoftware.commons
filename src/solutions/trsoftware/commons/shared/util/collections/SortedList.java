/*
 * Copyright 2023 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.commons.shared.util.collections;

import solutions.trsoftware.commons.shared.util.ListUtils;

import javax.annotation.Nonnull;
import java.util.*;

import static com.google.common.base.Strings.lenientFormat;
import static java.util.Objects.requireNonNull;

/**
 *
 * @see ListUtils#findInsertionPoint(List, Comparable)
 * @see ListUtils#insertInOrder(List, Comparable)
 * @see ListUtils#insertInOrder(List, Comparable, int)
 *
 * @author Alex
 * @since 4/13/2023
 */
public class SortedList<E extends Comparable<E>> extends AbstractList<E> implements RandomAccess {

  /*
  TODO(4/14/2023): perhaps better to make it a Collection rather than a List, since many methods violate the list spec;
    instead could create a toList method that returns a copy of the list
  TODO(4/17/2023): if List, perhaps better to just extend ArrayList: this would take care of incrementing modCount,
    and allow a more efficient removeRange(int, int)
  */

  @Nonnull
  private final List<E> delegate;

  /**
   * Constructs an empty sorted list backed by an {@link ArrayList}
   */
  public SortedList() {
    delegate = new ArrayList<>();
  }

  /**
   * Constructs a sorted list containing the elements from the given collection.
   */
  public SortedList(Collection<? extends E> c) {
    delegate = new ArrayList<>(c);
    delegate.sort(null);
  }

  /**
   * Constructs a sorted list containing the given elements.
   */
  @SafeVarargs
  public SortedList(E... elements) {
    this(Arrays.asList(elements));
  }

  @Override
  public int size() {
    return delegate.size();
  }

  /**
   * Uses {@link Collections#binarySearch(List, Object)} to insert the specified element into this sorted list,
   * at the appropriate position (as defined by the elements' {@linkplain Comparable natural ordering}).
   *
   * @param element the element to be added to this list
   * @return {@code true} (as specified by {@link Collection#add})
   * @throws NullPointerException if the key is {@code null}
   * @see ListUtils#insertInOrder(List, Comparable)
   * @see ListUtils#findInsertionPoint(List, Comparable)
   */
  @Override
  public boolean add(@Nonnull E element) {
    // TODO: can create a subclass that overrides add(int, Object) to implement bounded size behavior
    ListUtils.insertInOrder(delegate, requireNonNull(element, "element"));
    modCount++;
    return true;
  }

  @Override
  public E get(int index) {
    return delegate.get(index);
  }

  /**
   * Replaces the element at the specified position in this list with the specified element,
   * as long as such a replacement would not cause the list to become unsorted.
   *
   * @param index {@inheritDoc}
   * @param element {@inheritDoc}
   * @return {@inheritDoc}
   * @throws IllegalArgumentException if the specified element is lower than {@code get(i-1)}
   *                                  or greater than {@code get(i+1)}
   * @throws NullPointerException if the specified element is null
   */
  @Override
  public E set(int index, E element) {
    requireNonNull(element, "element");
    rangeCheck(index);
    // make sure that placing the new element at this position doesn't violate the sort order
    // in other words: can set if it's neither less than the preceding elem nor greater than the next
    if (isGreaterThanOrEqualTo(element, index-1) && isLessThanOrEqualTo(element, index+1)) {
      // NOTE: not incrementing modCount because neither ArrayList nor AbstractList does that for this method
      return delegate.set(index, element);
    }
    throw new IllegalArgumentException(
              lenientFormat("Replacing element <%s> at index %s with <%s> would violate this list's sort order",
                  delegate.get(index), index, element));
  }

  /**
   * Inserts the specified element at the specified position in this list,
   * <em>as long as such an insertion would not cause the list to become unsorted</em>.
   * Shifts the element currently at that position (if any) and any subsequent elements to the right.
   *
   * @param index {@inheritDoc}
   * @param element {@inheritDoc}
   * @throws IllegalArgumentException if the specified element is lower than {@code get(i-1)}
   *                                  or greater than {@code get(i)}
   * @throws NullPointerException if the specified element is null
   */
  @Override
  public void add(int index, @Nonnull E element) {
    requireNonNull(element, "element");
    rangeCheckForAdd(index);
    // make sure that placing the new element at this position doesn't violate the sort order
    // in other words: can set if it's neither less than the preceding elem nor greater than the next
    if (isGreaterThanOrEqualTo(element, index-1) && isLessThanOrEqualTo(element, index)) {
      delegate.add(index, element);
      modCount++;
    } else {
      throw new IllegalArgumentException(illegalInsertionMsg(index, element));
    }
  }

  @Nonnull
  private String illegalInsertionMsg(int index, @Nonnull E element) {
    return lenientFormat("Inserting element <%s> at index %s would violate this list's sort order",
        element, index);
  }

  /**
   * Inserts all of the elements in the specified collection into this list at the specified position,
   * <em>as long as such an insertion would not cause the list to become unsorted</em>.
   * Shifts the element currently at that position (if any) and any subsequent elements to the right (increases their indices).
   * The new elements will appear in this list in the order that they are returned by the specified collection's iterator.
   * The behavior of this operation is undefined if the specified collection is modified while the operation is in progress.
   * (Note that this will occur if the specified collection is this list, and it's nonempty.)
   *
   * @throws IllegalArgumentException if this operation would cause the list to become unsorted.  Specifically,
   *   if any element in the specified collection is lower than {@code get(i-1)}, greater than {@code get(i)},
   *   or lower than the preceding element in that collection (i.e. the collection is unsorted)
   */
  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    requireNonNull(c, "c");
    rangeCheckForAdd(index);
    if (c.isEmpty())
      return false;
    // scan the collection to make sure that inserting it at the specified position doesn't violate the sort order
    E lower = index > 0 ? get(index-1) : null;
    E upper = index < size() ? get(index) : null;
    Iterator<? extends E> it = c.iterator();
    for (int i = index; it.hasNext(); i++) {
      E e = it.next();
      // this element should be >= lower/prev and <= upper
      boolean lowerOk = lower == null || e.compareTo(lower) >= 0;
      boolean upperOk = upper == null || e.compareTo(upper) <= 0;
      if (!lowerOk || !upperOk) {
        throw new IllegalArgumentException(illegalInsertionMsg(i, e));
      }
      lower = e;
    }
    boolean modified = delegate.addAll(index, c);
    if (modified)
      modCount++;
    return modified;
  }

  /**
   * Returns {@code true} if the specified element is &ge; the current element at the specified index, or if the
   * specified index is lower than 0.
   */
  private boolean isGreaterThanOrEqualTo(E element, int index) {
    return index < 0 || element.compareTo(delegate.get(index)) >= 0;
  }

  /**
   * Returns {@code true} if the specified element is &le; the current element at the specified index, or if the
   * specified index == {@link #size()}
   */
  private boolean isLessThanOrEqualTo(E element, int index) {
    int size = size();
    return index == size || element.compareTo(get(Math.min(index, size-1))) <= 0;
  }

  @Override
  public E remove(int index) {
    modCount++;
    return delegate.remove(index);
  }

  /**
   * Uses {@link Collections#binarySearch(List, Object)} to remove the first occurrence of an element that
   * {@linkplain Comparable#compareTo(Object) compares} as equal to specified element, if it is present.
   * If this list does not contain such an the element, it is unchanged.
   * <p>
   * More formally, removes the element with the lowest index {@code i} such that {@code get(i).compareTo(o) == 0}
   * (if such an element exists).  Returns <tt>true</tt> if this list contained such an element
   * (or equivalently, if this list changed as a result of the call).
   * <p>
   * <b>Note:</b> This implementation differs from the specification of {@link List#remove(Object)}
   * if the {@link Comparable} type of the elements defines a natural ordering that is
   * inconsistent with {@link Object#equals(Object) equals}.
   *
   * @param o {@inheritDoc}
   * @return {@inheritDoc}
   *
   * @throws ClassCastException if the type of the specified element is incompatible with this list
   * @throws NullPointerException if the specified element is null
   * @see #removeAll(Object)
   */
  @Override
  public boolean remove(Object o) {
    int i = indexOf(o);
    if (i < 0)
      return false;
    modCount++;
    delegate.remove(i);
    return true;
  }

  /**
   * Returns the index of the first occurrence of an element that {@linkplain Comparable#compareTo(Object) compares}
   * as equal to specified element, or -1 if this list does not contain such an element.
   * <p>
   * This implementation uses {@link Collections#binarySearch(List, Object)}, potentially followed by a linear
   * scan of all matching elements to find the first occurrence
   * (since {@link Collections#binarySearch} doesn't guarantee which one it returns returned).
   * Therefore, the worst-case time complexity of this method is O(log(n) + n), if there are multiple duplicates.
   * <p>
   * <b>Note:</b> This implementation differs from the specification of {@link List#indexOf(Object)}
   * if the {@link Comparable} type of the elements defines a natural ordering that is
   * inconsistent with {@link Object#equals(Object) equals}.
   *
   * @param element {@inheritDoc}
   * @return {@inheritDoc}
   * @throws ClassCastException if the type of the specified element is incompatible with this list
   * @throws NullPointerException if the specified element is null
   */
  @Override
  @SuppressWarnings("unchecked")
  public int indexOf(@Nonnull Object element) {
    E key = (E)element;
    int index = Collections.binarySearch(delegate, key);
    if (index < 0)
      return -1;  // must return -1 if not found
    // TODO: maybe extract code duplicated in lastIndexOf
    /*
    If found, must return the first occurrence of this element, however,
    Collections.binarySearch doesn't guarantee which one will be returned.
    So we have to examine the neighbors
     */
    return findFirst(key, index);
  }

  /**
   * Returns the index of the last occurrence of an element that {@linkplain Comparable#compareTo(Object) compares}
   * as equal to specified element, or -1 if this list does not contain such an element.
   *
   * Returns the index of the last occurrence of the specified element in this list,
   * or -1 if this list does not contain the element.
   * <p>
   * This implementation uses {@link Collections#binarySearch(List, Object)}, potentially followed by a linear
   * scan of all matching elements to find the first occurrence
   * (since {@link Collections#binarySearch} doesn't guarantee which one it returns returned).
   * Therefore, the worst-case time complexity of this method is O(log(n) + n), if there are multiple duplicates.
   * <p>
   * <b>Note:</b> This implementation differs from the specification of {@link List#lastIndexOf(Object)}
   * if the {@link Comparable} type of the elements defines a natural ordering that is
   * inconsistent with {@link Object#equals(Object) equals}.
   *
   * @param element {@inheritDoc}
   * @return {@inheritDoc}
   * @throws ClassCastException if the type of the specified element is incompatible with this list
   * @throws NullPointerException if the specified element is null
   */
  @Override
  @SuppressWarnings("unchecked")
  public int lastIndexOf(Object element) {
    E key = (E)element;
    int index = Collections.binarySearch(delegate, key);
    if (index < 0)
      return -1;  // must return -1 if not found
    // TODO: maybe extract code duplicated in indexOf
    /*
    If found, must return the last occurrence of this element, however, Collections.binarySearch
    doesn't guarantee which one will be returned, so we have to examine the neighbors
     */
    return findLast(key, index);
  }

  /**
   * Finds the index of the first element that {@linkplain Comparable#compareTo(Object) compares}
   * as equal to specified key, starting the search at the given index.
   *
   * @param key an object from the same equivalence class
   * @param index an index of an element from the same equivalence class
   *   (derived via {@link Collections#binarySearch(List, Object) binarySearch(delegate, key)})
   * @return the index of the first element in the same equivalence class as the given key
   * @see #indexOf(Object)
   */
  private int findFirst(E key, int index) {
    // TODO: maybe extract code duplicated in findLast
    ListIterator<E> it = delegate.listIterator(index);
    int cursor = index;
    while (it.hasPrevious()) {
      E previous = it.previous();
      if (previous.compareTo(key) < 0)
        return cursor;
      else
        cursor = it.nextIndex();
    }
    return cursor;
  }

  /**
   * Finds the index of the last element that {@linkplain Comparable#compareTo(Object) compares}
   * as equal to specified key, starting the search at the given index.
   *
   * @param key an object from the same equivalence class
   * @param index the index of any existing list element in the same equivalence class
   *   (derived via {@link Collections#binarySearch(List, Object) binarySearch(delegate, key)})
   * @return the index of the last element in the same equivalence class as the given key
   * @see #lastIndexOf(Object)
   */
  private int findLast(E key, int index) {
    // TODO: maybe extract code duplicated in findFirst
    ListIterator<E> it = delegate.listIterator(index);
    int cursor = index;
    while (it.hasNext()) {
      E next = it.next();
      if (next.compareTo(key) > 0)
        return cursor;
      else
        cursor = it.previousIndex();
    }
    return cursor;
  }

  // Bulk Operations


  @Override
  public void clear() {
    modCount++;
    delegate.clear();
  }

  @Override
  public boolean contains(Object object) {
    return indexOf(object) >= 0;
  }

  @Override
  public List<E> subList(int fromIndex, int toIndex) {
    // TODO: make sure the default subList implementation preserves the ordering guarantees of this list;
    //  otherwise, create a subclass of SortedList or wrap the default subList with ForwardingList to reject certain operations
//    return new SubList(this, fromIndex, toIndex);
    return super.subList(fromIndex, toIndex);
    /* TODO: fix undesired behaviors in the AbstractList.subList implementation:
         - SubList.add(E) results in a call to add(size(), E), which is not what we want
           ** actually, this is probably ok, since the add(size(), E) will be called on the SubList, which
           would still insert the element in sorted order -- but not sure, should test this assumption!
     */
  }

  /**
   * Returns a view of the portion of this list where each element
   * {@linkplain Comparable#compareTo(Object) compares} as equal to specified element.
   * <p>
   * If found, the returned sublist will cover all the contained elements in the same
   * <i>equivalence class</i> as the specified element, and will be the same as
   * {@code subList(indexOf(key}, lastIndexOf(key}+1)}.
   * <p>
   * If not found, the returned sublist will be the same as
   * {@code subList(insertionPoint, insertionPoint)},
   * where the <i>insertionPoint</i> is defined as the point at which the key would be inserted into the list (i.e.
   * the index of the first element greater than the key, or {@code list.size()} if all elements in the list are
   * less than the specified key).  This empty sublist can be used to insert the key into the list.
   *
   * @param key an element that {@linkplain Comparable#compareTo(Object) compares} as equal to all the elements
   *   in the desired sublist
   */
  public List<E> subList(E key) {
    // NOTE: this code partially duplicates ListUtils.findInsertionPoint
    int index = Collections.binarySearch(delegate, requireNonNull(key, "key"));
    if (index < 0) {
      /* The list does not already contain the key: a negative value returned by Collections.binarySearch
         is defined as (-insertionPoint - 1) */
      int insPoint = -(index + 1);
      return subList(insPoint, insPoint);
    }
    /*
     The list already contains one or more elements "equal" to the given key, but since Collections.binarySearch doesn't
     guarantee which one is returned, we have to manually search through the adjacent indices to find the last one
     */
    return subList(findFirst(key, index), findLast(key, index)+1);
  }

  /**
   * Removes all elements that {@linkplain Comparable#compareTo(Object) compare} as equal to specified object.
   * In other words, removes all elements in the same <i>equivalence class</i> as the object.
   * <p>
   * This method is equivalent to:
   * <pre>
   *   {@link #subList(Comparable) subList(o)}.{@link #clear()}
   * </pre>
   *
   * @param o an object from the same equivalence class as the elements to be removed
   * @return {@code true}  if this list changed as a result of the call
   * @throws ClassCastException if the type of the specified element is incompatible with this list
   * @throws NullPointerException if the specified element is null
   * @see #remove(Object)
   * @see #subList(Comparable)
   */
  public boolean removeAll(Object o) {
    @SuppressWarnings("unchecked")
    E key = (E)o;
    int i = indexOf(key);
    if (i < 0)
      return false;  // not found
    ListIterator<E> it = delegate.listIterator(i);
    while (it.hasNext()) {
      E next = it.next();
      if (next.compareTo(key) <= 0) {
        assert next.compareTo(key) == 0;  // should be in same equivalence class
        it.remove();
      }
      else
        break;  // advanced past the last element the equivalence class
    }
    return true;
  }

  /**
   * Removes from this list all of its elements that are contained in the specified collection.
   * <p>
   * This implementation is the same as {@link AbstractCollection#removeAll(Collection)}, in that it removes all
   * elements {@code e} for which {@link Collection#contains(Object) c.contains(e)} is {@code true}.
   * In other words, it uses {@link Object#equals(Object)} rather than {@link Comparable#compareTo(Object) compareTo(Object)}.
   * <p>
   * For an implementation that's based on {@link Comparable#compareTo(Object) compareTo(Object)}, see the custom
   * {@link SortedList} method {@link #removeAll(Object)}.  For example, the following code removes all elements
   * that {@linkplain Comparable#compareTo(Object) compare} as equal to any element in the specified collection:
   * <pre>
   *   c.forEach(sortedList::removeAll);
   * </pre>
   */
  @Override
  public boolean removeAll(Collection<?> c) {
    // overriding this method to provide custom javadoc
    return super.removeAll(c);
  }

  /*
   TODO: make sure we don't have to override iterators (probably fine, since the AbstractList iterators simply delegate all ops to the list)
     - same thing with subList
   TODO: override spliterator (give it the IMMUTABLE characteristic)
   Other ideas:
     - perhaps could provide more efficient implementations of some bulk methods when the arg is also a SortedList
  */


  @Override
  public void sort(Comparator<? super E> c) {
    throw new UnsupportedOperationException(getClass().getName()
        + " is sorted according to the natural ordering of the elements; can't re-sort with an arbitrary Comparator");
  }

  @Nonnull
  @Override
  public Object[] toArray() {
    return delegate.toArray();
  }

  @Nonnull
  @Override
  public <T> T[] toArray(@Nonnull T[] a) {
    //noinspection SuspiciousToArrayCall
    return delegate.toArray(a);
  }

  /**
   * @return the simple name of this class, to be used in UOE exception messages
   */
  @Nonnull
  private String getName() {
    return getClass().getSimpleName();
  }


  private void rangeCheck(int index) {
    if (index < 0 || index >= delegate.size())
      throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
  }

  /**
   * A version of rangeCheck used by add and addAll.
   */
  private void rangeCheckForAdd(int index) {
    if (index < 0 || index > delegate.size())
      throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
  }

  private String outOfBoundsMsg(int index) {
    return "Index: "+index+", Size: "+delegate.size();
  }


}
