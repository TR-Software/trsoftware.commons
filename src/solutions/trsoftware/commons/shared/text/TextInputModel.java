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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import solutions.trsoftware.commons.bridge.BridgeTypeFactory;
import solutions.trsoftware.commons.shared.util.Duration;
import solutions.trsoftware.commons.shared.util.Levenshtein;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Models the state of correctness of a stream of characters being input by a user into a text input field
 * while typing a particular text.
 *
 * Provides feedback to the UI during the text input session and generates a {@link TypingLog} at the end.
 *
 * @since Nov 19, 2012
 * @author Alex
 */
public class TextInputModel {
  /** The complete text to be typed */
  private final String text;
  /** The language of {@link #text} */
  private final Language textLanguage;
  /** The character counts for the words in {@link #text} */
  private final TextCharCounts textCharCounts;
  /**
   * This setting determines whether the UI intends to remove "accepted" prefixes from the text input element
   * after each {@linkplain #update(String) update}. This is typically the case for standard languages
   * (but not for {@link Language#isLogographic() logographic} languages).
   */
  private final boolean enableAcceptingPrefixes;

  /**
   * Stores the time when each char in the text was accepted as correct.
   * @see TypingLog#getCharTimings()
   * @see #update(String)
   */
  private final int[] acceptedCharTimings;

  /** The last-touched index in {@link #acceptedCharTimings} */
  private int lastAcceptedCharIndex = -1;

  /**
   * The character position in the {@link #text} corresponding to the first character in the text input element.
   * This value is used by {@link #update(String)} to correlate the given input with the full text.
   * <p>
   * <strong>NOTE:</strong> this value does not indicate the number of chars that have been accepted (see {@link #lastAcceptedCharIndex}
   * for that information).  As a matter of fact, it never changes if {@link #enableAcceptingPrefixes} is disabled.
   * <p>
   * Example (assuming {@link #enableAcceptingPrefixes} is enabled):
   * If the full text is "Hello world" and the user is about to type 'r',
   * then the cursor position is 6 (just at the start of "world"), because the complete
   * word "Hello " has been accepted (and cleared out of the input element),
   * but the "wo" prefix of "world" hasn't been accepted ye.
   */
  private int charCursor = 0;  // TODO(11/17/2021): come up with a better name for this field (e.g. inputStartPosition)

  /**
   * The number of complete words that have been accepted.
   * <p>
   * This value is used by the UI for updating the WPM and highlighting the next word in the text.
   */
  private int wordCursor = 0;

  /**
   * A log of the updates to the user's input.
   *
   * @see TypingLog#getEditLog()
   * @see #update(String)
   */
  private final List<TypingEdit> editLog = new ArrayList<>();

  /**
   * What the current value in the text input field should be after the latest invocation of {@link #update(String)}.
   * This may or may not be the same as the argument passed to the last invocation of {@link #update(String)}, depending
   * on whether {@link #enableAcceptingPrefixes} is enabled.
   * The UI must ensure that its input element is always updated accordingly.
   *
   * @see TextInputUpdate#getNewInputValue()
   */
  private String lastInput = "";

  /** Result of the last invocation of {@link #update(String)} */
  private TextInputUpdate lastUpdateResult;

  /** The official start time of the recording */
  private Duration duration;

  /**
   * Alternate constructor that doesn't require a pre-existing {@link TextCharCounts} instance for the text
   * (it will be constructed automatically based on the text language).
   *
   * @param text The complete text to be typed
   * @param language The language of the text
   * @param enableAcceptingPrefixes see {@link #enableAcceptingPrefixes}
   * @see #TextInputModel(String, Language, TextCharCounts, boolean)
   */
  public TextInputModel(@Nonnull String text, @Nonnull Language language, boolean enableAcceptingPrefixes) {
    this(text, language, new TextCharCounts(language.getTokenizer().tokenize(text), language), enableAcceptingPrefixes);
  }

  /**
   * @param text The complete text to be typed
   * @param language The language of the text
   * @param charCounts The character counts for the words in the text
   * @param enableAcceptingPrefixes see {@link #enableAcceptingPrefixes}
   * @see #TextInputModel(String, Language, boolean)
   */
  public TextInputModel(@Nonnull String text, @Nonnull Language language, @Nonnull TextCharCounts charCounts, boolean enableAcceptingPrefixes) {
    this.enableAcceptingPrefixes = enableAcceptingPrefixes;
    this.text = requireNonNull(text, "text");
    textLanguage = requireNonNull(language, "language");
    textCharCounts = requireNonNull(charCounts, "charCounts");
    acceptedCharTimings = new int[this.text.length()];
  }

