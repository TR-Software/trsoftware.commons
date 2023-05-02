/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.JavaScriptException;
import solutions.trsoftware.commons.shared.util.collections.FluentList;
import solutions.trsoftware.commons.shared.util.collections.SortedList;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * @author Alex
 * @see FluentList
 */
public class ListUtils {

  /**
   * Copies the specified range of the given list into a new list.
   * <p>
   * This method was originally created because the views returned by {@link List#subList(int, int)}
   * were not serializable by GWT, but is still valid because many implementations of {@link List#subList}
   * (e.g. {@link ArrayList.SubList}) are not serializable.
   *
   * @param fromIndex low endpoint (inclusive) of the subList
   * @param toIndex high endpoint (exclusive) of the subList
   * @param newListSupplier used to create the destination list; could use a method reference to a list constructor
   * @return a <i>new</i> {@link ArrayList} that contains the elements in the specified range of the given list;
   * not a <i>view</i> (modifications do not propagate to the original list and vice-versa)
   * @throws IndexOutOfBoundsException for an illegal endpoint index value
   *         (<tt>fromIndex &lt; 0 || toIndex &gt; size ||
   *         fromIndex &gt; toIndex</tt>)
   * @see FluentList#subList(int, int)
   * @see FluentList#subList(int)
   */
  public static <E, L extends List<E>> L copyOfRange(List<E> list, int fromIndex, int toIndex, Supplier<L> newListSupplier) {
    // TODO: rename this method to copyOfRange, since it most-closely resembles Arrays#copyOfRange(Object[], int, int) rather than List#subList(int, int)
    // NOTE: this implementation only runs fast on random-access lists
    // (this is true for all lists in javascript code compiled with GWT, since it uses JS arrays for all list implementations)
    L ret = newListSupplier.get();
    for (int i = fromIndex; i < toIndex; i++) {
      ret.add(list.get(i));
    }
    return ret;
  }

  /**
   * Copies the specified range of the given list into a new {@link ArrayList}.
   *
   * @see #copyOfRange(List, int, int, Supplier)
   */
  public static <E> List<E> copyOfRange(List<E> list, int fromIndex, int toIndex) {
    return copyOfRange(list, fromIndex, toIndex, ArrayList::new);
  }

  /**
   * Returns a sublist that contains the elements in the specified range of the given list, but doesn't throw
   * {@link IndexOutOfBoundsException} if the specified range is invalid, in which case we use a valid range that
   * most closely resembles the given range.
   * <p>
   * Examples:
   * <pre>{@code
   *   List<Integer> list = Arrays.asList(1,2,3,4,5);
   *   assertEquals(list, safeSubList(list, -50, 50));
   *   assertEquals(Collections.emptyList(), safeSubList(list, -50, -1));
   *   assertEquals(Arrays.asList(3,4,5), safeSubList(list, 2, 50));
   * }</pre>
   *
   * @param list the original list
   * @param fromIndex low endpoint (inclusive) of the subList
   * @param toIndex high endpoint (exclusive) of the subList
   * @return a valid sublist of the given list that most-closely resembles the given range
   * @see #copyOfRange(List, int, int)
   */
  public static <E> List<E> safeSubList(List<E> list, int fromIndex, int toIndex) {
    // fix the range bounds, if needed
    int size = list.size();
    if (fromIndex < 0)
      fromIndex = 0;
    if (fromIndex > size)
      fromIndex = size;
    if (toIndex > size)
      toIndex = size;
    if (toIndex < fromIndex)
      toIndex = fromIndex;
    return list.subList(fromIndex, toIndex);
  }

  /**
   * Same as {@link #safeSubList(List, int, int)}, but returns a new list rather than a view of the original list.
   *
   * @param list the original list
   * @param fromIndex low endpoint (inclusive) of the subList
   * @param toIndex high endpoint (exclusive) of the subList
   * @return copy of a valid range within the the given list that most-closely resembles the given bounds
   * @see #copyOfRange(List, int, int)
   */
  public static <E> ArrayList<E> safeCopyOfRange(List<E> list, int fromIndex, int toIndex) {
    return new ArrayList<>(safeSubList(list, fromIndex, toIndex));
  }

