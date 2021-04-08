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

import com.google.gwt.core.client.Duration;
import solutions.trsoftware.commons.shared.util.Levenshtein;

import java.util.ArrayList;
import java.util.List;

/**
 * Models the state of correctness of a stream of characters being input
 * by the user while typing a particular text.  Provides feedback to the UI
 * during the race and generates a TypingLog at the end of the race.
 *
 * Nov 19, 2012
 *
 * @author Alex
 */
public class TextInputModel {
  /** The text to be typed */
  private final String text;
  /** The language of {@link #text} */
  private final Language textLanguage;
  /** The language of the text */
  private TextCharCounts textCharCounts;
  /**
   * This setting decides whether correct input prefixes will be "accepted"
   * (i.e. cleared from the text box). This is the case for NewInputPanel with
   * normal languages, but not the case with NewInputPanelLogographic for
   * logographic languages.
    */
  private boolean enableAcceptingPrefixes;
  /** Stores the time when each char in the text was accepted as correct */
  private int[] acceptedCharTimings;

  /** The last-touched index in acceptedCharTimings */
  private int lastAcceptedCharIndex;

  /**
   * The position in the text (and consequently in the acceptedCharTimings array)
   * at which the user's current input text field starts.
   * (For example if the text is "Hello world" and the user is about to type 'r',
   * then cursor position is 6 - just at the start of "world", because the
   * word "Hello " has been accepted, but the "wo" have not yet been accepted.
   * (unless we start clearing correct chars as the user types).
   *
   * This value is used to know what the text input element in the UI represents.
   */
  private int charCursor = 0;

  /**
   * The number of words that have been accepted.
   *
   * This value is used for WPM and for highlighting the next word
   * in WordsView.
   */
  private int wordCursor = 0;

  /** A full log of each update to the user's input */
  private List<TypingEdit> editLog = new ArrayList<TypingEdit>();

  /**
   * What the current value in the text box should be (might be updated
   * as a result of a call to update() if some of the correct chars
   * in the beginning are to be "accepted" (i.e. deleted from the text box).
   */
  private String lastInput = "";

  /** Result of the last call to update */
  private TextInputUpdate lastUpdateResult;

  /** The official start time of the race */
  private Duration duration;

  /** Constructor */
  public TextInputModel(String text, Language language, TextCharCounts charCounts, boolean enableAcceptingPrefixes) {
    this.enableAcceptingPrefixes = enableAcceptingPrefixes;
    this.text = text;
    textLanguage = language;
    textCharCounts = charCounts;
    acceptedCharTimings = new int[this.text.length()];
  }

  public void startTimer() {
    duration = new Duration();  // timing starts as soon as this object is created
  }

  /**
   * To be called when an ordinary character was typed (or backspace).
   * @param input the value of the text input field after this character
   * will be typed.
   * @return the result of this update: what's changed.
   */
  public TextInputUpdate update(final String input) {
    if (input.equals(lastInput))
      return null;  // the input hasn't changed since last time (e.g. del key pressed at the end of the word, or a letter overwrites the same letter that was selected)
    final int time = duration.elapsedMillis();

    // update the accepted chars, one at a time, with special handling of each word boundary
    int acceptedPrefixLength = 0; // the index of the last char of the last full word accepted from the input
    int correctCharCount = 0;  // how many of the leading input chars are correct
    for (int i = 0; i < input.length(); i++) {
      // go through each char and mark it correct if it is, updating the word cursor if the word is finished
      // NOTE: for logographic languages we'll end up going over the same input over and over again since charCursor will never advance
      int newCursor = charCursor + i;
      if (newCursor >= text.length())
        break; // exceeded the text length
      char acceptedChar = text.charAt(newCursor);
      if (acceptedChar != input.charAt(i)) {
        break; // mismatch at this position
      }
      // otherwise, we have a match: accept this char
      correctCharCount++;
      if (acceptedCharTimings[newCursor] == 0) {
        // mark this char as correct if not already marked (don't overwrite if already marked correct)
        acceptedCharTimings[newCursor] = time;
        lastAcceptedCharIndex = newCursor;
      }
      if (enableAcceptingPrefixes && (newCursor == text.length()-1 || acceptedChar == ' '))  {
        // this is either the end of the last word or a word boundary
        // how should the UI input field be updated as a result of this word boundary transition?
        // 1) for normal languages, all the correct input (up to this point) should be cleared
        // 2) for logographic languages, we can't modify the text box because that will throw off the IME (so we always leave acceptedPrefixLength=0)
        // but this block is implementing non-logographic logic, so we clear the accepted word
        acceptedPrefixLength = i+1;
      }
    }
    return lastUpdateResult = finishUpdate(input, time, acceptedPrefixLength, correctCharCount);
  }

