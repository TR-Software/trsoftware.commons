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

import solutions.trsoftware.commons.shared.util.ArrayUtils;

import java.util.Arrays;

/**
 * Immutable object containing character counts for the words a text.
 *
 * @author Alex
 */
public class TextCharCounts {
  /**
   * The total number of characters in the text.  Tracked separately
   * from the text string because it considers all internal whitespace
   * (e.g. between sentences) as a single char.
   */
  private final int charCount;
  /** The total number of words in the text */
  private final int wordCount;

  /** Gives the total number of characters in the text up to word i */
  private final int[] charCountUpToWord;

  public TextCharCounts(String[] words, Language lang) {
    int counter = 0;
    wordCount = words.length;
    charCountUpToWord = new int[wordCount];
    if (ArrayUtils.isEmpty(words))
      throw new IllegalArgumentException("Empty words.");
    final int addend = lang.isLogographic() ? 0 : 1;  // +1 for the trailing space (or enter) in languages that have spaces
    for (int i = 0; i < wordCount-1; i++) {
      int wordLength = words[i].length();
      if (wordLength == 0)
        throw new IllegalArgumentException("Empty word.");
      int adjustedWordLength = wordLength + addend;
      counter += adjustedWordLength;
      charCountUpToWord[i+1] = charCountUpToWord[i] + adjustedWordLength;
    }
    charCount = counter + words[wordCount-1].length();
  }

  public int getCharCount() {
    return charCount;
  }

  public int getWordCount() {
    return wordCount;
  }

  /**
   * Returns the number of characters in the text before word {@code i}.
   * This is equal to the index (in the full text string) of the first character in this word.
   *
   * @param i the word index (0-indexed)
   */
  public int getCharCountUpToWord(int i) {
    if (i <= 0)
      return 0;
    if (i >= wordCount)
      return charCount;
    else
      return charCountUpToWord[i];
  }

  /**
   * Returns the word index at the given char index (i.e. the inverse of {@link #getCharCountUpToWord(int)}).
   * <p>
   * This is equal to the number of whole words typed when the given number of characters have been typed.
   *
   * @param charPosition index of a character in the full text string
   */
  public int getWordCountAtCharPosition(int charPosition) {
    if (charPosition >= charCount)
      return wordCount;
    int i = Arrays.binarySearch(charCountUpToWord, charPosition);
    if (i >= 0)
      return i;  // found an exact match
    else
      return Math.max(0, ((i+1)*-1)-1);  // (i+1)*-1 gives the insertion position for the match, from which we subtract 1 to get the word count
  }

  /**
   * @param charPosition index of a character in the full text string
   * @return {@code true} iff the given char index is the last character of one of the words in the text
   */
  public boolean isWordBoundary(int charPosition) {
    if (charPosition < 0 || charPosition >= charCount)
      return false;
    return getWordCountAtCharPosition(charPosition + 1) > getWordCountAtCharPosition(charPosition);
  }

}