  /**
   * Uses {@link Collections#binarySearch(List, Object)} to insert the given element into the given sorted list,
   * at the appropriate position (as defined by the elements' {@linkplain Comparable natural ordering}).
   *
   * @param list a sorted list without any {@code null} elements
   *   (failing to ensure either condition could result in non-deterministic behavior)
   * @param newElement the element to be added to the list
   * @return A reference to the given list, to allow method chaining.
   * @throws NullPointerException if the key is {@code null} or the list contains {@code null} elements (although
   *    in the latter case, an exception is not guaranteed, depending on whether any {@code null} elements end up being
   *    used as mid-points in the binary search)
   * @see #findInsertionPoint(List, Comparable)
   * @see SortedList
   */
  public static <T extends Comparable<T>> List<T> insertInOrder(List<T> list, T newElement) {
    int insertionPoint = findInsertionPoint(list, newElement);
    list.add(insertionPoint, newElement);
    return list;
  }

  /**
   * Uses {@link Collections#binarySearch(List, Object)} to insert the given element into a capacity-restricted
   * sorted list at the appropriate position (as defined by the elements' {@linkplain Comparable natural ordering}).
   * <p>
   * If the {@linkplain #findInsertionPoint(List, Comparable) insertion point} is greater than {@code maxSize}, the list
   * will not be modified.  If inserted, the list will be truncated to {@code maxSize}, if needed.
   * <p>
   * This operation is useful for maintaining data structures such as leaderboards of top N scores in a game.
   *
   * @param list a sorted list without any {@code null} elements
   *   (failing to ensure either condition could result in non-deterministic behavior)
   * @param newElement the element to be added to the list
   * @param maxSize the maximum capacity of the list
   * @return {@code true} if the list was modified.
   * @throws NullPointerException if the key is {@code null} or the list contains {@code null} elements (although
   *    in the latter case, an exception is not guaranteed, depending on whether any {@code null} elements end up being
   *    used as mid-points in the binary search)
   * @see #findInsertionPoint(List, Comparable)
   * @see SortedList
   */
  public static <T extends Comparable<T>> boolean insertInOrder(List<T> list, T newElement, int maxSize) {
    int insertionPoint = findInsertionPoint(list, newElement);
    if (insertionPoint < maxSize) {
      trimTail(list, maxSize - 1);
      /* Note: As a perf optimization, we're truncating the list prior to the insertion; this reduces the number of
         elements that need to be shifted to the right (e.g. if we're dealing with an ArrayList)
       */
      list.add(insertionPoint, newElement);
      return true;
    }
    return false;
  }

  /**
   * Uses {@link Collections#binarySearch(List, Object)} to find the insertion point of the given key into the given
   * sorted list.
   * <p>
   * The <i>insertion point</i> is defined as the point at which the key would be inserted into the list:
   * the index of the first element greater than the key, or {@code list.size()} if all elements in the list are
   * less than the specified key.
   * <p>
   * If the list already contains one or more elements "equal" to the given key, the insertion point will be the next
   * index following the last of such elements.  Such a scenario requires a sequential search over these adjacent
   * elements, making the time complexity of this method O(n) instead of O(log n).
   * <p>
   * The list will not be modified by this method, and it's up to the caller to perform the actual insertion at the
   * returned index, if desired.
   *
   * @param list a sorted list without any {@code null} elements
   *   (failing to ensure either condition could result in non-deterministic behavior)
   * @param key the key to be inserted
   * @return the index of the first element greater than the key, or {@code list.size()}
   *         if all elements in the list are less than the specified key.
   * @throws NullPointerException if the key is {@code null} or the list contains {@code null} elements (although
   *    in the latter case, an exception is not guaranteed, depending on whether any {@code null} elements end up being
   *    used as mid-points in the binary search)
   * @see #insertInOrder(List, Comparable)
   * @see SortedList
   */
  public static <T extends Comparable<T>> int findInsertionPoint(List<T> list, T key) {
    int index = Collections.binarySearch(list, requireNonNull(key, "key"));
    if (index < 0) {
      /* The list does not contain the key: a negative value returned by Collections.binarySearch
         is defined as (-insertionPoint - 1) */
      return -(index + 1);
    }
    /*
     The list already contains one or more elements "equal" to the given key, but since Collections.binarySearch doesn't
     guarantee which one is returned, we have to manually search through the adjacent indices to find the last one
     */
    ListIterator<T> it = list.listIterator(index);
    while (it.hasNext()) {
      T next = it.next();
      if (next.compareTo(key) > 0)
        return it.previousIndex();
    }
    // didn't find an element greater than the key; will return list.size()
    return it.nextIndex();
  }