  /**
   * Start the clock that will be used to populate {@link #acceptedCharTimings}.
   *
   * @throws IllegalStateException if already invoked
   * @see TypingLog#getCharTimings()
   */
  public void startTiming() {
    startTiming(BridgeTypeFactory.newDuration());  // timing starts as soon as the Duration object is created
  }

  /**
   * Unit tests can call this method instead of {@link #startTiming()} to use a mock timer.
   */
  @VisibleForTesting
  void startTiming(Duration duration) {
    Preconditions.checkState(this.duration == null, "Already timing");
    this.duration = duration;
  }

  /**
   * @return {@code true} iff all the characters in the {@link #text} have been "accepted".
   */
  public boolean isFinished() {
    return wordCursor >= textCharCounts.getWordCount();
  }

  /**
   * Should be called every time the value of the text input element is modified.
   * <p>
   * If a prefix of the input matches the next substring of the {@link #text}, the chars in that prefix are considered
   * "accepted", and {@link #acceptedCharTimings} are updated accordingly for each one.
   * Either way, a new {@link TypingEdit} (representing the {@linkplain Levenshtein#editSequence(String, String) diffs}
   * from the {@linkplain #lastInput last input value}) is appended to the {@link #editLog}.
   * <p>
   * If the "accepted" prefix spans one or more {@linkplain TextCharCounts#isWordBoundary word boundaries} in the text,
   * the {@link #wordCursor} will be updated accordingly, and, if {@link #enableAcceptingPrefixes} is enabled,
   * the the result will indicate what the {@linkplain TextInputUpdate#newInputValue new value} of text input field
   * should be after this update.
   * <strong>NOTE:</strong> in order for this method to function correctly, the UI must ensure that its
   * input element is kept in-sync with this value.
   * <p>
   * If the given argument is equal to the {@linkplain TextInputUpdate#newInputValue new value} from the last update,
   * or if the recording is {@linkplain #isFinished() finished}, this method returns {@code null}.
   * To disambiguate the meaning of a {@code null} return value, the caller can check {@link #isFinished()}.
   *
   * @param input current the value of the text input field
   * @return the result of this update (i.e. what's changed since the last invocation of this method);
   *     returns {@code null} if nothing's changed or the text is {@linkplain #isFinished() finished}.
   * @throws IllegalStateException if {@link #startTiming()} hasn't been invoked yet
   */
  public @Nullable TextInputUpdate update(@Nonnull final String input) {
    Preconditions.checkNotNull(input);
    Preconditions.checkState(duration != null, "startTiming() hasn't been called yet");

    if (isFinished() || input.equals(lastInput)) {
      return null;  // the input hasn't changed since last time (e.g. del key pressed at the end of the word, or a letter overwrites the same letter that was selected)
      // TODO: consider providing a better way to disambiguate (maybe never return null, and instead use a different subclass of TextInputUpdate)
    }

    final int time = (int)duration.elapsedMillis();

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
      if (enableAcceptingPrefixes && textCharCounts.isWordBoundary(newCursor))  {
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
    Levenshtein.EditSequence edits = Levenshtein.editSequence(lastInput, input, true, false);
    editLog.add(new TypingEdit(charCursor, edits.getOperations(), time));
    /*
    NOTE: the above call to Levenshtein.editSequence deliberately sets the commonPrefixPossible=true
    and commonSuffixPossible=false in order to more-accurately reflect the use-case of typing text.

    For example, if the user types "F", "o", "o":
      editSequence("Fo", "Foo", true, true) returns [+(1, 'o')] whereas
      editSequence("Fo", "Foo", true, false) returns [+(2, 'o')], which is the one we want

    TODO:
      Perhaps could use info from the UI event (e.g. cursor position before/after, InputEvent.inputType, etc.)
      to improve the edit sequence to more accurately reflect the user's action
    */

    // what do we want to know after the update:
    // 0) acceptedPrefixLength: have any words been accepted (i.e. are to be cleared)?
    // 1) what the new value of the text field should be (some words might have been accepted and are to be cleared)
    // 2) charCursor where the text field now starts (i.e. how much of the text that's been accepted)
    // 3) how many chars remaining in the input field are correct (used for error highlighting in the UI)
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
    if (lastAcceptedCharIndex >= 0) {
      wordCursor = textCharCounts.getWordCountAtCharPosition(lastAcceptedCharIndex + 1);
    }
    return new TextInputUpdate(acceptedPrefixLength, lastInput, charCursor, correctCharCount, wordCursor);
  }

  /**
   * The return value of each call to {@link #update(String)}; provides the following information:
   * <ul>
   *   <li>have any words been accepted (i.e. are to be cleared)?
   *     &mdash; {@link #getAcceptedInputPrefixLength()}</li>
   *   <li>what the new value of the text field should be (some words might have been accepted and are to be cleared).
   *     &mdash; {@link #getNewInputValue()}</li>
   *   <li>char position within the {@link #text} where the input field now starts (i.e. how much of the text that's been accepted)
   *     &mdash; {@link #getNewCharCursor()}</li>
   *   <li>how many chars remaining in the text field are correct (used for error highlighting in the UI)
   *     &mdash; {@link #getCorrectInputPrefixLength()}</li>
   *   <li>how many full words have been typed correctly (used for sending progress updates to server)
   *     &mdash; {@link #getNewWordCursor()}</li>
   * </ul>
   */
  public static class TextInputUpdate {
    /**
     * The length of the prefix that should be cleared out of the text input field,
     * if any words have been accepted and have to be cleared from the text input field as a result of this update.
     * @see #enableAcceptingPrefixes
     */
    private final int acceptedInputPrefixLength;
    /**
     * What the new value of the text input element should be
     * (after stripping the prefix of {@link #acceptedInputPrefixLength}).
     * @see #enableAcceptingPrefixes
     * @see #lastInput
     */
    private final String newInputValue;
    /**
     * Character position within the full text where the text field should now start.
     * In other words, this indicates how much of the text has been accepted and removed from the input field at this point.
     */
    private final int newCharCursor;
    /**
     * The number of leading chars in {@link #newInputValue} that are correct.
     * This can be used for error highlighting in the UI.
     */
    private final int correctInputPrefixLength;
    /**
     * How many full words of the text have been accepted at this point.
     */
    private final int newWordCursor;

    public TextInputUpdate(int acceptedInputPrefixLength, @Nonnull String newInputValue, int newCharCursor, int correctInputPrefixLength, int newWordCursor) {
      this.acceptedInputPrefixLength = acceptedInputPrefixLength;
      this.newInputValue = requireNonNull(newInputValue, "newInputValue");
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

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      TextInputUpdate that = (TextInputUpdate)o;

      if (acceptedInputPrefixLength != that.acceptedInputPrefixLength)
        return false;
      if (newCharCursor != that.newCharCursor)
        return false;
      if (correctInputPrefixLength != that.correctInputPrefixLength)
        return false;
      if (newWordCursor != that.newWordCursor)
        return false;
      return newInputValue.equals(that.newInputValue);
    }

    @Override
    public int hashCode() {
      int result = acceptedInputPrefixLength;
      result = 31 * result + newInputValue.hashCode();
      result = 31 * result + newCharCursor;
      result = 31 * result + correctInputPrefixLength;
      result = 31 * result + newWordCursor;
      return result;
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("text", text)
        .add("charCursor", charCursor)
        .add("wordCursor", wordCursor)
        .add("acceptedCharTimings", acceptedCharTimings)
        .add("editLog", editLog)
        .toString();
  }

  public int getWordCursor() {
    return wordCursor;
  }

  public int getCharCursor() {
    return charCursor;
  }

  /**
   * @return the number of characters from the {@link #text} that have been recorded in {@link #acceptedCharTimings}.
   */
  public int getNumCharsAccepted() {
    return lastAcceptedCharIndex + 1;
  }

  public boolean isEnableAcceptingPrefixes() {
    // TODO(11/17/2021): come up with a better name for this field (and its getter)
    return enableAcceptingPrefixes;
  }

  public String getText() {
    return text;
  }

  @Nullable
  public TextInputUpdate getLastUpdateResult() {
    return lastUpdateResult;
  }

  /** Converts the stored info to a typing log */
  public TypingLog getTypingLog() {
    return new TypingLog(text, textLanguage, acceptedCharTimings, editLog);
  }

}