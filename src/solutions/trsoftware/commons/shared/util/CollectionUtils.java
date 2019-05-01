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
import solutions.trsoftware.commons.shared.util.callables.Function1;
import solutions.trsoftware.commons.shared.util.compare.ComparisonOperator;
import solutions.trsoftware.commons.shared.util.iterators.ArrayIterator;
import solutions.trsoftware.commons.shared.util.iterators.ChainedIterator;

import java.util.*;

/**
 * Date: Sep 16, 2008 Time: 5:36:41 PM
 *
 * @author Alex
 */
public class CollectionUtils {

  /**
   * @return {@code true} iff the given collection is either {@code null} or empty.
   */
  public static boolean isEmpty(Collection c) {
    return c == null || c.isEmpty();
  }

  /**
   * @return {@code true} iff the given collection is either {@code null} or empty.
   */
  public static boolean isEmpty(Map m) {
    return m == null || m.isEmpty();
  }

  /**
   * Turns any collection into a database!
   *
   * @return a mapping of all items in the collection matching each key (as defined by keyGetter).
   */
  public static <K, V> Map<K, List<V>> buildIndex(Collection<V> items, Function1<V, K> keyGetter) {
    Map<K, List<V>> map = new HashMap<K, List<V>>(items.size());
    for (V item : items) {
      K key = keyGetter.call(item);
      List<V> entriesMatchingKey;
      if (map.containsKey(key))
        entriesMatchingKey = map.get(key);
      else {
        entriesMatchingKey = new ArrayList<V>();
        map.put(key, entriesMatchingKey);
      }
      entriesMatchingKey.add(item);
    }
    return map;
  }

  /**
   * Similar to the Groovy collect method.  Returns the results of the given
   * transformation on the collection.  For example, can be used to reduce
   * a collection of person objects to a list of name strings.
   *
   * @return a mapping of all items in the collection matching each key (as defined by keyGetter).
   * @deprecated in Java 1.8+ can replace this method with {@code items.stream().map(transformation).collect(Collectors.toList())}
   */
  public static <I, O> List<O> collect(Collection<I> items, Function1<I, O> transformation) {
    List<O> outputs = new ArrayList<O>(items.size());
    for (I item : items) {
      outputs.add(transformation.call(item));
    }
    return outputs;
  }


  /**
   * Removes all the entries matching predicate from the given collection.
   * @return the same map to allow method chaining.
   * @deprecated Java 1.8+ provides {@link Collection#removeIf(java.util.function.Predicate)} for this purpose
   */
  public static <V> Collection<V> removeMatchingEntries(Collection<V> collection, Predicate<V> predicate) {
    Iterator<V> iter = collection.iterator();
    while (iter.hasNext()) {
      V entry = iter.next();
      if (predicate.apply(entry))
        iter.remove();
    }
    return collection;
  }

  /**
   * @return a new list containing all the elements returned by the given iterator
   */
  public static <T> ArrayList<T> asList(Iterator<T> it) {
    ArrayList<T> ret = new ArrayList<T>();
    while (it.hasNext())
      ret.add(it.next());
    return ret;
  }

  /**
   * @return a new list containing all the elements returned by the iterator of the given {@link Iterable}.
   */
  public static <T> ArrayList<T> asList(Iterable<T> iterable) {
    return asList(iterable.iterator());
  }

  public static <T, C extends Collection<T>> C addAll(C target, Iterator<T> it) {
    while (it.hasNext())
      target.add(it.next());
    return target;
  }

  public static <T, C extends Collection<T>> C addAll(C target, Iterable<T> it) {
    return addAll(target, it.iterator());
  }

  @SafeVarargs
  public static <T, C extends Collection<T>> C addAll(C target, T... items) {
    Collections.addAll(target, items);
    return target;
  }

  /**
   * @return the last element of the given iterator, or null if the iterator
   * has no elements.
   */
  public static <T> T last(Iterator<T> it) {
    T last = null;
    while (it.hasNext()) {
      last = it.next();
    }
    return last;
  }

  /**
   * @return the last element of the given iterator, or null if the iterator
   * has no elements.
   */
  public static <T> T last(Iterable<T> it) {
    return last(it.iterator());
  }

