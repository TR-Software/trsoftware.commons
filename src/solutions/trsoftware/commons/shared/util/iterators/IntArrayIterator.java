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

import java.util.PrimitiveIterator;

/**
 * Same as {@link ArrayIterator}, but optimized for primitive {@code int} arrays:
 * provides the {@link #nextInt()} method to avoid auto-boxing.
 *
 * @author Alex
 * @since 1/11/2019
 */
public class IntArrayIterator extends IndexedIterator<Integer> implements PrimitiveIterator.OfInt {

  private final int[] arr;

  public IntArrayIterator(int[] arr) {
    super(arr.length);
    this.arr = arr;
  }

  public IntArrayIterator(int[] arr, int limit) {
    super(limit);
    this.arr = arr;
  }

  public IntArrayIterator(int[] arr, int start, int limit) {
    super(start, limit);
    this.arr = arr;
    checkBounds();
  }

  private void checkBounds() throws ArrayIndexOutOfBoundsException {
    // force an ArrayIndexOutOfBoundsException if the starting index isn't valid (in client-side GWT code might throw a generic JavaScriptException otherwise)
    ArrayUtils.checkBounds(arr.length, start);
    ArrayUtils.checkBounds(arr.length, limit);
  }

  @Override
  public int nextInt() {
    maybeThrowNoSuchElement();
    return arr[i++];
  }

  @Override
  protected Integer get(int idx) {
    return arr[idx];
  }
}
