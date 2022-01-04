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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
   * Like {@link Arrays#toString(Object[])}, but omits the enclosing brackets and allows using a custom delimiter.
   * @return a string representation of the given array, constructed by joining the elements using the given delimiter
   * @deprecated use {@link StringUtils#join(String, Object[])} instead.
   */
  public static <T> String toString(T[] array, String delimiter) {
    return StringUtils.join(delimiter, array);
  }

  /**
   * Like {@link Arrays#toString(int[])}, but omits the enclosing brackets and allows using a custom delimiter.
   * @return a string representation of the given array, constructed by joining the elements using the given delimiter
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
   * Linearly scans a range of the given array for the first occurrence of the value {@code target}.
   *
   * @param array the array to be searched
   * @param target the value to find
   * @param fromIndex the index of the first element (inclusive) to be searched
   * @param toIndex the index of the last element (exclusive) to be searched
   * @return the index of the first matching element in the array, or -1 if not found
   */
  public static <T> int indexOf(T[] array, T target, int fromIndex, int toIndex) {
    for (int i = fromIndex; i < toIndex; i++) {
      if ((target == null && array[i] == null) || (target != null && target.equals(array[i])))
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
   * @param fromIndex the index of the first element (inclusive) to be searched
   * @param toIndex the index of the last element (exclusive) to be searched
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
   * Returns <tt>true</tt> if the two specified arrays of ints are
   * <i>equal</i> to one another.  Two arrays are considered equal if both
   * arrays contain the same number of elements, and all corresponding pairs
   * of elements in the two arrays are equal.  In other words, two arrays
   * are equal if they contain the same elements in the same order.  Also,
   * two array references are considered equal if both are <tt>null</tt>.<p>
   *
   * @param a one array to be tested for equality.
   * @param a2 the other array to be tested for equality.
   * @return <tt>true</tt> if the two arrays are equal.
   * @deprecated can now use {@link Arrays#equals(int[], int[])} in GWT
   */
  public static boolean equals(int[] a, int[] a2) {
      if (a==a2)
          return true;
      if (a==null || a2==null)
          return false;

      int length = a.length;
      if (a2.length != length)
          return false;

      for (int i=0; i<length; i++)
          if (a[i] != a2[i])
              return false;

      return true;
  }


  /**
   * Inserts the given entry into the given array at the given index,
   * if necessary, losslessly resizing the array.
   *
   * Note: this method exists only to support hosted mode execution in java,
   * and is not needed in javascript since JS arrays are dynamic.
   *
   * @return The reference to the same array if it wasn't resized or a new
   * one if it was.
   * @deprecated
   */
  public static <T> T[] flexibleArrayAdd(T[] array, int index, T value) {
    // can't instantiate T[] directly, so going through an intermediate ArrayList
    // TODO(12/16/2019): can use Arrays.copyOf(T[], int) and System.arraycopy() to avoid using an intermediate list
    if (array == null || index >= array.length) {
      ArrayList<T> newArrayList = new ArrayList<T>(index+1);
      // must append nulls to the array list since the index is greater than its size
      for (int i = 0; i < index; i++) {
        newArrayList.add(array != null && i < array.length ? array[i] : null);
      }
      newArrayList.add(value);
      return (T[])newArrayList.toArray();
    }
    else {
      // TODO(1/3/2022): potential bug: this overwrites the existing element at array[index]
      array[index] = value;
      return array;
    }
  }

  /**
   * Adds the given element to the given primitive array at the given index.
   * The index should represent the next available empty slot in the given array.
   * If the array is already full, creates a new one 1.5 the size and copies all
   * the elements into it before doing the addition.
   *
   * This method can be used to implement auto-resizing collections of primitives
   * (e.g. ArrayList<Float> except using primitives).
   *
   * @return the new array after the insertion (may or may not be the same one
   * as was given)
   */
  public static float[] flexibleArrayAdd(float[] array, int index, float value) {
    if (index >= array.length) {
      // must grow the array
      float[] newArray = new float[(int)Math.max(2, Math.ceil(1.5 * array.length))];
      // copy all the existing elements to the new array
      System.arraycopy(array, 0, newArray, 0, array.length);
      array = newArray;
    }
    // TODO(1/3/2022): potential bug: this overwrites the existing element at array[index]
    array[index] = value;
    return array;
  }

  /**
   * Merges the given arrays into one.
   * @return the new array containing all the elements of the given arrays
   */
  public static <T> T[] concat(T[]... arrays) {
    // can't instantiate T[] directly, so going through an intermediate list
    // TODO(12/16/2019): can use Arrays.copyOf(T[], int) to avoid using an intermediate list
    LinkedList<T> list = new LinkedList<T>();
    for (T[] array : arrays) {
      for (T elt : array) {
        list.addLast(elt);
      }
    }
    return list.toArray(arrays[0]);  // use the first array the typecast "hint" (the no-arg version of toArray will not work to get T[] back)
  }

  /**
   * Interleaves the arrays: [a1, b1, c1] + [a2, b2, c2] => [a1, a2, b1, b2, c1, c2]
   */
  public static <T> T[] interleave(T[]... arrays) {
    // can't instantiate T[] directly, so going through an intermediate list
    // TODO(12/16/2019): can use Arrays.copyOf(T[], int) to avoid using an intermediate list
    LinkedList<T> list = new LinkedList<T>();
    boolean allTapped;
    int i = 0;
    do {
      allTapped = true;
      for (T[] array : arrays) {
        if (array.length > i) {
          list.add(array[i]);
          allTapped = false;
        }
      }
      i++;
    } while (!allTapped);
    return list.toArray(arrays[0]);  // use the first array the typecast "hint" (the no-arg version of toArray will not work to get T[] back)
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
   * @param supplier will be invoked for each array index to generate the elements
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
   * Returns a List with the selected (endIndex - startIndex + 1) elements
   * of the given array.
   *
   * @deprecated
   * @see Arrays#copyOfRange(Object[], int, int)
   */
  public static <T> List<T> slice(T[] arr, int startIndex, int endIndex) {
    ArrayList<T> ret = new ArrayList<T>(endIndex - startIndex + 1);
    ret.addAll(Arrays.asList(arr).subList(startIndex, endIndex + 1));
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
   * Forces an {@link ArrayIndexOutOfBoundsException} if the given index is not in range for the given array length.
   * This method is useful for client-side GWT code, which might throw a generic {@link com.google.gwt.core.client.JavaScriptException}
   * instead.
   * @param arrayLength length of the array whose bounds are to be checked
   * @param idx the index to check for being within the array's bounds
   * @throws ArrayIndexOutOfBoundsException if the given index is not in the range {@code [0, arrayLength[}
   */
  public static void checkBounds(int arrayLength, int idx) {
    if (idx < 0 || idx > arrayLength-1)
      throw new ArrayIndexOutOfBoundsException(idx);
  }

}
