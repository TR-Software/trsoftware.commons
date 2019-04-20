/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.shared.util;

import com.google.common.base.Predicate;

import java.util.*;

/**
 * @author Alex
 */
public class ListUtils {

  /**
   * This method exists because the types returned by {@link List#subList(int, int)} are not serializable by GWT.
   *
   * @param fromIndex low endpoint (inclusive) of the subList
   * @param toIndex high endpoint (exclusive) of the subList
   * @return a new {@link ArrayList} that contains the elements in the specified range of the given list.
   * @throws IndexOutOfBoundsException for an illegal endpoint index value
   *         (<tt>fromIndex &lt; 0 || toIndex &gt; size ||
   *         fromIndex &gt; toIndex</tt>)
   */
  public static <T> List<T> subList(List<T> list, int fromIndex, int toIndex) {
    // NOTE: this implementation only runs fast on random-access lists
    // (this is true for all lists in javascript code compiled with GWT, since it uses JS arrays for all list implementations)
    ArrayList<T> ret = new ArrayList<T>();
    for (int i = fromIndex; i < toIndex; i++) {
      ret.add(list.get(i));
    }
    return ret;
  }

  /**
   * Returns a new list that contains the elements in the specified range of the given list, but doesn't throw
   * {@link IndexOutOfBoundsException} if the specified range is invalid, in which case we use a valid range that
   * most closely resembles the given range.
   *
   * Examples:
   * <pre>
   *   // TODO: give some examples
   * </pre>
   *
   * @param list the original list
   * @param fromIndex low endpoint (inclusive) of the subList
   * @param toIndex high endpoint (exclusive) of the subList
   * @return a view of the specified range within this list
   */
  public static <T> List<T> safeSubList(List<T> list, int fromIndex, int toIndex) {
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
    return subList(list, fromIndex, toIndex);
  }

  /**
   * Inserts an element into the given list, in ascending order.
   * @param list should already be sorted
   * @return A reference to the given list, to allow method chaining.
   */
  public static <T extends Comparable> List<T> insertInOrder(List<T> list, T newElement) {
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
   * @return {@code true} iff the given list is sorted in ascending order (as defined by its comparator).
   */
  public static <T extends Comparable<T>> boolean isSorted(List<T> list) {
    T lastElt = null;
    for (T elt : list) {
      if (lastElt != null) {
        if (elt.compareTo(lastElt) <= 0)
          return false;
      }
      lastElt = elt;
    }
    return true;
  }


  /**
   * Returns a standard {@link ArrayList} with the given elements (in contrast to {@link Arrays#asList},
   * which returns a very limited implementation of {@link List}).
   */
  @SafeVarargs
  public static <T> ArrayList<T> arrayList(T... a) {
    return new ArrayList<T>(Arrays.asList(a));
  }

  /**
   * @return the last element of the given list
   * @throws IndexOutOfBoundsException if the list is empty
   */
  public static <T> T last(List<T> list) {
    return list.get(list.size()-1);
  }

  /**
   * Including this method for symmetry with getLast.
   *
   * @return the first element of the given list
   * @throws IndexOutOfBoundsException if the list is empty
   */
  public static <T> T first(List<T> lst) {
    return lst.get(0);
  }

  /** @return a new array list containing the elements of the given collection, in reverse order */
  public static <T> ArrayList<T> reversedCopy(Collection<T> inputs) {
    ArrayList<T> copy = new ArrayList<T>(inputs);
    Collections.reverse(copy);
    return copy;
  }

  /** Clears the given list and fills it with {@code n} occurrences of {@code value}. */
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
  public static boolean isEmpty(List lst) {
    return lst == null || lst.isEmpty();
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
   * @return the subset of elements satisfying the given predicate
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
}
