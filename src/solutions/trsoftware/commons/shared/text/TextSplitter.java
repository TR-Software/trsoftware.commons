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

package solutions.trsoftware.commons.shared.text;

import solutions.trsoftware.commons.shared.util.MathUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.stats.ArgMin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Algorithm for breaking text into N segments (e.g. lines) of approximately equal length, such that words are not
 * broken.
 * <p>
 * NOTE: this is a similar problem to Knuth's line-breaking algorithm (used in TeX), but serves a different purpose:
 * the goal here is to break the text into a specific number of lines of roughly the same length, rather than
 * an arbitrary number of lines to fit a specific width.
 *
 * @author Alex
 * @since 11/2/2018
 */
public class TextSplitter {

  private final String text;
  private final List<String> words;
  private final TextCharCounts charCounts;
  private final Language language;

  public TextSplitter(String text, Language language) {
    this.text = text;
    this.language = language;
    String[] wordsArr = language.getTokenizer().tokenize(text);
    words = Arrays.asList(wordsArr);
    charCounts = new TextCharCounts(wordsArr, language);
  }

  /**
   * Splits the text into {@code n} segments of approximately equal length.
   *
   * This method uses a divide-and-conquer algorithm, breaking up the text into two halves on each iteration,
   * hence the number of segments has to be a power of 2.  The time complexity of this algorithm is {@code O(n*log(n))}
   * (analysis similar to mergesort).
   *
   * @param n the desired number of segments; NOTE: this is only a hint - the actual number of segments returned will
   * be the nearest power of 2 which is less than or equal to the actual number of words.
   *
   * @return the split segments (NOTE: the number of segments returned could be less than requested, if
   * the text doesn't have enough words to be split into {@code n} segments, or {@code n} is not a power of 2)
   */
  public List<WordLine> split(int n) {
    // first, make sure nSegments is a power of 2 less than or equal to the actual number of words
    int nWords = charCounts.getWordCount();
    // 1) ensure that nSegments is less than or equal to the actual number of words
    int nSegments = Math.min(n, nWords);
    // 2) ensure that it's a power of 2
    if (!MathUtils.isPowerOf2(nSegments)) {
      // find the next lower power of 2
      while (nSegments > 0 && !MathUtils.isPowerOf2(nSegments)) {
        nSegments--;
      }
    }
    // we start with a single segment containing the full text
    WordLine line = new WordLine(0, nWords);
    if (nSegments < 2)
      return Collections.singletonList(line);  // we only need 1 segment
    return splitLines(line, nSegments);  // recursively split the segment
  }


  /**
   * Uses recursive divide & conquer calls to {@link #splitLine(WordLine)}
   * @see #split(int)
   */
  private List<WordLine> splitLines(WordLine wordLine, int nSegments) {
    List<WordLine> results = new ArrayList<WordLine>();
    if (nSegments == 2) {
      // base case
      WordLine[] split = splitLine(wordLine);
      results.add(split[0]);
      results.add(split[1]);
    }
    else {
      // recursive case
      WordLine[] split = splitLine(wordLine);
      results.addAll(splitLines(split[0], nSegments / 2));
      results.addAll(splitLines(split[1], nSegments / 2));
    }
    return results;
  }

  /**
   * Breaks up the given line into 2 halves, of roughly equal width
   * @see #splitLines(WordLine, int)
   */
  private WordLine[] splitLine(WordLine line) {
    // try each possible split point (O(n))
    ArgMin<WordLine[], Double> argMin = new ArgMin<WordLine[], Double>();
    for (int i = line.wordIdxStart; i < line.wordIdxEnd; i++) {
      WordLine[] parts = new WordLine[2];
      parts[0] = new WordLine(line.wordIdxStart, i);
      parts[1] = new WordLine(i, line.wordIdxEnd);
      // the cost of this split is the squared width difference of the two halves
      double cost = Math.pow(parts[0].width() - parts[1].width(), 2);
      argMin.update(parts, cost);
    }
    return argMin.get();  // return the best split
  }

  /** Helper class for {@link #split(int)} */
  public class WordLine {
    private int wordIdxStart;
    private int wordIdxEnd;

    private WordLine(int wordIdxStart, int wordIdxEnd) {
      this.wordIdxStart = wordIdxStart;
      this.wordIdxEnd = wordIdxEnd;
    }

    public int getWordIdxStart() {
      return wordIdxStart;
    }

    public int getWordIdxEnd() {
      return wordIdxEnd;
    }

    /**
     * @return the number of chars on this line
     */
    public int width() {
      return charCounts.getCharCountUpToWord(wordIdxEnd) - charCounts.getCharCountUpToWord(wordIdxStart);
    }

    /**
     * @return the words on this line
     */
    public List<String> getWords() {
      return words.subList(wordIdxStart, wordIdxEnd);
    }

    /**
     * @return the textual representation of this line (the result of {@link #getWords()} separated using the
     * word delimiter for the text's language)
     */
    public String getText() {
      return StringUtils.join(language.getTokenizer().getDelimiter(), getWords());
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("WordLine{");
      sb.append("wordIdxStart=").append(wordIdxStart);
      sb.append(", wordIdxEnd=").append(wordIdxEnd);
      sb.append("}: ").append(getText());
      return sb.toString();
    }
  }



}