  /**
   * @return A String array containing the {@link String#valueOf(Object)} representation of each item in the given collection.
   */
  public static String[] toStringArray(Collection items) {
    String[] ret = new String[items.size()];
    Iterator it = items.iterator();
    for (int i = 0; it.hasNext(); i++) {
      ret[i] = String.valueOf(it.next());
    }
    return ret;
  }

  /**
   * @return true iff collection contains any items in the query.
   */
  public static <T> boolean containsAny(Collection<T> collection, Collection<T> query) {
    for (T q : query) {
      if (collection.contains(q))
        return true;
    }
    return false;
  }

  /**
   * @return an iterable over all the elements in all the given iterables
   */
  @SafeVarargs
  public static <A extends Iterable<T>, T> Iterable<T> concatIter(final A... iterables) {
    return new Iterable<T>() {
          @Override
          public Iterator<T> iterator() {
            return ChainedIterator.fromIterables(new ArrayIterator<A>(iterables));
          }
        };
  }

  // TODO: unit test this version

  /**
   * @return an iterable over all the elements in all the nested iterables.
   */
  public static <A extends Iterable<T>, T> Iterable<T> concatIter(final Collection<A> iterables) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return ChainedIterator.fromIterables(iterables);
      }
    };
  }

  /**
   * @return A list containing all the elements from the given collections.
   */
  public static <T> List<T> concat(final Iterable<T> i1, Iterable<T> i2) {
    return asList(concatIter(i1, i2));
   }

  /**
   * @return A list containing all the elements from the given collections.
   */
  public static <T> List<T> concat(final Iterable<T>... iterables) {
    return asList(concatIter(iterables));
   }

  /**
   * @return the first element returned by the given collection's iterator
   * @throws NoSuchElementException if the collection is empty
   */
  public static <T> T first(Iterable<T> iterable) {
    return iterable.iterator().next();
  }


  /**
   * @return the subset of elements satisfying the given predicate
   */
  public static <T> ArrayList<T> filter(Iterable<T> elements, Predicate<T> predicate) {
    ArrayList<T> ret = new ArrayList<T>();
    for (T elt : elements) {
      if (predicate.apply(elt))
        ret.add(elt);
    }
    return ret;
  }

  /**
   * @param collection a collection of mutually-comparable elements
   * @return a new list containing the same elements as the given collection, but sorted with {@link Collections#sort(List)}
   */
  public static <T extends Comparable<? super T>> ArrayList<T> sorted(Collection<T> collection) {
    ArrayList<T> ret = new ArrayList<>(collection);
    Collections.sort(ret);
    return ret;
  }

  /**
   * @param collection a collection of mutually-comparable elements
   * @param cmp the comparator to determine the order of the result.
   * A {@code null} value indicates that the elements' <i>natural ordering</i> should be used.
   * @return a new list containing the same elements as the given collection, but sorted with {@link Collections#sort(List, Comparator)}
   */
  public static <T> ArrayList<T> sorted(Collection<T> collection, Comparator<? super T> cmp) {
    ArrayList<T> ret = new ArrayList<>(collection);
    Collections.sort(ret, cmp);
    return ret;
  }

  /**
   * Prints the elements of the given collection sorted according to its <i>natural ordering</i>, with the
   * corresponding comparison operators between them.
   * <p>
   * <b>Example</b>: given {@code [5, 2, 4, 3, 3, 1]}, returns {@code "1 < 2 < 3 == 3 < 4 < 5"}
   * @param collection a collection of mutually-comparable elements
   * @return a string representation of the total ordering of the given collection
   */
  public static <T extends Comparable<? super T>> String printTotalOrdering(Collection<T> collection) {
    if (collection.isEmpty())
      return "";
    ArrayList<T> ordering = sorted(collection);
    StringBuilder out = new StringBuilder();
    out.append(ordering.get(0));
    for (int i = 1; i < ordering.size(); i++) {
        out.append(' ').append(ComparisonOperator.lookup(ordering.get(i-1), ordering.get(i)))
            .append(' ').append(ordering.get(i));
    }
    return out.toString();
  }

}
