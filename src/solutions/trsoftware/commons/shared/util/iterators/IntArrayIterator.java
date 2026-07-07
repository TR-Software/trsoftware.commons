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
 * Iterator over the elements of a primitive {@code int[]} array,
 * providing a {@link #nextInt()} method to avoid auto-boxing.
 *
 * @author Alex
 * @since 1/11/2019
 */
public class IntArrayIterator extends IntRangeIterator {

  private final int[] array;

  public IntArrayIterator(int... array) {
    this(array, array.length);
  }

  public IntArrayIterator(int[] array, int limit) {
    this(array, 0, limit);
  }

  public IntArrayIterator(int[] array, int start, int limit) {
    super(start, limit);
    this.array = array;
    checkBounds();
  }

  // TODO(6/3/2026): write doc for constructors + unit test similar to IntRangeIterator

  private void checkBounds() throws ArrayIndexOutOfBoundsException {
    // force an ArrayIndexOutOfBoundsException if the starting index isn't valid (in client-side GWT code might throw a generic JavaScriptException otherwise)
    ArrayUtils.checkBounds(array.length, start);
    ArrayUtils.checkBounds(array.length, limit);
  }

  @Override
  public int nextInt() {
    return array[super.nextInt()];
  }
}
