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

package solutions.trsoftware.commons.shared.text;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;

import java.util.stream.IntStream;

import static solutions.trsoftware.commons.shared.text.Language.ENGLISH;

public class TextCharCountsTest extends TestCase {

  /**
   * Verifies that <code>counts.getWordCountAtCharPosition(i)</code> returns {@code expected}
   * for all {@code i} between {@code start} and {@code end} (inclusive).
   *
   * @see TextCharCounts#getWordCountAtCharPosition(int)
   */
  private void checkWordCountInRange(TextCharCounts counts, int expected, int start, int end) {
    for (int i = start; i <= end; i++) {
      assertEquals(expected, counts.getWordCountAtCharPosition(i));
    }
  }

  private void checkWordBoundaries(TextCharCounts counts, int... boundaries) {
    int[] actual = IntStream.range(-5, counts.getCharCount() + 5).filter(counts::isWordBoundary).toArray();
    AssertUtils.assertArraysEqual(boundaries, actual);
  }

  /**
   * Tests {@link TextCharCounts#getCharCountUpToWord(int)} and {@link TextCharCounts#getWordCountAtCharPosition(int)}.
   */
  public void testCounts() throws Exception {
    {
      TextCharCounts tc = new TextCharCounts(new String[]{"a", "b"}, ENGLISH);
      assertEquals(0, tc.getCharCountUpToWord(0));
      checkWordCountInRange(tc, 0, 0, 1);
      assertEquals(2, tc.getCharCountUpToWord(1));  // counts include spaces between words, but not after the last word
      checkWordCountInRange(tc, 1, 2, 2);
      assertEquals(3, tc.getCharCountUpToWord(2));
      checkWordCountInRange(tc, 2, 3, 100); // everything over 3 should return 2, which is the maximum word count
      assertEquals(3, tc.getCharCount());
      assertEquals(2, tc.getWordCount());
      checkWordBoundaries(tc, 1, 2);
    }
    {
      TextCharCounts tc = new TextCharCounts(new String[]{"foo", "barre", "bizzarre"}, ENGLISH);
      assertEquals(0, tc.getCharCountUpToWord(0));
      checkWordCountInRange(tc, 0, 0, 3);
      assertEquals(4, tc.getCharCountUpToWord(1));  // counts include spaces between words, but not after the last word
      checkWordCountInRange(tc, 1, 4, 9);
      assertEquals(10, tc.getCharCountUpToWord(2));
      checkWordCountInRange(tc, 2, 10, 17);
      assertEquals(18, tc.getCharCountUpToWord(3));
      checkWordCountInRange(tc, 3, 18, 100);  // everything 18 and over should return 3, which is the maximum word count
      assertEquals(18, tc.getCharCount());
      assertEquals(3, tc.getWordCount());
      checkWordBoundaries(tc, 3, 9, 17);
    }
    {
      TextCharCounts tc = new TextCharCounts(new String[]{"foo"}, ENGLISH);
      assertEquals(0, tc.getCharCountUpToWord(0));
      checkWordCountInRange(tc, 0, 0, 2);
      assertEquals(3, tc.getCharCountUpToWord(1));
      checkWordCountInRange(tc, 1, 3, 100);
      assertEquals(3, tc.getCharCountUpToWord(2));
      assertEquals(3, tc.getCharCountUpToWord(3));
      assertEquals(3, tc.getCharCount());
      assertEquals(1, tc.getWordCount());
      checkWordBoundaries(tc, 2);
    }
  }

  public void testEmptyWordsNotAllowed() {
    for (Language lang : Language.values()) {
      final Language language = lang;
      // check that empty words are not allowed
      AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
        public void run() {
          TextCharCounts tc = new TextCharCounts(new String[]{"", "barre"}, language);
        }
      });
      AssertUtils.assertThrows(IllegalArgumentException.class, new Runnable() {
        public void run() {
          TextCharCounts tc = new TextCharCounts(new String[0], language);
        }
      });
    }
  }

  public void testLogographicLanguages() throws Exception {
    // make sure spaces aren't counted for Chinese (or other logographic texts
    for (Language lang : Language.values()) {
      if (lang.isLogographic()) {
        String[] words = {"a", "b", "c", "d"};
        TextCharCounts tc = new TextCharCounts(words, lang);
        System.out.println(lang);

        for (int i = 0; i < words.length; i++) {
          assertEquals(i, tc.getCharCountUpToWord(i));
          assertEquals(i, tc.getWordCountAtCharPosition(i));
          assertTrue(tc.isWordBoundary(i));
        }
        for (int i = words.length; i < 100; i++) {
          assertEquals(words.length, tc.getCharCountUpToWord(i));
          assertEquals(words.length, tc.getWordCountAtCharPosition(i));
          assertFalse(tc.isWordBoundary(i));
        }
      }
    }
  }

}