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

import com.google.common.base.Preconditions;

/**
 * @author Alex, 4/27/2016
 */
public class CharArrayIterator extends IndexedCharIterator {

  private final char[] arr;

  public CharArrayIterator(char... arr) {
    super(arr.length);
    this.arr = arr;
  }

  /**
   * Creates an iterator over the elements in the given array starting at the given index
   *
   * @param start index of the first element to be returned
   */
  public CharArrayIterator(char[] arr, int start) {
    this(arr, start, arr.length);
  }

  /**
   * Creates an iterator over the elements in the given array between indices {@code start} (inclusive) and
   * {@code end} (exclusive)
   */
  public CharArrayIterator(char[] arr, int start, int end) {
    super(start, end);
    this.arr = arr;
    Preconditions.checkPositionIndexes(start, end, arr.length);
  }

  @Override
  protected char get(int idx) {
    return arr[idx];
  }

}
