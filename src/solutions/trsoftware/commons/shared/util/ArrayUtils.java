/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Date: Nov 7, 2007
 * Time: 8:15:31 PM
 *
 * @author Alex
 */
public class ArrayUtils {

  // TODO: change the order of the args and make array a vararg
  /**
   * @deprecated use {@link StringUtils#join(String, Object[])} instead.
   */
  public static <T> String toString(T[] array, String delimiter) {
    return StringUtils.join(delimiter, array);
  }

  public static String toString(int[] array, String delimiter) {
    StringBuilder str = new StringBuilder();
    for (int i = 0; i < array.length; i++) {
      str.append(array[i]);
      if (i < array.length-1)
        str.append(delimiter);
    }
    return str.toString();
  }

  // TODO: document how the above toString methods differ from Arrays.toString (i.e. they omit brackets and allow specifying a custom delimiter

  /** Linearly scans the array for the given element */
  public static boolean contains(int[] array, int element) {
    for (int elt : array) {
      if (elt == element)
        return true;
    }
    return false;
  }

  /** Linearly scans the array for the given element */
  public static boolean contains(Object[] array, Object element) {
    for (Object elt : array) {
      if (elt != null && elt.equals(element))  // TODO: this seems inferior to linearSearch, which accounts for nulls; why not just delegate to linearSearch?
        return true;
    }
    return false;
  }

  /**
   * Linearly scans the array for the given element.
   * @return The index of the first matching element in the array or -1
   * if not found.
   */
  public static <T> int linearSearch(T[] array, T element) {
    for (int i = 0; i < array.length; i++) {
      if ((element == null && array[i] == null) || (element != null && element.equals(array[i])))
        return i;
    }
    return -1;
  }

  /**
   * Linearly scans the array for the given element.
   * @return The index of the first matching element in the array or -1
   * if not found.
   */
  public static int linearSearch(int[] array, int element) {
    for (int i = 0; i < array.length; i++) {
      if (array[i] == element)
        return i;
    }
    return -1;
  }

  /**
   * Linearly scans the array for the given element.
   * @return The index of the first matching element in the array or -1
   * if not found.
   */
  public static int linearSearch(char[] array, char element) {
    for (int i = 0; i < array.length; i++) {
      if (array[i] == element)
        return i;
    }
    return -1;
  }

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
   */
  public static <T> T[] flexibleArrayAdd(T[] array, int index, T value) {
    // can't instantiate T[] directly, so going through an intermediate ArrayList
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
    array[index] = value;
    return array;
  }

  /**
   * Merges the given arrays into one.
   * @return the new array containing all the elements of the given arrays
   */
  public static <T> T[] concat(T[]... arrays) {
    // can't instantiate T[] directly, so going through an intermediate list
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

  public static int[] filter(int[] array, Predicate<Integer> predicate) {
    ArrayList<Integer> matchingMembers = new ArrayList<Integer>();
    for (int member : array) {
      if (predicate.apply(member))
        matchingMembers.add(member);
    }
    int[] result = new int[matchingMembers.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = matchingMembers.get(i);
    }
    return result;
  }
  
  public static <T> ArrayList<T> filter(T[] array, Predicate<T> predicate) {
    ArrayList<T> matchingMembers = new ArrayList<T>();
    for (T member : array) {
      if (predicate.apply(member))
        matchingMembers.add(member);
    }
    return matchingMembers;
  }

  public static boolean isEmpty(int[] array) {
    return array == null || array.length == 0;
  }

  public static boolean isEmpty(Object[] array) {
    return array == null || array.length == 0;
  }

  public static List<Double> asList(double[] array) {
    ArrayList<Double> ret = new ArrayList<Double>();
    for (double d : array) {
      ret.add(d);
    }
    return ret;
  }

  public static List<Integer> asList(int[] array) {
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

  /** Unboxes every element in the given array */
  public static double[] unbox(Double[] arr) {
    double[] ret = new double[arr.length];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = arr[i];
    }
    return ret;
  }

  /**
   * Returns a List with the selected (endIndex - startIndex + 1) elements
   * of the given arary.
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
  
}
