
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

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * Based on the implementation of {@link CharSequence#codePoints()}, this class iterates over the Unicode code points
 * of a {@link CharSequence} such as a {@link String} or {@link StringBuilder}.
 * <p>
 * This class is intended as a GWT-compatible substitute for {@link CharSequence#codePoints()} (which is not
 * emulated by GWT 2.8.2)
 * <p>
 * For an exact replica of {@link CharSequence#codePoints()}, use the {@link #codePointsStream(CharSequence)} factory method.
 *
 * @author Alex
 * @since 5/14/2019
 * @see #codePointsStream(CharSequence)
 */
public class CodePointIterator implements PrimitiveIterator.OfInt {

  private int cur = 0;
  private CharSequence chars;

  public CodePointIterator(CharSequence chars) {
    this.chars = chars;
  }

  @Override
  public void forEachRemaining(IntConsumer block) {
    final int length = chars.length();
    int i = cur;
    try {
      while (i < length) {
        char c1 = chars.charAt(i++);
        if (!Character.isHighSurrogate(c1) || i >= length) {
          block.accept(c1);
        }
        else {
          char c2 = chars.charAt(i);
          if (Character.isLowSurrogate(c2)) {
            i++;
            block.accept(Character.toCodePoint(c1, c2));
          }
          else {
            block.accept(c1);
          }
        }
      }
    }
    finally {
      cur = i;
    }
  }

  public boolean hasNext() {
    return cur < chars.length();
  }

  public int nextInt() {
    final int length = chars.length();

    if (cur >= length) {
      throw new NoSuchElementException();
    }
    char c1 = chars.charAt(cur++);
    if (Character.isHighSurrogate(c1) && cur < length) {
      char c2 = chars.charAt(cur);
      if (Character.isLowSurrogate(c2)) {
        cur++;
        return Character.toCodePoint(c1, c2);
      }
    }
    return c1;
  }

  /**
   * Emulates the behavior of {@link CharSequence#codePoints()} for GWT code.
   * @return an IntStream of Unicode code points from the given sequence
   */
  public static IntStream codePointsStream(CharSequence chars) {
    return StreamSupport.intStream(() ->
            Spliterators.spliteratorUnknownSize(
                new CodePointIterator(chars),
                Spliterator.ORDERED),
        Spliterator.ORDERED,
        false);
  }

}
