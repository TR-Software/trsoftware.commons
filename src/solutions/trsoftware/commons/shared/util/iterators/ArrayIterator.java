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

package solutions.trsoftware.commons.shared.util.iterators;

import solutions.trsoftware.commons.shared.util.ArrayUtils;

/**
 * Iterates the elements of a generic typed array.
 * <p>
 * If the type of the array elements is not known at compile-time,
 * use {@link solutions.trsoftware.commons.server.util.reflect.ArrayIterator} instead.
 *
 * @see com.google.common.collect.Iterators#forArray(Object[])
 * @author Alex
 * @since 5/1/2018
 */
public class ArrayIterator<T> extends IndexedIterator<T> {

  private final T[] arr;

  public ArrayIterator(T[] arr) {
    super(arr.length);
    this.arr = arr;
  }

  /**
   * Will iterate array elements starting from the given index
   * @param arr the array to iterate
   * @param start the starting index for the iteration
   * @throws ArrayIndexOutOfBoundsException if the given index is not in the range {@code [0, arr.length[}
   */
  public ArrayIterator(T[] arr, int start) {
    super(start, arr.length);
    this.arr = arr;
    // force an ArrayIndexOutOfBoundsException if the starting index isn't valid (in client-side GWT code might throw a generic JavaScriptException otherwise)
    ArrayUtils.checkBounds(arr.length, start);
  }

  @Override
  protected T get(int idx) {
    return arr[idx];
  }

}
