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

import solutions.trsoftware.commons.shared.util.Levenshtein;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.util.*;

/**
 * Nov 26, 2012
 *
 * @author Alex
 */
public class TypingLogReplayState {

  /** The log being replayed */
  private TypingLog typingLog;
  private String[] words;
  private TextCharCounts textCharCounts;

  // replay state:
  private static class State {
    /** The time elapsed since start of race at the last {@link #editCursor} */
    private int time;
    /** The index of the current position in {@link TypingLog#getEditLog()} */
    private int editCursor;
    /** Points to the index of the next char after the last one was typed correctly */
    private int charCursor;
    /** The number of basic {@link Levenshtein.EditOperation}s recorded up to this point */
    private int editOpCount;
    /** The text buffer to which edits are applied */
    private StringBuilder editBuffer = new StringBuilder();
    /** The positions of errors up to this point (as determined by all the calls made to {@link #findErrors()} up to this point) */
    private Set<Integer> errorPositions = new HashSet<Integer>();
    private SortedSet<Word> wordsWithErrors = new TreeSet<Word>();
  }
  private State state = new State();

  public class Word implements Comparable<Word> {
    private int wordIdx;
    private String wordStr;
    private int editCursor;

    public Word(int wordIdx, String wordStr, int editCursor) {
      this.wordIdx = wordIdx;
      this.wordStr = wordStr;
      this.editCursor = editCursor;
    }

    public int getWordIdx() {
      return wordIdx;
    }

    public String getWordStr() {
      return wordStr;
    }

    public int getEditCursor() {
      return editCursor;
    }

    @Override
    public int compareTo(Word o) {
      return wordIdx - o.wordIdx;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Word word = (Word)o;

      if (wordIdx != word.wordIdx) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return wordIdx;
    }
  }

  public TypingLogReplayState(TypingLog typingLog) {
    this.typingLog = typingLog;
    words = typingLog.getTextLanguage().getTokenizer().tokenize(typingLog.getText());
    textCharCounts = new TextCharCounts(words, typingLog.getTextLanguage());
  }

  public void reset() {
    state = new State();
  }

  /**
   * Advances the replay state to the given time position.
   * @param time millis since start of the race.
   */
  public void seekToTime(int time) {
    if (time < 0)
      time = 0;  // treat a negative arg value as 0
    if (state.time > time) {
      // we're already past the point being seeked, so reset the replay before seeking
      reset();
    }
    while (!isReplayFinished() && getTimeOfNextEdit() <= time) {
      // advance state to the next edit
      applyNextEdit();
    }
    if (isReplayFinished()) {
      // set the current time position be the time of the last edit that was processed
      state.time = getTimeOfEdit(state.editCursor - 1);
    }
    else {
      // set the current time position be the requested time position
      state.time = time;
    }
  }

  public void seekToEditCursor(int editCursor) {
    if (editCursor < 0)
      editCursor = 0;  // treat a negative arg value as 0
    if (state.editCursor > editCursor) {
      // we're already past the point being seeked, so reset the replay before seeking
      reset();
    }
    while (!isReplayFinished() && state.editCursor < editCursor) {
      // advance state to the next edit
      applyNextEdit();
    }
  }

  public void seekToCharCursor(int charCursor) {
    if (charCursor < 0)
      charCursor = 0;  // treat a negative arg value as 0
    if (state.charCursor > charCursor) {
      // we're already past the point being seeked, so reset the replay before seeking
      reset();
    }
    while (!isReplayFinished() && state.charCursor < charCursor) {
      // advance state to the next edit
      applyNextEdit();
    }
  }

  public void seekToEnd() {
    while (!isReplayFinished()) {
      // advance state to the next edit
      applyNextEdit();
    }
  }

  /**
   * @return {@code true} if we've reached the end of the replay
   */
  public boolean isReplayFinished() {
    return state.editCursor >= typingLog.getEditLog().size();
  }

  private int getTimeOfNextEdit() {
    if (isReplayFinished())
      return Integer.MAX_VALUE;
    int editCursor = state.editCursor;
    return getTimeOfEdit(editCursor);
  }

  private int getTimeOfEdit(int editCursor) {
    List<TypingEdit> editLog = typingLog.getEditLog();
    // make sure the given editCursor is within bounds
    if (editCursor < 0)
      editCursor = 0;
    if (editCursor >= editLog.size())
      editCursor = editLog.size()-1;
    return editLog.get(editCursor).getTime();
  }

