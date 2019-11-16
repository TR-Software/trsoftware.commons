/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.shared.util.text;


import solutions.trsoftware.commons.shared.util.iterators.CharSequenceIterator;

import java.util.Iterator;

/**
 * Represents a range of consecutive chars.
 *
 * @author Alex
 * @since 12/24/2017
 */
public class CharRange implements CharSequence, Iterable<Character> {

  public final char min;
  public final char max;

  /** Will be lazily initialized on the first invocation of {@link #toString()} */
  private String str;

  /**
   * Creates a range of consecutive chars in the range {@code [min, max]}
   * @param min the lowest char to be included in the range
   * @param max the highest char to be included in the range (inclusive)
   */
  public CharRange(char min, char max) {
    if (min > max)
      throw new IllegalArgumentException(min + " > " + max);
    this.min = min;
    this.max = max;
  }

  @Override
  public int length() {
    return max - min + 1;
  }

  @Override
  public char charAt(int index) {
    if (index < 0 || index >= length())
      throw new IndexOutOfBoundsException(String.valueOf(index));
    return (char)(min + index);
  }

  @Override
  public String toString() {
    if (str == null)
      str = toString(this);
    return str;
  }

  public static String toString(CharSequence charSequence) {
    if (charSequence != null) {
      int len = charSequence.length();
      if (len > 0) {
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) {
          chars[i] = charSequence.charAt(i);
        }
        return new String(chars);
      }
    }
    return "";
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    // check bounds
    if (start < 0 || end < 0 || start > end || end > length())
      throw new IndexOutOfBoundsException(start + ", " + end);
    int len = end - start;
    if (len == 0)
      return "";  // we have no way of representing an empty sequence with CharRange
    return new CharRange(charAt(start), charAt(end-1));
  }

  @Override
  public Iterator<Character> iterator() {
    return new CharSequenceIterator(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    CharRange that = (CharRange)o;

    if (min != that.min)
      return false;
    return max == that.max;
  }

  @Override
  public int hashCode() {
    int result = (int)min;
    result = 31 * result + (int)max;
    return result;
  }
}
