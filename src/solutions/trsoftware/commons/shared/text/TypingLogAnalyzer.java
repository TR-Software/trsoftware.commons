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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * Calculates various statistics about a {@link TypingLog}, such as accuracy, segment WPMs, and a list of all
 * the words with errors.
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
   * number, such that they contain a roughly equal amounts of text (split on word boundaries).
   * This method uses a divide & conquer algorithm, breaking up the text into two halves on each iteration (hence
   * the number of segments should be a power of 2).
   *
   * The time complexity of this algorithm is O(n*log(n)), analysis similar to merge-sort.
   *
   * @param nSegments should be a power of 2, and cannot be greater than the number of words in the text
   * @return the split segments (NOTE: the number of segments returned could be less than requested, if
   * the text doesn't have enough words to be split into {@code N} segments)
   * @throws IllegalArgumentException if {@code nSegments} is not a power of 2
   */
  public List<TextSegment> getSegmentWPMs(int nSegments) {
    int[] charTimings = typingLog.getCharTimings();
    // find suitable split points in the text (we don't want to break words)
    TextSplitter splitter = new TextSplitter(typingLog.getText(), typingLog.getTextLanguage());
    List<TextSplitter.WordLine> lines = splitter.split(nSegments);
    List<TextSegment> ret = new ArrayList<TextSegment>();
    TextCharCounts textCharCounts = replayState.getCharCounts();
    int lastCharTyped = Math.min(typingLog.getNumCharsTyped(), charTimings.length) - 1;
    for (TextSplitter.WordLine line : lines) {
      int charPosStart = textCharCounts.getCharCountUpToWord(line.getWordIdxStart());
      int charPosEnd = Math.min(textCharCounts.getCharCountUpToWord(line.getWordIdxEnd()), lastCharTyped);
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

}