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
 * A primitive {@code char} iterator for a {@link CharSequence}
 *
 * @author Alex, 4/27/2016
 */
public class CharSequenceIterator extends IndexedCharIterator {

  private final CharSequence charSequence;

  public CharSequenceIterator(CharSequence charSequence) {
    super(charSequence.length());
    this.charSequence = charSequence;
  }

  /**
   * Creates an iterator over the chars in the given sequence starting at the given index
   * @param start index of the first char to be returned
   */
  public CharSequenceIterator(CharSequence charSequence, int start) {
    this(charSequence, start, charSequence.length());
  }

  /**
   * Creates an iterator over the chars in the given sequence  between indices {@code start} (inclusive) and
   * {@code end} (exclusive).
   */
  public CharSequenceIterator(CharSequence charSequence, int start, int end) {
    super(start, end);
    Preconditions.checkPositionIndexes(start, end, charSequence.length());
    this.charSequence = charSequence;
  }

  @Override
  protected char get(int idx) {
    return charSequence.charAt(idx);
  }

}