  private TextInputUpdate finishUpdate(String input, int time, int acceptedPrefixLength, int correctCharCount) {

    // now update the edit history
    Levenshtein.EditSequence edits = Levenshtein.editSequence(lastInput, input, true, true);
    editLog.add(new TypingEdit(charCursor, edits.getOperations(), time));

    // what do we want to know after the update:
    // 0) acceptedPrefixLength: have any words been accepted (i.e. are to be cleared)? 
    // 1) what the new value of the text field should be (some words might have been accepted and are to be cleared)
    // 2) charCursor where the text field now starts (i.e. how much of the text that's been accepted)
    // 3) how many chars remaining in the text field are correct (used for error highlighting in the UI)
    // 4) how many words have been typed correctly (used for sending player progress updates to server)

    // compute (1): what the new value of the text field should be
    if (acceptedPrefixLength > 0) {
      lastInput = input.substring(acceptedPrefixLength);
    }
    else {
      lastInput = input;
    }
    // compute (2): charCursor where the text field starts (might always be zero for logographic languages)
    charCursor += acceptedPrefixLength;
    // compute (3): how many chars remaining in the text field are correct
    correctCharCount -= acceptedPrefixLength;
    // compute (4): how many words have been typed correctly (used for sending player progress updates to server)
    if (lastAcceptedCharIndex > 0) {
      // NOTE: the following computation will fail for logographic languages when lastAcceptedCharIndex == 0, so we special case it
      wordCursor = textCharCounts.getWordCountAtCharPosition(lastAcceptedCharIndex + 1);
    }
    return new TextInputUpdate(acceptedPrefixLength, lastInput, charCursor, correctCharCount, wordCursor);
  }

  /**
   * As a result of each call to update, the caller will want to know these 3 quantities:
   * 0) have any words been accepted (i.e. are to be cleared)?
   * 1) what the new value of the text field should be (some words might have been accepted and are to be cleared)
   * 2) charCursor where the text field now starts (i.e. how much of the text that's been accepted)
   * 3) how many chars remaining in the text field are correct (used for error highlighting in the UI)
   * 4) how many words have been typed correctly (used for sending player progress updates to server)
   */
  public static class TextInputUpdate {
    /**
     * If words have been accepted, this will give the length of the prefix
     * that is to be cleared out of the text box as a result of this update.
     */
    private int acceptedInputPrefixLength;
    /**
     * What the new value of the text field should be (some words might have been accepted and are to be cleared)
     */
    private String newInputValue;

    /**
     * Cursor in the full text where the text field now starts (i.e. how much of the text that's been accepted)
     */
    private int newCharCursor;

    /**
     * How many chars remaining in the text field are correct (used for error highlighting in the UI)
     */
    private int correctInputPrefixLength;

    /**
     * How many words have been typed correctly (used for sending player progress updates to server)
     */
    private int newWordCursor;

    public TextInputUpdate(int acceptedInputPrefixLength, String newInputValue, int newCharCursor, int correctInputPrefixLength, int newWordCursor) {
      this.acceptedInputPrefixLength = acceptedInputPrefixLength;
      this.newInputValue = newInputValue;
      this.newCharCursor = newCharCursor;
      this.correctInputPrefixLength = correctInputPrefixLength;
      this.newWordCursor = newWordCursor;
    }

    public int getAcceptedInputPrefixLength() {
      return acceptedInputPrefixLength;
    }

    public String getNewInputValue() {
      return newInputValue;
    }

    public int getNewCharCursor() {
      return newCharCursor;
    }

    public int getCorrectInputPrefixLength() {
      return correctInputPrefixLength;
    }

    public int getNewWordCursor() {
      return newWordCursor;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append("TextInputUpdate");
      sb.append("(newCharCursor=").append(newCharCursor);
      sb.append(", newWordCursor=").append(newWordCursor);
      sb.append(", acceptedInputPrefixLength=").append(acceptedInputPrefixLength);
      sb.append(", correctInputPrefixLength=").append(correctInputPrefixLength);
      sb.append(", newInputValue='").append(newInputValue).append('\'');
      sb.append(')');
      return sb.toString();
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("TextInputModel");
    sb.append("(text='").append(text).append('\'');
    sb.append(", acceptedCharTimings=").append(acceptedCharTimings == null ? "null" : "");
    for (int i = 0; acceptedCharTimings != null && i < acceptedCharTimings.length; ++i)
      sb.append(i == 0 ? "" : ", ").append(acceptedCharTimings[i]);
    sb.append(", editLog=").append(editLog);
    sb.append(')');
    return sb.toString();
  }

  public int getWordCursor() {
    return wordCursor;
  }

  public String getText() {
    return text;
  }

  public TextInputUpdate getLastUpdateResult() {
    return lastUpdateResult;
  }

  /** Converts the stored info to a typing log */
  public TypingLog getTypingLog() {
    return new TypingLog(text, textLanguage, acceptedCharTimings, editLog);
  }

}