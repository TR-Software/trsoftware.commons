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

import com.google.gwt.core.client.JavaScriptException;
import solutions.trsoftware.commons.shared.util.function.IntBiFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Date: Nov 7, 2007
 * Time: 8:15:31 PM
 *
 * @author Alex
 */
public class ArrayUtils {

  /**
   * Like {@link Arrays#toString(int[])}, but omits the enclosing brackets and allows using a custom delimiter.
   * @return a string representation of the given array, constructed by joining the elements using the given delimiter
   *
   * @deprecated use {@link StringUtils#join(String, int...)}
   */
  public static String toString(int[] array, String delimiter) {
    return StringUtils.join(delimiter, array);
  }

  /**
   * Linearly scans the array for the given element.
   *
   * @return {@code true} iff found
   * @see com.google.common.primitives.Ints#contains(int[], int)
   */
  public static boolean contains(int[] array, int element) {
    return indexOf(array, element) >= 0;
  }

  /**
   * Linearly scans the array for the given element.
   *
   * @return {@code true} iff found
   */
  public static <T> boolean contains(T[] array, T element) {
    return indexOf(array, element) >= 0;
  }

  /**
   * Linearly scans the given array for the first occurrence of the value {@code target}.
   *
   * @param array the array to be searched
   * @param target the value to find
   * @return the index of the first matching element in the array, or -1 if not found
   */
  public static <T> int indexOf(T[] array, T target) {
    return indexOf(array, target, 0, array.length);
  }

  /**
   * Linearly scans a range of the given array for the first occurrence of the {@code target} element.
   *
   * @param array the array to be searched
   * @param target the value to find
   * @param fromIndex (inclusive) the index of the first element to be searched
   * @param toIndex (exclusive) the index after the last element to be searched
   * @return the index of the first matching element in the array, or -1 if not found
   */
  public static <T> int indexOf(T[] array, T target, int fromIndex, int toIndex) {
    for (int i = fromIndex; i < toIndex; i++) {
      if (Objects.equals(array[i], target))
        return i;
    }
    return -1;
  }

  /**
   * Linearly scans the given array for the first occurrence of the value {@code target}.
   *
   * @param array the array to be searched
   * @param target the value to find
   * @return the index of the first matching element in the array, or -1 if not found
   *
   * @see com.google.common.primitives.Ints#indexOf(int[], int)
   */
  public static int indexOf(int[] array, int target) {
    return indexOf(array, target, 0, array.length);
  }

  /**
   * Linearly scans a range of the given array for the first occurrence of the value {@code target}.
   *
   * @param array the array to be searched
   * @param target the value to find
   * @param fromIndex the index of the first element (inclusive) to be searched
   * @param toIndex the index of the last element (exclusive) to be searched
   * @return the index of the first matching element in the array, or -1 if not found
   */
  public static int indexOf(int[] array, int target, int fromIndex, int toIndex) {
    for (int i = fromIndex; i < toIndex; i++) {
      if (array[i] == target)
        return i;
    }
    return -1;
  }

  /**
   * Linearly scans the given array for the first occurrence of the value {@code target}.
   *
   * @param array the array to be searched
   * @param target the value to find
   * @return the index of the first matching element in the array, or -1 if not found
   *
   * @see com.google.common.primitives.Chars#indexOf(char[], char)
   */
  public static int indexOf(char[] array, char target) {
    return indexOf(array, target, 0, array.length);
  }

  /**
   * Linearly scans a range of the given array for the first occurrence of the value {@code target}.
   *
   * @param array the array to be searched
   * @param target the value to find
   * @param fromIndex the index of the first element to be searched (inclusive)
   * @param toIndex the index after the last element to be searched (exclusive)
   * @return the index of the first matching element in the array, or -1 if not found
   */
  public static int indexOf(char[] array, char target, int fromIndex, int toIndex) {
    for (int i = fromIndex; i < toIndex; i++) {
      if (array[i] == target)
        return i;
    }
    return -1;
  }

  // TODO: consider adding symmetric lastIndexOf methods for all the indexOf methods (see com.google.common.primitives.Ints.lastIndexOf(int[], int) for example)

  /**
   * Merges the given arrays into one.
   *
   * @return a new array containing all the elements of the given arrays
   */
  @SafeVarargs
  public static <T> T[] concat(T[]... arrays) {
    if (arrays.length == 0)
      throw new IllegalArgumentException("At least 1 input array expected");
    int newLength = Arrays.stream(arrays).mapToInt(a -> a.length).sum();
    T[] ret = Arrays.copyOf(arrays[0], newLength);
    int cursor = arrays[0].length;
    for (int i = 1; i < arrays.length; i++) {
      T[] arr = arrays[i];
      System.arraycopy(arr, 0, ret, cursor, arr.length);
      cursor += arr.length;
    }
    return ret;
  }

