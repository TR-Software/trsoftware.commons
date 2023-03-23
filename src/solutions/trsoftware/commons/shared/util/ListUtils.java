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

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
   * Inserts an element into the given list, in ascending order.
   * @param list should already be sorted
   * @return A reference to the given list, to allow method chaining.
   */
  public static <T extends Comparable<T>> List<T> insertInOrder(List<T> list, T newElement) {
    /*
     * TODO: improve performance using Collections.binarySearch when list has non-trivial length
     * Our current algorithm is O(n): uses a linear scan of the list
     * We can achieve O(log n) with a binary search to find the proper insertion point in the list,
     * using an algorithm similar to Python's bisect module (https://docs.python.org/2.7/library/bisect.html)
     * Should be able to use Collections.binarySearch, which returns (-(insertion point) - 1) when key not found
     * @see https://stackoverflow.com/questions/2945017/javas-equivalent-to-bisect-in-python
     * @see https://www.geeksforgeeks.org/collections-binarysearch-java-examples/
     *
     * NOTE: could also roll our own "TreeSortedList" data structure using Guava's TreeMultiset for this (not random access),
     * or "SortedArrayList" that uses the aforementioned binarySearch method (random access).
     * @see https://stackoverflow.com/questions/8725387/why-is-there-no-sortedlist-in-java
     */
    ListIterator<T> listIterator = list.listIterator();
    int index = -1;
    while (listIterator.hasNext()) {
      T currentElement = listIterator.next();
      if (currentElement.compareTo(newElement) > 0) {
        // insert before this element
        index = listIterator.previousIndex();
        break;
      }
    }
    if (index == -1)
      index = list.size();  // this element goes at the end of the list

    list.add(index, newElement);  
    return list;
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
   */
  public static <T> void trimTail(List<T> list, int maxSize) {
    assert maxSize >= 0;
    if (list.size() > maxSize) {
      list.subList(maxSize, list.size()).clear();
    }
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
