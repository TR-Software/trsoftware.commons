package solutions.trsoftware.commons.server.util.reflect;

import solutions.trsoftware.commons.client.util.iterators.IndexedIterator;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

/**
 * Iterates the elements of any array object, regardless of its component type, by using the reflection helper
 * class {@link Array}.
 *
 * This class is useful when the array value is obtained by reflection (its type is unknown at compile-time) which
 * means that {@link Arrays#asList(Object[])} can't be used to adapt an array to a {@link Collection}.
 *
 * @author Alex, 4/1/2016
 */
public class ArrayIterator<T> extends IndexedIterator<T> {

  private final Object arr;

  public ArrayIterator(Object arr) {
    super(Array.getLength(arr));
    this.arr = arr;
  }

  public ArrayIterator(Object arr, int start) {
    super(start, Array.getLength(arr));
    this.arr = arr;
    get(start); // trigger an ArrayIndexOutOfBoundsException if the starting index isn't valid
  }

  @Override
  protected T get(int idx) {
    return (T)Array.get(arr, idx);
  }

}
