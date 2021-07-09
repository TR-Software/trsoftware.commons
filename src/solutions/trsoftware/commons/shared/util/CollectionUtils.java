/*
 * Copyright 2021 TR Software Inc.
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

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.UmbrellaException;
import solutions.trsoftware.commons.shared.util.callables.Function1;
import solutions.trsoftware.commons.shared.util.compare.ComparisonOperator;
import solutions.trsoftware.commons.shared.util.iterators.ArrayIterator;
import solutions.trsoftware.commons.shared.util.iterators.ChainedIterator;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
   * @return {@code true} iff the given map is either {@code null} or empty.
   */
  public static boolean isEmpty(Map m) {
    return m == null || m.isEmpty();
  }

  /**
   * Creates a database-like index for the given collection.
   *
   * @return a mapping of all items in the collection matching each key (as defined by keyGetter).
   * @see java.util.stream.Collectors#toMap
   */
  public static <K, V> Map<K, List<V>> buildIndex(Iterable<V> items, Function<V, K> keyGetter) {
    // TODO: it might make more sense to use a Multimap here
    Map<K, List<V>> map = new HashMap<>();
    for (V item : items) {
      K key = keyGetter.apply(item);
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

  /*
  TODO(11/1/2019): un-deprecate any methods that were deprecated only because there's an equivalent Java 1.8 Stream construct:
  the iterative implementations contained in this class generally have better performance than their Stream equivalents
   */

  /**
   * Similar to the Groovy collect method.  Returns the results of the given
   * transformation on the collection.  For example, can be used to reduce
   * a collection of person objects to a list of name strings.
   *
   * @return a list containing the results of the given transformation applied to the given items
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
   * @return the same collection, for call chaining.
   * @deprecated Java 1.8+ provides {@link Collection#removeIf(java.util.function.Predicate)} for this purpose
   */
  public static <V> Collection<V> removeMatchingEntries(Collection<V> collection, Predicate<V> predicate) {
    collection.removeIf(predicate);
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

  /**
   * Adds all elements from the given iterator to the given collection and returns the collection (for chaining).
   * The iterator will be left exhausted after this.
   *
   * @return the given collection, for chaining
   * @see Iterators#addAll(java.util.Collection, java.util.Iterator)
   */
  public static <T, C extends Collection<T>> C addAll(C target, Iterator<T> it) {
    while (it.hasNext())
      target.add(it.next());
    return target;
  }

  /**
   * Adds all elements from the given iterable to the given collection and returns the collection (for chaining).
   *
   * @return the given collection, for chaining
   * @see Iterables#addAll(Collection, Iterable)
   */
  public static <T, C extends Collection<T>> C addAll(C target, Iterable<T> it) {
    return addAll(target, it.iterator());
  }

  /**
   * Adds the given elements to the given collection using {@link Collections#addAll(Collection, Object[])},
   * and returns the collection (for chaining).
   *
   * @return the given collection, for chaining
   * @see Collections#addAll(Collection, Object[])
   */
  @SafeVarargs
  public static <T, C extends Collection<T>> C addAll(C target, T... items) {
    Collections.addAll(target, items);
    return target;
  }

  /**
   * Adds the first {@code n} items generated by the given supplier to the given collection.
   * <p>
   * Equivalent to:
   * <pre>
   *   Stream.generate(supplier).limit(n).forEach(target::add);
   * </pre>
   *
   * @param n the number of times to invoke the supplier (should be a positive integer)
   * @param supplier will be invoked {@code n} times to generate items to add to the collection
   *
   * @return the given collection, for chaining
   * @see java.util.stream.Stream#generate(Supplier)
   */
  public static <T, C extends Collection<T>> C addFromSupplier(C target, int n, Supplier<T> supplier) {
    for (int i = 0; i < n; i++)
      target.add(supplier.get());
    return target;
  }

  /**
   * @return the last element of the given iterator, or null if the iterator
   * has no elements.
   * <p style="color: #0073BF; font-weight: bold;">
   *   TODO: when no elements, returning null is ambiguous; better to either throw {@link NoSuchElementException} or
   *   return some sentinel value passed as an argument ({@link Iterators#getLast(Iterator, Object)}
   * </p>
   * @deprecated use the corresponding methods from Guava's {@link Iterators} instead: they offer better semantics
   * @see Iterators#getLast(Iterator)
   * @see Iterators#getLast(Iterator, Object)
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
   * <p style="color: #0073BF; font-weight: bold;">
   *   TODO: when no elements, returning null is ambiguous; better to either throw {@link NoSuchElementException} or
   *   return some sentinel value passed as an argument ({@link Iterables#getLast(Iterable, Object)}
   * </p>
   * @deprecated use the corresponding methods from Guava's {@link Iterables} instead: they offer better semantics
   * @see Iterables#getLast(Iterable)
   * @see Iterables#getLast(Iterable, Object)
   */
  public static <T> T last(Iterable<T> it) {
    if (it instanceof List) {
      // if it's a List, can do better than iterating over all the elements
      try {
        return ListUtils.last((List<T>)it);
      }
      catch (IndexOutOfBoundsException e) {
        // return null just to support the original contract of this method; but really this is semantically wrong
        // (see deprecation note)
        return null;
      }
    }
    return last(it.iterator());
  }

  /**
   * @return A String array containing the {@link String#valueOf(Object)} representation of each item in the given collection.
   */
  public static String[] toStringArray(Collection<?> items) {
    String[] ret = new String[items.size()];
    Iterator<?> it = items.iterator();
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
   * @deprecated Can use {@link Iterables#concat(Iterable[])} instead
   * @see Iterables#concat(Iterable[])
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
   * @deprecated Can use {@link Iterables#concat(Iterable)} instead
   * @see Iterables#concat(Iterable)
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
   * @deprecated Can use {@link Iterables#concat(Iterable, Iterable)} instead
   * @see Iterables#concat(Iterable, Iterable)
   */
  public static <T> List<T> concat(final Iterable<T> i1, Iterable<T> i2) {
    return asList(concatIter(i1, i2));
   }

  /**
   * @return A list containing all the elements from the given collections.
   * @deprecated Can use {@link Iterables#concat(Iterable[])} instead
   * @see Iterables#concat(Iterable[])
   */
  @SafeVarargs
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
   * Equivalent to the following Java 1.8 {@code Stream} operations:
   * <pre>
   *   // for Collection:
   *   elements.stream().filter(predicate).collect(Collectors.toList());
   *   // for Iterable:
   *   StreamSupport.stream(elements.spliterator(), false)
   *                .filter(predicate).collect(Collectors.toList());
   * </pre>
   *
   * <i>NOTE: this method is not deprecated because it offers better performance than the above examples</i>
   *
   * @return a new list containing the elements that satisfy the given predicate
   */
  public static <T> ArrayList<T> filter(Iterable<T> elements, Predicate<T> predicate) {
    ArrayList<T> ret = new ArrayList<T>();
    for (T elt : elements) {
      if (predicate.test(elt))
        ret.add(elt);
    }
    return ret;
  }

  /**
   * @param collection a collection of mutually-comparable elements
   * @return a new list containing the same elements as the given collection, but sorted with {@link Collections#sort(List)}
   * @see com.google.common.collect.ImmutableList#sortedCopyOf(Iterable)
   */
  public static <T extends Comparable<? super T>> ArrayList<T> sortedCopy(Collection<T> collection) {
    ArrayList<T> ret = new ArrayList<>(collection);
    Collections.sort(ret);
    return ret;
  }

  /**
   * @param collection a collection of mutually-comparable elements
   * @param cmp the comparator to determine the order of the result.
   * A {@code null} value indicates that the elements' <i>natural ordering</i> should be used.
   * @return a new list containing the same elements as the given collection, but sorted with {@link Collections#sort(List, Comparator)}
   * @see com.google.common.collect.ImmutableList#sortedCopyOf(Comparator, Iterable)
   */
  public static <T> ArrayList<T> sortedCopy(Collection<T> collection, Comparator<? super T> cmp) {
    ArrayList<T> ret = new ArrayList<>(collection);
    ret.sort(cmp);
    return ret;
  }

  /**
   * Prints the elements of the given collection sorted according to its <i>natural ordering</i>, with subsequent
   * elements delimited by the corresponding comparison operators.
   * <p>
   * <b>Example</b>: given {@code [5, 2, 4, 3, 3, 1]}, returns {@code "1 < 2 < 3 == 3 < 4 < 5"}
   * @param collection a collection of mutually-comparable elements
   * @return a string representation of the total ordering of the given collection
   */
  public static <T extends Comparable<? super T>> String printTotalOrdering(Collection<T> collection) {
    if (collection.isEmpty())
      return "";
    ArrayList<T> ordering = sortedCopy(collection);
    StringBuilder out = new StringBuilder();
    out.append(ordering.get(0));
    for (int i = 1; i < ordering.size(); i++) {
        out.append(' ').append(ComparisonOperator.describeRelationship(ordering.get(i-1), ordering.get(i)))
            .append(' ').append(ordering.get(i));
    }
    return out.toString();
  }

  /**
   * @return {@code true} iff the given collection is sorted (in the natural ordering of its elements)
   */
  // TODO: create an overloaded version that takes a Comparator?
  public static <T extends Comparable<T>> boolean isSorted(Iterable<T> list) {
    T lastElt = null;
    for (T elt : list) {
      if (lastElt != null) {
        if (elt.compareTo(lastElt) < 0)
          return false;
      }
      lastElt = elt;
    }
    return true;
  }

  /**
   * Creates a copy of the given collection and calls {@link Collections#reverse(List)} on it.
   *
   * @return a new list containing all the elements of the given collection in reverse order
   * @see Lists#reverse(List)
   */
  public static <E> ArrayList<E> reversedCopy(Collection<E> collection) {
    ArrayList<E> reversedList = new ArrayList<>(collection);
    Collections.reverse(reversedList);
    return reversedList;
  }

  /**
   * @param collection the collection to be shuffled
   * @return a new list containing the same elements as the given collection, but shuffled using {@link Collections#shuffle(List)}
   */
  public static <T> ArrayList<T> shuffledCopy(Collection<T> collection) {
    ArrayList<T> ret = new ArrayList<>(collection);
    Collections.shuffle(ret);
    return ret;
  }

  /**
   * @param collection the collection to be shuffled
   * @param rnd the source of randomness to use to shuffle the elements
   * @return a new list containing the same elements as the given collection, but shuffled using {@link Collections#shuffle(List)}
   */
  public static <T> ArrayList<T> shuffledCopy(Collection<T> collection, Random rnd) {
    ArrayList<T> ret = new ArrayList<>(collection);
    Collections.shuffle(ret, rnd);
    return ret;
  }

  /**
   * Similar to {@link Iterable#forEach(Consumer)}, but guarantees that the given function will be applied to all elements,
   * regardless of any exceptions thrown by the function on any particular element.
   * <p>
   * Any individual exceptions will be collected into a single single {@link UmbrellaException},
   * which will be thrown at the end, after all the elements have been processed.
   * @throws UmbrellaException if the function throws an exception for any of the items.
   */
  public static <T> void safeForEach(Iterable<T> items, Consumer<? super T> action) {
    Set<Throwable> caught = null;
    for (T item : items) {
      try {
        action.accept(item);
      } catch (Throwable e) {
        if (caught == null)
          caught = new LinkedHashSet<>();
        caught.add(e);
      }
    }
    if (caught != null)
      throw new UmbrellaException(caught);
  }
}