  /**
   * Creates a new array representing a copy of {@code arr} with the given elements appended at the end.
   */
  @SafeVarargs
  public static <T> T[] append(T[] arr, T... newElements) {
    return concat(arr, newElements);
  }

  /**
   * @return a new array containing only the elements from the given array that match the given predicate
   * @see java.util.stream.IntStream#filter(IntPredicate)
   */
  public static int[] filter(int[] array, IntPredicate predicate) {
    return Arrays.stream(array).filter(predicate).toArray();
  }

  /**
   * @return a new array list containing only the elements from the given array that match the given predicate
   * @see Stream#filter(Predicate)
   */
  public static <T> ArrayList<T> filter(T[] array, Predicate<T> predicate) {
    return Arrays.stream(array).filter(predicate).collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * Similar to {@link Arrays#fill(Object[], Object)}, but uses a supplier that is invoked every time to generate
   * the new elements.
   *
   * @param array the array to be filled
   * @param supplier will be invoked to generate each array element
   * @return the same instance that was passed in, after the transformation has been applied
   * @see Arrays#fill(Object[], Object)
   * @see Stream#generate(Supplier)
   */
  public static <T> T[] fill(T[] array, Supplier<T> supplier) {
    for (int i = 0; i < array.length; i++) {
      array[i] = supplier.get();
    }
    return array;
  }

  /**
   * Similar to {@link Arrays#fill(Object[], Object)}, but uses a supplier that is invoked for each array index
   * to generate the new element values.
   *
   * @param array the array to be filled
   * @param supplier will be invoked for each array index to generate the elements:
   *   takes the array index and returns a value for the corresponding element
   * @return the same instance that was passed in, after the transformation has been applied
   * @see Arrays#fill(Object[], Object)
   * @see Stream#generate(Supplier)
   */
  public static <T> T[] fill(T[] array, IntFunction<T> supplier) {
    for (int i = 0; i < array.length; i++) {
      array[i] = supplier.apply(i);
    }
    return array;
  }

  /**
   * @return {@code true} if the array is either {@code null} or empty
   */
  public static boolean isEmpty(int[] array) {
    return array == null || array.length == 0;
  }

  /**
   * @return {@code true} if the array is either {@code null} or empty
   */
  public static boolean isEmpty(Object[] array) {
    return array == null || array.length == 0;
  }

  /**
   * Returns a list representation of the given primitive array.
   * <p>
   * NOTE: this is not the same as {@link Arrays#asList(Object[])}, which would always a list of size 1 when given
   * a primitive array.
   * @see com.google.common.primitives.Doubles#asList(double...)
   */
  public static ArrayList<Double> asList(double[] array) {
    ArrayList<Double> ret = new ArrayList<Double>();
    for (double d : array) {
      ret.add(d);
    }
    return ret;
  }

  /**
   * Returns a list representation of the given primitive array.
   * <p>
   * NOTE: this is not the same as {@link Arrays#asList(Object[])}, which would always a list of size 1 when given
   * a primitive array.
   * @see com.google.common.primitives.Ints#asList(int...)
   */
  public static ArrayList<Integer> asList(int[] array) {
    ArrayList<Integer> ret = new ArrayList<Integer>();
    for (int i : array) {
      ret.add(i);
    }
    return ret;
  }

  public static int[] swap(int[] arr, int i, int j) {
    int temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
    return arr;
  }

  public static <T> T[] swap(T[] arr, int i, int j) {
    T temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
    return arr;
  }

  /** Unwraps every element in the given array */
  public static double[] unbox(Double[] arr) {
    double[] ret = new double[arr.length];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = arr[i];
    }
    return ret;
  }

  /** Wraps every element in the given array */
  public static Double[] box(double[] arr) {
    Double[] ret = new Double[arr.length];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = arr[i];
    }
    return ret;
  }

  /**
   * @return the last element of the given array
   * @throws ArrayIndexOutOfBoundsException if the array is empty
   * @throws NullPointerException if the array is null
   */
  public static <T> T getLast(T[] arr) {
    return arr[arr.length-1];
  }

