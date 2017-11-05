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

package solutions.trsoftware.commons.shared.text;

import solutions.trsoftware.commons.shared.util.stats.ArgMin;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * Calculates various statistics about a {@link TypingLog}.
 *
 * For now, this class exists just to calculate the accuracy,
 * but in the future, it can be used to compute other statistics to the
 * user, like partial WPM (x millis into the race), the list of words where
 * errors have been made, etc.
 *
 * Dec 29, 2012
 *
 * @author Alex
 */
public class TypingLogAnalyzer {

  private TypingLog typingLog;
  /** We use {@link TypingLogReplayState} to calculate the accuracy and other info, like words with errors */
  private TypingLogReplayState replayState;

  public TypingLogAnalyzer(TypingLog typingLog) {
    this.typingLog = typingLog;
    replayState = new TypingLogReplayState(typingLog);
    replayState.seekToEnd();
  }

  /**
   * We define accuracy as the % of correct typing edits out of the total number of typing edits that were made.
   *
   * @return The accuracy of the given log in entering the original text.
   */
  public double calcAccuracy() {
    return replayState.getAccuracy();
  }

  public List<String> getWordsWithErrors() {
    SortedSet<TypingLogReplayState.Word> wordsWithErrors = replayState.getWordsWithErrors();
    ArrayList<String> ret = new ArrayList<String>();
    for (TypingLogReplayState.Word word : wordsWithErrors) {
      ret.add(word.getWordStr());
    }
    return ret;
  }

  /**
   * Calculates typing speed in the given number of segments in the race.  The segments are chosen based on the given
   * number, such that they contain a roughly equal amount of text (split on word boundaries).
   * This method uses a divide & conquer algorithm, breaking up the text into two halves on each iteration (hence
   * the number of segments should be a power of 2).
   * @param nSegments should be a power of 2
   */
  public List<TextSegment> getSegmentWPMs(int nSegments) {
    int[] charTimings = typingLog.getCharTimings();
    // find suitable split points in the text (we don't want to break words)
    List<WordLine> lines = splitLines(nSegments);
    List<TextSegment> ret = new ArrayList<TextSegment>();
    TextCharCounts textCharCounts = replayState.getCharCounts();
    int lastCharTyped = Math.min(typingLog.getNumCharsTyped(), charTimings.length) - 1;
    for (WordLine line : lines) {
      int charPosStart = textCharCounts.getCharCountUpToWord(line.wordIdxStart);
      int charPosEnd = Math.min(textCharCounts.getCharCountUpToWord(line.wordIdxEnd), lastCharTyped);
      int charLen = charPosEnd - charPosStart;
      if (charLen <= 0)
        break;  // the user didn't actually type this segment (because he didn't finish the race)
      int segmentTime = charTimings[charPosEnd] - charTimings[charPosStart];
      double segmentWpm = TypingSpeed.calcWpm(charLen, segmentTime, typingLog.getTextLanguage());
      ret.add(new TextSegment(charPosStart, charLen, segmentWpm));
    }
    return ret;
  }

  /** Helper class for {@link #getSegmentWPMs(int)} */
  public class TextSegment {
    private int startPos;
    private int length;
    private double wpm;

    public TextSegment(int startPos, int length, double wpm) {
      this.startPos = startPos;
      this.length = length;
      this.wpm = wpm;
    }

    public int getStartPos() {
      return startPos;
    }

    public int getEndPos() {
      return startPos + length;
    }

    public int getLength() {
      return length;
    }

    public double getWpm() {
      return wpm;
    }

    public String getTextStr() {
      return typingLog.getText().substring(startPos, startPos+length);
    }
  }


  /** Helper class for {@link #splitLines(int)} */
  private class WordLine {
    private int wordIdxStart;
    private int wordIdxEnd;

    private WordLine(int wordIdxStart, int wordIdxEnd) {
      this.wordIdxStart = wordIdxStart;
      this.wordIdxEnd = wordIdxEnd;
    }

    private int width() {
      TextCharCounts charCounts = replayState.getCharCounts();
      return charCounts.getCharCountUpToWord(wordIdxEnd) - charCounts.getCharCountUpToWord(wordIdxStart);
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("WordLine{");
      sb.append("wordIdxStart=").append(wordIdxStart);
      sb.append(", wordIdxEnd=").append(wordIdxEnd);
      sb.append("}: ");
      String[] words = replayState.getWords(true);
      for (int i = wordIdxStart; i < wordIdxEnd; i++) {
        sb.append(words[i]);
      }
      return sb.toString();
    }
  }

  /** Helper method for {@link #splitLines(int)}. Breaks up the given line into 2 halves, of roughly equal width */
  public WordLine[] splitLine(WordLine line) {
    // try all the possible split points
    ArgMin<WordLine[], Double> argMin = new ArgMin<WordLine[], Double>();
    for (int i = line.wordIdxStart; i < line.wordIdxEnd; i++) {
      WordLine[] parts = new WordLine[2];
      parts[0] = new WordLine(line.wordIdxStart, i);
      parts[1] = new WordLine(i, line.wordIdxEnd);
      // the cost of this split is the squared width difference of the two halves
      double cost = Math.pow(parts[0].width() - parts[1].width(), 2);
      argMin.update(parts, cost);
    }
    WordLine[] bestSplit = argMin.get();
    return bestSplit;
  }

  /** Helper method for {@link #getSegmentWPMs(int)}. Breaks the text into the given number of segments. */
  public List<WordLine> splitLines(int nSegments) {
    TextCharCounts charCounts = replayState.getCharCounts();
    int nWords = charCounts.getWordCount();
    return splitLines(new WordLine(0, nWords), nSegments);
  }

  /** Helper method for {@link #splitLines(int)}. Uses recursive divide & conquer calls to {@link #splitLine(WordLine)} */
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
}