  /**
   * Applies the next edit to the {@link #state}'s {@link State#editBuffer}
   */
  private void applyNextEdit() {
    if (isReplayFinished())
      return;  // no more edits to step through
    List<TypingEdit> editLog = typingLog.getEditLog();
    TypingEdit typingEdit = editLog.get(state.editCursor);
    List<Levenshtein.EditOperation> editOps = typingEdit.getEdits();
    for (Levenshtein.EditOperation op : editOps) {
      state.editOpCount++;
      int pos = op.getPosition() + typingEdit.getOffset();
      if (op instanceof Levenshtein.Insertion)
        state.editBuffer.insert(pos, op.getChar());
      else if (op instanceof Levenshtein.Substitution)
        state.editBuffer.replace(pos, pos+1, Character.toString(op.getChar()));
      else if (op instanceof Levenshtein.Deletion) {
        assert op.getChar() == state.editBuffer.charAt(pos); // TODO; verify this assertion?
        state.editBuffer.deleteCharAt(pos);
      }
    }
    int time = typingEdit.getTime();
    advanceCharCursor(time);
    state.time = time;
    state.editCursor++;  // advance to the next edit
    List<Levenshtein.EditOperation> errors = findErrors();
    if (!errors.isEmpty()) {
      for (Levenshtein.EditOperation error : errors) {
        int pos = error.getPosition();
        state.errorPositions.add(pos);
        int wordIdx = getCharCounts().getWordCountAtCharPosition(pos);
        if (wordIdx < words.length)
          state.wordsWithErrors.add(new Word(wordIdx, getWords()[wordIdx], getEditCursor()));
      }
    }
  }

  private void advanceCharCursor(int timeSinceStart) {
    int[] charTimings = typingLog.getCharTimings();
    while (state.charCursor < charTimings.length) {
      int charTime = charTimings[state.charCursor];
      // advance the cursor if the char is correct (time > 0) and it occurs
      // before the current animation time
      if (charTime > 0 && charTime <= timeSinceStart)
        state.charCursor++;
      else
        break;
    }
  }

  /**
   * @return the edit ops needed to correct the current value {@link #getEditBuffer()} to match the expected text
   */
  public List<Levenshtein.EditOperation> findErrors() {
    String textStr = typingLog.getText();
    String typedText = state.editBuffer.toString();
    String expectedText = textStr.substring(0, Math.min(textStr.length(), typedText.length()+5));  // give it 5 chars of lookahead
    // fill the lookahead buffer with junk chars (that are not in the original text), so that Levenshtein.editSequence(x, y) doesn't put the edits out of place
    String filler = StringUtils.repeat('\b', Math.max(0, expectedText.length() - typedText.length()));
    Levenshtein.EditSequence diffs = Levenshtein.editSequence(typedText + filler, expectedText, true, true);
    List<Levenshtein.EditOperation> diffOps = diffs.getOperations();
    StringBuilder editBuffer = new StringBuilder(typedText);
    int i = 0;
    for (Levenshtein.EditOperation diff : diffOps) {
      if (diff.getPosition() < editBuffer.length()) {
        // does this diff correction make the input match expectation?
        diff.apply(editBuffer);
        i++;
        if (expectedText.startsWith(editBuffer.toString())) {
          break;
        }
      }
    }
    return diffOps.subList(0, i);
  }

  public TypingLog getTypingLog() {
    return typingLog;
  }

  public int getTime() {
    return state.time;
  }

  public int getEditCursor() {
    return state.editCursor;
  }

  public int getCharCursor() {
    return state.charCursor;
  }

  public StringBuilder getEditBuffer() {
    return state.editBuffer;
  }

  public SortedSet<Word> getWordsWithErrors() {
    return state.wordsWithErrors;
  }

  public double getAccuracy() {
    return 1d - ((double)state.errorPositions.size() / state.editOpCount);
  }

  public String[] getWords() {
    return words;
  }

  /**
   * @param includeSpaces whether a trailing space should be appended to each word where applicable
   * @return the words in this text, with a trailing space optionally appended to each word.
   */
  public String[] getWords(boolean includeSpaces) {
    String[] rawWords = getWords();
    if (!includeSpaces)
      return rawWords;
    // create a new array with a whitespace optionally appended to each word
    String[] ret = new String[rawWords.length];
    for (int i = 0; i < rawWords.length; i++) {
      String word = rawWords[i];
      if (i < rawWords.length-1)
        word += typingLog.getTextLanguage().getTokenizer().getDelimiter();
      ret[i] = word;
    }
    return ret;
  }

  public TextCharCounts getCharCounts() {
    return textCharCounts;
  }
}