  /**
   * A simplified wrapper for {@link System#arraycopy}: copies the given number of elements from srcArr to destArr,
   * starting at index 0 in both, and returns destArr.
   * @param srcArr Any array type
   * @param destArr Another array of the same type as srcArr
   * @param length The number of elements to copy starting at index 0.
   * @return destArr
   */
  public static Object copy(Object srcArr, Object destArr, int length) {
    System.arraycopy(srcArr, 0, destArr, 0, length);
    return destArr;
  }

  public static String[] merge(String[] a, String[] b) {
    String[] ret = Arrays.copyOf(a, a.length + b.length);
    System.arraycopy(b, 0, ret, a.length, b.length);
    return ret;
  }

  public static int[] grow(int[] original, int newLen) {
    assert newLen > original.length;
    return copyOf(original, newLen);
  }

  /**
   * Provided for older versions of GWT where {@link Arrays#copyOf(int[], int)} isn't implemented.
   * @see Arrays#copyOf(int[], int)
   *
   * @param original the array to be copied
   * @param newLength the length of the copy to be returned
   * @return a copy of the original array, truncated or padded with zeros
   *     to obtain the specified length
   * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
   * @throws NullPointerException if <tt>original</tt> is null
   * @since 1.6
   */
  public static int[] copyOf(int[] original, int newLength) {
      int[] copy = new int[newLength];
      System.arraycopy(original, 0, copy, 0,
                       Math.min(original.length, newLength));
      return copy;
  }

  /**
   * Provided for older versions of GWT where {@link Arrays#copyOfRange(int[], int, int)} isn't implemented.
   * @see Arrays#copyOfRange(int[], int, int)
   *
   * @param original the array from which a range is to be copied
   * @param from the initial index of the range to be copied, inclusive
   * @param to the final index of the range to be copied, exclusive.
   *     (This index may lie outside the array.)
   * @return a new array containing the specified range from the original array,
   *     truncated or padded with zeros to obtain the required length
   * @throws ArrayIndexOutOfBoundsException if {@code from < 0}
   *     or {@code from > original.length}
   * @throws IllegalArgumentException if <tt>from &gt; to</tt>
   * @throws NullPointerException if <tt>original</tt> is null
   * @since 1.6
   */
  public static int[] copyOfRange(int[] original, int from, int to) {
      int newLength = to - from;
      if (newLength < 0)
          throw new IllegalArgumentException(from + " > " + to);
      int[] copy = new int[newLength];
      System.arraycopy(original, from, copy, 0,
                       Math.min(original.length - from, newLength));
      return copy;
  }

  /**
   * Throws an {@link ArrayIndexOutOfBoundsException} if the given index is not in range for the given array length.
   * This method is useful for client-side GWT code, which might throw a generic {@link JavaScriptException}
   * instead.
   * @param arrayLength length of the array whose bounds are to be checked
   * @param idx the index to check for being within the array's bounds
   * @throws ArrayIndexOutOfBoundsException if the given index is not in the range {@code [0, arrayLength[}
   */
  public static void checkBounds(int arrayLength, int idx) {
    if (idx < 0 || idx >= arrayLength)
      throw new ArrayIndexOutOfBoundsException(idx);
  }

  /**
   * Returns {@code arr[i]} if present, otherwise returns the value computed by the given function
   * after entering it into the array.
   * <p>
   * Same idea as {@link Map#computeIfAbsent}, but intended for using an array instead of Map as a cache.
   *
   * @param i array index (and arg for producer)
   * @param y row index
   * @param producer a function that computes the value for {@code arr[i]} if it's absent
   * @return the current (existing or computed) value associated with the specified array indices,
   *     or null if the computed value is null
   */
  public static <T> T computeIfAbsent(T[] arr, int i, IntFunction<T> producer) {
    T cached = arr[i];
    if (cached == null)
      return arr[i] = producer.apply(i);
    return cached;
  }

  /**
   * Returns {@code arr[y][x]} if present, otherwise returns the value computed by the given function
   * after entering it into the array.
   * <p>
   * Same idea as {@link Map#computeIfAbsent}, but intended for using an array instead of Map as a cache.
   *
   * @param y row index (for outer array)
   * @param x column index (for nested array)
   * @param producer a function, <code>(y, x) &rarr; T</code>, that
   *     computes the value for {@code arr[y][x]} if it's absent
   * @return the current (existing or computed) value associated with the specified array indices,
   *     or null if the computed value is null
   */
  public static <T> T computeIfAbsent(T[][] arr, int y, int x, IntBiFunction<T> producer) {
    T cached = arr[y][x];
    if (cached == null)
      return arr[y][x] = producer.apply(y, x);
    return cached;
  }

}
