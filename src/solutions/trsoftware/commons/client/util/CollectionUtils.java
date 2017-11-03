package solutions.trsoftware.commons.client.util;

import solutions.trsoftware.commons.client.bridge.util.RandomGen;
import solutions.trsoftware.commons.client.util.callables.Function1;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import java.util.*;

/**
 * Date: Sep 16, 2008 Time: 5:36:41 PM
 *
 * @author Alex
 */
public class CollectionUtils {

  public static boolean isEmpty(Collection c) {
    return c == null || c.isEmpty();
  }

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
   * Returns the same map to allow method chaining.
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


  public static <T> ArrayList<T> asList(Iterator<T> it) {
    ArrayList<T> ret = new ArrayList<T>();
    while (it.hasNext())
      ret.add(it.next());
    return ret;
  }

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
  public static <A extends Iterable<T>, T> Iterable<T> concatIter(final A... iterables) {
    return concatIter(Arrays.asList(iterables));
  }

  // TODO: unit test this version

  /**
   * @return an iterable over all the elements in all the nested iterables.
   */
  public static <A extends Iterable<T>, T> Iterable<T> concatIter(final Collection<A> iterables) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return new Iterator<T>() {

          private Iterator<A> cursor = iterables.iterator();
          private Iterator<T> it = getNextIterator();

          private Iterator<T> getNextIterator() {
            while (cursor.hasNext()) {
              Iterator<T> nextIt = cursor.next().iterator();
              if (nextIt.hasNext())
                return nextIt;
              // skip empty iterators
            }
            return null;
          }

          @Override
          public boolean hasNext() {
            return it != null && it.hasNext();
          }

          @Override
          public T next() {
            T result = it.next();
            if (!it.hasNext())
              it = getNextIterator();
            return result;
          }

          @Override
          public void remove() {
            it.remove();
          }
        };
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

  /** Creates an immutable list from any given collection */
  public static <T> List<T> immutableList(Collection<T> src) {
    List<T> srcList;
    if (src instanceof List)
      srcList = (List<T>)src;
    else
      srcList = new ArrayList<T>(src);
    return Collections.unmodifiableList(srcList);
  }

  /**
   * @return the first element returned by the given collection's iterator
   * @throws NoSuchElementException if the collection is empty
   */
  public static <T> T first(Iterable<T> iterable) {
    return iterable.iterator().next();
  }


}