  /**
   * @return {@code true} iff the given list is sorted in ascending order (as defined by the natural ordering of the elements).
   */
  public static <T extends Comparable<T>> boolean isSorted(List<T> list) {
    return CollectionUtils.isSorted(list);
  }


  /**
   * Returns a new (mutable) instance of {@link ArrayList} containing the given elements (in contrast with {@link Arrays#asList},
   * which returns a very limited implementation of {@link List}).
   * @see Arrays#asList(Object[])
   * @see com.google.common.collect.Lists#newArrayList(Object[])
   */
  @SafeVarargs
  public static <T> ArrayList<T> arrayList(T... elements) {
    ArrayList<T> ret = new ArrayList<>();
    Collections.addAll(ret, elements);
    return ret;
  }

  /**
   * Returns a new instance of {@link LinkedList} containing the given elements.
   */
  @SafeVarargs
  public static <T> LinkedList<T> linkedList(T... elements) {
    LinkedList<T> ret = new LinkedList<>();
    Collections.addAll(ret, elements);
    return ret;
  }

  /**
   * Returns a new instance of {@link ArrayList} containing all the elements of the given lists, in the same order.
   * @param inputs the lists to concatenate
   * @return a new list representing the concatenation of the inputs
   */
  @SafeVarargs
  public static <T> ArrayList<T> concat(List<T>... inputs) {
    ArrayList<T> ret = new ArrayList<>();
    // NOTE: ArrayList.addAll is more efficient than Collections.addAll
    Arrays.stream(inputs).forEach(ret::addAll);
    return ret;
  }

  /**
   * Returns a new instance of {@link ArrayList} containing all the elements of the given lists, in the same order.
   * @param inputs the lists to concatenate
   * @return a new list representing the concatenation of the inputs
   */
  public static <T> ArrayList<T> concat(List<T> first, List<T> second) {
    // this provides a more efficient implementation of concat(java.util.List[]) when there are only 2 inputs
    // by ensuring that the ArrayList won't have to grow
    ArrayList<T> ret = new ArrayList<>(first.size() + second.size());
    ret.addAll(first);
    ret.addAll(second);
    return ret;
  }

  /**
   * Adds the given element to the given list (using {@link List#add(Object)}) and returns the same list instance
   * to facilitate call chaining.
   *
   * @param inputs the lists to concatenate
   * @return a new list representing the concatenation of the inputs
   */
  public static <L extends List<E>, E> L append(L list, E element) {
    list.add(element);
    return list;
  }

  /**
   * @return the last element of the given list
   * @throws IndexOutOfBoundsException if the list is empty
   */
  public static <T> T last(@Nonnull List<T> list) {
    return list.get(list.size()-1);
  }

  /**
   * Including this method for symmetry with {@link #last(List)}.
   *
   * @return the first element of the given list
   * @throws IndexOutOfBoundsException if the list is empty
   */
  public static <T> T first(List<T> lst) {
    return lst.get(0);
  }

  /**
   * @return a new list containing the same elements as the given collection, but in reversed order
   * @see Lists#reverse(List)
   * @see Collections#reverse(List)
   */
  public static <E> ArrayList<E> reversedCopy(Collection<E> inputs) {
    return CollectionUtils.reversedCopy(inputs);
  }

  /**
   * Clears the given list and fills it with {@code n} occurrences of {@code value}.
   * <p>
   * NOTE: unlike {@link Collections#fill(List, Object)}, this method always returns a list that contains
   * exactly {@code n} elements.
   * @return the same list instance that was passed in (for chaining)
   */
  public static <L extends List<T>, T> L fill(L list, int n, T value) {
    list.clear();
    for (int i = 0; i < n; i++) {
      list.add(value);
    }
    return list;
  }

