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