  /**
   * @return {@code true} iff the given list is either {@code null} or empty
   */
  public static boolean isEmpty(List<?> list) {
    return list == null || list.isEmpty();
  }

  /**
   * Checks that the given list is neither {@code null} nor empty and throws an {@link IllegalArgumentException} if it is.
   * This method is designed primarily for doing parameter validation, similar to {@link Objects#requireNonNull(Object)}.
   *
   * @param list the list to check
   * @return {@code list} if neither {@code null} nor empty
   * @throws IllegalArgumentException if {@code list} is either {@code null} or empty
   * @see #requireNonEmpty(List, String)
   */
  public static <L extends List<?>> L requireNonEmpty(L list) throws IllegalArgumentException {
    if (isEmpty(list))
      throw new IllegalArgumentException();
    return list;
  }

  /**
   * Checks that the given list is neither {@code null} nor empty and throws a customized
   * {@link IllegalArgumentException} if it is.  This method is designed primarily for doing parameter validation,
   * similar to {@link Objects#requireNonNull(Object, String)}.
   *
   * @param list the list to check
   * @param message detail message to be used in the event that an {@link IllegalArgumentException} is thrown
   * @param <L> the type of the list
   * @return {@code list} if neither {@code null} nor empty
   * @throws IllegalArgumentException if {@code list} is either {@code null} or empty
   */
  public static <L extends List<?>> L requireNonEmpty(L list, String message) throws IllegalArgumentException {
    if (isEmpty(list))
      throw new IllegalArgumentException(message);
    return list;
  }

  /**
   * @return the size of the given list or {@code 0} if it's {@code null}
   */
  public static int size(List lst) {
    if (lst == null)
      return 0;
    return lst.size();
  }

  /**
   * Equivalent to the following Java 1.8 {@code Stream} operation:
   * <pre>
   *   list.stream().filter(predicate).collect(Collectors.toList());
   * </pre>
   *
   * <i>NOTE: this method is not deprecated because it might have better performance than the above stream example</i>
   *
   * @return a new list containing the elements that satisfy the given predicate
   * @see CollectionUtils#filter(Iterable, Predicate)
   */
  public static <T> ArrayList<T> filter(List<T> list, Predicate<T> predicate) {
    return CollectionUtils.filter(list, predicate);
  }

  /**
   * Removes elements from the tail of the list until its size is &le; {@code maxSize}.
   *
   * @param maxSize the max number of elements to keep in the original list
   * @return {@code true} iff the list was modified
   */
  public static boolean trimTail(List<?> list, int maxSize) {
    assert maxSize >= 0;
    int size = list.size();
    if (size > maxSize) {
      // newSize == maxSize+1, can just call remove(maxSize)
      if (size == maxSize + 1) {
        // optimize the most-common use-case: last element of list being evicted; no need to create intermediate subList
        list.remove(maxSize);
      } else {
        // otherwise, going through a subList could be more efficient (e.g. if list is subclass of AbstractList,
        // this can take advantage of a fast AbstractList.removeRange implementation; see ArrayList.removeRange)
        list.subList(maxSize, size).clear();
      }
      assert list.size() == maxSize;
      return true;
    }
    return false;
  }

  /**
   * Throws an {@link IndexOutOfBoundsException} if the given index is not in range for the given list size.
   * This method is useful for client-side GWT code, which might throw a generic {@link JavaScriptException}
   * instead.
   * @param size the size of the list
   * @param index the index to check for being within the list's bounds
   * @return the given index if it's valid
   * @throws IndexOutOfBoundsException if the given index is not in the range {@code [0, size[}
   */
  public static int checkBounds(int size, int index) {
    if (index < 0 || index >= size)
      throw new IndexOutOfBoundsException(Integer.toString(index));
    return index;
  }

  /**
   * Throws an {@link IndexOutOfBoundsException} if the given index is not in range for the given list size.
   * This method is useful for client-side GWT code, which might throw a generic {@link JavaScriptException}
   * instead.
   * @param size the size of the list
   * @param index the index to check for being within the list's bounds
   * @return the given index if it's valid
   * @throws IndexOutOfBoundsException if the given index is not in the range {@code [0, size[}
   */
  public static int checkBounds(List<?> list, int index) {
    return checkBounds(list.size(), index);
  }
}
