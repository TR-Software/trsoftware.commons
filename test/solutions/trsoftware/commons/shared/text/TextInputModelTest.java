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

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.testutil.MockDuration;
import solutions.trsoftware.commons.shared.text.TextInputModel.TextInputUpdate;
import solutions.trsoftware.commons.shared.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertArraysEqual;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;
import static solutions.trsoftware.commons.shared.util.Levenshtein.*;

/**
 * @author Alex
 * @since 11/11/2021
 */
public class TextInputModelTest extends TestCase {

  private String text;
  private Language language;
  private String[] words;
  private TextInputModel model;
  private int[] expectedCharTimings;
  private List<TypingEdit> expectedEditLog;
  private int charCursor;
  private int wordCursor;
  private MockDuration duration;
  private InputField inputField;
  private TextInputUpdate lastUpdate;


  /**
   * @param text {@link TextInputModel#text}
   * @param language {@link TextInputModel#textLanguage}
   * @param enableAcceptingPrefixes see {@link TextInputModel#enableAcceptingPrefixes}
   */
  private void initModel(String text, Language language, boolean enableAcceptingPrefixes) {
    this.text = text;
    this.language = language;
    words = language.getTokenizer().tokenize(text);
    model = new TextInputModel(text, language, enableAcceptingPrefixes);
    expectedCharTimings = new int[text.length()];
    expectedEditLog = new ArrayList<>();
    charCursor = 0;
    wordCursor = 0;
    duration = new MockDuration();
    // simulates the text input field that the TextInputModel is attached to
    inputField = new InputField();

    // assert exceptions thrown by model.update when preconditions not satisfied
    //   a) null input argument
    assertThrows(NullPointerException.class, () -> model.update(null));
    //   b) startTiming() hasn't been called yet
    assertThrows(IllegalStateException.class, () -> model.update("F"));
    model.startTiming(duration);

    System.out.println(Strings.lenientFormat("%s initialized for text \"%s\" (%s)",
        model.getClass().getSimpleName(), text, language.name()));
  }


  public void testUpdate() throws Exception {
    startUpdate();
    // finish typing the rest of the text normally, and check the final state of the model
    while (wordCursor < words.length) {
      typeNextWord();
    }
    /*
      TODO:
        what happens if we try to keep going?
          - it just keeps appending edits to the TypingLog without advancing any cursors
        what if we're on the last word and we insert more chars than needed in a single update (e.g. if last word is "bar." and we insert "bar.foo")?
          - will probably accept "bar.", and generate a log update for "bar.foo"
        This could be a problem for NIP, if it doesn't catch the last char event precisely.
        Should we stop the log after the last char is reached and reject any additional updates?
    */
    verifyFinalState();
  }

  public void testUpdate2() {
    startUpdate();
    // type out the rest of the text normally, up to the last word
    while (wordCursor < words.length-1)
      typeNextWord();
    String word = words[words.length-1];
    int midPoint = word.length() / 2;
    String prefix = word.substring(0, midPoint);  // "wor"
    String suffix = word.substring(midPoint);  // "ld."
    inputField.insert(suffix).applyUpdate(0)
        .verifyResult(0, inputField.getValue(), 0);
    String extra = "123";
    inputField.insert(prefix + extra).applyUpdate(0)
        .verifyResult(0, inputField.getValue(), 0);
    // current input value "ld.wor123"
    // now "drag & drop" the suffix into the correct position as a single update
    // "ld.wor123" -> "world.123"
    {
      InputField.Update update = inputField.shift(0, suffix.length(), prefix.length()).applyUpdate(word.length());
      // update our expectations
      charCursor += word.length();
      wordCursor++;
      update.verifyResult(word.length(), extra, 0);
      // asdf
    }
    verifyFinalState();
  }

  public void testUpdateLogographic() throws Exception {
    // test with the TIM.enableAcceptingPrefixes setting
    /* TODO: test the following assumptions:
        - TIU.newInputValue is always the same as the input
        - charCursor is never updated (or should it be?)
    */
    // TODO: maybe also test enableAcceptingPrefixes=false with a standard language (e.g. ENGLISH)
    initModel("asdfqwerty", Language.CHINESE, false);

    // type the first word ("a") correctly
    int i = 0;
    // TODO: extract method for Update.verifyResult, since the args are the same every time in this test
    inputField.insert(words[i++]).applyUpdate(++wordCursor)
        .verifyResult(0, inputField.getValue(), wordCursor);
    // type the next 2 words with some arbitrary chars in-between (only the first word should be accepted)
    String junk = scramble(text, 2);
    inputField.insert(words[i++] + junk + words[i++]).applyUpdate(++wordCursor)
        .verifyResult(0, inputField.getValue(), wordCursor);
    // drag & drop the junk chars to the end of input (this should make the 3rd word accepted)
    inputField.shift(wordCursor, wordCursor+junk.length(), 1).applyUpdate(++wordCursor)
        .verifyResult(0, inputField.getValue(), wordCursor);

    // backspace-delete the junk at the end
    inputField.setCursorPos(inputField.length());
    for (int j = 0; j < junk.length(); j++) {
      inputField.backspace().applyUpdate(wordCursor)
          .verifyResult(0, inputField.getValue(), wordCursor);
    }
    // type out the rest of the words, stopping just before the last one
    while (i < words.length - 1) {
      inputField.insert(words[i++]).applyUpdate(++wordCursor)
              .verifyResult(0, inputField.getValue(), wordCursor);
    }
    assertEquals(text.substring(0, text.length()-1), inputField.getValue());
    // insert another junk string
    junk = scramble(text, 3);
    inputField.insert(junk).applyUpdate(wordCursor)
        .verifyResult(0, inputField.getValue(), wordCursor);
    // finish the text by inserting the final word into the correct position (just before the junk we just entered)
    inputField.moveCursor(-junk.length()).insert(words[i]).applyUpdate(++wordCursor)
        .verifyResult(0, inputField.getValue(), wordCursor);
    verifyFinalState();
  }

  private void startUpdate() {
    // TODO: could turn this code into a general-purpose typing simulator that can be used for testing an input widget (e.g. NIP)

    initModel("Foo bar hello world.", Language.ENGLISH, true);

    // 1) input the first word, one char at a time, with no mistakes
    {
      // "Foo "
      assertEquals("Foo", words[wordCursor]);
      typeNextWord();
    }

    // 2) input the next word, making some mistakes deliberately
    {
      // "bar"
      String word = words[wordCursor];
      assertEquals("bar", word);
      assertEquals(0, inputField.length());  // input field should be empty (since the last word was fully accepted)

      // type 'b' (correct), followed by 2 incorrect chars
      inputField.insert('b').applyUpdate(1)
          .verifyResult(0, inputField.getValue(), 1);
      // type 2 arbitrary incorrect chars
      String wrongChars = "xy";
      for (int i = 0; i < wrongChars.length(); i++) {
        inputField.insert(wrongChars.charAt(i)).applyUpdate(1)
            .verifyResult(0, inputField.getValue(), 1);
      }
      // Backspace-delete everything (1 char at a time)
      for (int i = 0; i < wrongChars.length(); i++) {
        inputField.backspace().applyUpdate(1)
            .verifyResult(0, inputField.getValue(), 1);
      }
      inputField.backspace().applyUpdate(1)
          .verifyResult(0, inputField.getValue(), 0);
      assertEquals(0, inputField.length());

      // insert the full word as a single update (e.g. paste)
      inputField.insert(word).applyUpdate(word.length())
          .verifyResult(0, inputField.getValue(), word.length());

      // type the first few chars of the next word
      String nextWord = words[wordCursor + 1];
      String nextWordPrefix = nextWord.substring(0, nextWord.length() - 1);
      System.out.println("nextWord = " + nextWord); // TODO: temp
      for (int i = 0; i < nextWordPrefix.length(); i++) {
        char c = nextWord.charAt(i);
        inputField.insert(c).applyUpdate(word.length())  // no additional chars should've been accepted
            .verifyResult(0, inputField.getValue(), word.length());
      }

      // now insert a space in front of the 2nd word
      {
        int acceptedPrefixLength = word.length() + 1;
        TextInputUpdate result = inputField.setCursorPos(word.length()).insert(" ")
            .applyUpdate(acceptedPrefixLength + nextWordPrefix.length()).getResult();
        // the whole word, "bar ", plus the space after it should have been accepted at this point
        charCursor += acceptedPrefixLength;
        wordCursor++;
        verifyUpdateResult(result, acceptedPrefixLength, nextWordPrefix, nextWordPrefix.length());
        assertEquals(nextWordPrefix, inputField.getValue());
      }

      // now replace a substring of the input (to simulate paste over a selection)
      {
        int start = 1;
        int end = nextWordPrefix.length() - 1;
        String selection = nextWordPrefix.substring(start, end);
        inputField.replace(start, end, scramble(selection, selection.length() + 1))
            .applyUpdate(1) // the first char should still be correct
            .verifyResult(0, inputField.getValue(), 1);
      }
      // now delete everything (to simulate delete selection)
      inputField.delete(0, inputField.length())
          .applyUpdate(0)
          .verifyResult(0, "", 0);
    }
  }

  private void verifyFinalState() {
    int i = 0;
    String nextInput = inputField.getValue();
    do {
      // make sure we finished inputting the whole text
      assertEquals(words.length, model.getWordCursor());
      assertEquals(model.isEnableAcceptingPrefixes() ? text.length() : 0,
          model.getCharCursor());
      assertEquals(text.length(), model.getNumCharsAccepted());
      // any subsequent invocations of mode.update should return null and not change the state in any way
      nextInput += RandomUtils.randString(3);
      assertNull(submitUpdate(nextInput));
      i++;
    }
    while (i < 3);
    System.out.println("Final typing log:");
    System.out.println(model.getTypingLog().toString());
  }

  /**
   * Inputs the given word exactly (without mistakes), one char at a time, and verifies that the word was accepted
   * by the model after inputting the last character in the given string.  The last character in the given
   * string should be the word boundary, unless it's the last word in the text.
   */
  private void typeWord(String word) {
    for (int i = 0; i < word.length(); i++) {
      // this loop types out the exact chars in the word, one-by-one
      char c = word.charAt(i);
      TextInputUpdate result = inputField.insert(c).applyUpdate(i+1).getResult();

      if (i < word.length() - 1) {
        // typed one of the chars before the word boundary.
        verifyUpdateResult(result, 0, inputField.getValue(), inputField.length());
      } else {
        // typed the space at the end of the word; the full word should now be accepted and the input field cleared
        charCursor += word.length();
        wordCursor++;
        verifyUpdateResult(result, word.length(), "", 0);
      }
    }
  }

  /**
   * Calls {@link #typeWord(String)} for the next word in the text (as determined by {@link #wordCursor}).
   */
  private void typeNextWord() {
    String word = words[wordCursor];
    // append the word boundary char if this isn't the last word in the text
    if (wordCursor < words.length - 1)
      word += language.getTokenizer().getDelimiter();
    typeWord(word);
  }


  /**
   * Returns a random string of the given length, guaranteed not to contain any chars from the original string.
   * 
   * @param s the chars to exclude from the result
   * @param len length of the desired random string
   */
  private String scramble(String s, int len) {
    Set<Character> uniqueChars = SetUtils.difference(StringUtils.toCharacterSet(StringUtils.ASCII_PRINTABLE_CHARS), StringUtils.toCharacterSet(s));
    assertFalse("Unable to compute unique string", uniqueChars.isEmpty());
    Character[] chars = uniqueChars.toArray(new Character[0]);
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      sb.append(RandomUtils.randomElement(chars).charValue());
    }
    return sb.toString();
  }

  /**
   * @param acceptedInputPrefixLength see {@link TextInputUpdate#acceptedInputPrefixLength}
   * @param newInputValue see {@link TextInputUpdate#newInputValue}
   * @param correctInputPrefixLength see {@link TextInputUpdate#correctInputPrefixLength}
   */
  private void verifyUpdateResult(TextInputUpdate update,
                                  int acceptedInputPrefixLength, String newInputValue,
                                  int correctInputPrefixLength) {
    assertEquals("acceptedInputPrefixLength", acceptedInputPrefixLength, update.getAcceptedInputPrefixLength());
    assertEquals("newInputValue", newInputValue, update.getNewInputValue());
    assertEquals("charCursor", charCursor, update.getNewCharCursor());
    assertEquals("correctInputPrefixLength", correctInputPrefixLength, update.getCorrectInputPrefixLength());
    assertEquals("wordCursor", wordCursor, update.getNewWordCursor());
    verifyModelState();
  }

  /**
   * Asserts that the current {@link #model} state matches our expectations.
   */
  private void verifyModelState() {
    assertEquals(text, model.getText());
    assertEquals(wordCursor, model.getWordCursor());
    assertEquals(charCursor, model.getCharCursor());
    int nextCharIndex = ArrayUtils.indexOf(expectedCharTimings, 0);
    if (nextCharIndex >= 0)
      assertEquals(nextCharIndex, model.getNumCharsAccepted());
    else
      assertEquals(text.length(), model.getNumCharsAccepted());
    assertEquals(lastUpdate, model.getLastUpdateResult());
    assertEquals(wordCursor == words.length, model.isFinished());
    // check the TypingLog up to this point
    TypingLog typingLog = model.getTypingLog();
    assertArraysEqual(expectedCharTimings, typingLog.getCharTimings());
    assertEquals(text, typingLog.getText());
    assertEquals(language, typingLog.getTextLanguage());
    assertEquals(expectedEditLog, typingLog.getEditLog());
  }


  /**
   * Calls {@link TextInputModel#update(String)} and prints the result.
   * @return the result returned by {@link TextInputModel#update(String)}
   */
  private TextInputUpdate submitUpdate(String input) {
    lastUpdate = model.update(input);
    System.out.println(StringUtils.methodCallToString(
        "model.update", input) + ":\n  " + lastUpdate);
    System.out.println("  typingLog = " + model.getTypingLog().toDebugString());
//    System.out.println("  model = " + model);
    return lastUpdate;
  }


  /**
   * Simulates a text input field such as {@link com.google.gwt.user.client.ui.TextBox}
   */
  private class InputField {
    private StringBuilder value = new StringBuilder();
    /** cursor position within this text field */
    private int cursorPos;
    private int selectionLength;

    /**
     * Keeps track of any update currently in progress in order to ensure consistency of the input state.
     */
    private Update currentUpdate;

    // as12dfasdf

    /**
     * Inserts a single char at the current cursor position.
     *
     * @param c the char to insert at the current cursor position
     * @return a command object for submitting this update to {@link TextInputModel}
     */
    public Update insert(char c) {
      return insert(String.valueOf(c));
    }

    /**
     * Inserts the given string as a single update.
     *
     * @param s the string to insert at the current cursor position
     * @return a command object for submitting this update to {@link TextInputModel}
     */
    public Update insert(String s) {
      checkStateAndPrint("insert", s);

      ArrayList<EditOperation> editOps = insertImpl(cursorPos, s);
      setCursorPos(cursorPos + s.length());

      return currentUpdate = new Update(editOps);
    }

    private ArrayList<EditOperation> insertImpl(int cursorPos, String s) {
      value.insert(cursorPos, s);
      // generate the expected edit ops for inserted chars
      ArrayList<EditOperation> editOps = new ArrayList<>();
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        editOps.add(new Insertion(cursorPos +i, c));
      }
      return editOps;
    }

    /**
     * Delete a single character before the current cursor position.
     *
     * @return a command object for submitting this update to {@link TextInputModel}
     */
    public Update backspace() {
      return delete(cursorPos -1, cursorPos);
    }

    /**
     * Delete a substring of the input.
     *
     * @param start the starting index (inclusive)
     * @param end the ending index (exclusive)
     * @return a command object for submitting this update to {@link TextInputModel}
     */
    public Update delete(int start, int end) {
      checkStateAndPrint("delete", start, end);
      ArrayList<EditOperation> editOps = deleteImpl(start, end);
      setCursorPos(start);

      return currentUpdate = new Update(editOps);
    }

    private ArrayList<EditOperation> deleteImpl(int start, int end) {
      // generate the expected edit ops for the chars in the to-be-deleted range
      ArrayList<EditOperation> editOps = new ArrayList<>();
      for (int i = start; i < end; i++) {
        char c = value.charAt(i);
        editOps.add(new Deletion(start, c));
      }
      value.delete(start, end);
      return editOps;
    }

    /**
     * Replace a substring of the input with the given string.
     *
     * @param start the starting index (inclusive)
     * @param end the ending index (exclusive)
     * @return a command object for submitting this update to {@link TextInputModel}
     * @see StringBuilder#replace(int, int, String)
     */
    public Update replace(int start, int end, String rep) {
      checkStateAndPrint("replace", start, end, rep);
      String old = value.substring(start, end);

      // generate the expected edit ops for the chars in the to-be-replaced range
      ArrayList<EditOperation> editOps = new ArrayList<>();
      for (int i = 0; i < Math.max(old.length(), rep.length()); i++) {
        // TODO: this may not be the same as the actual Levenshtein.editSequence
        int pos = start+i;
        if (i < old.length() && i < rep.length()) {
          char oldChar = old.charAt(i);
          char newChar = rep.charAt(i);
          if (oldChar != newChar)
            editOps.add(new Substitution(pos, newChar));
        } else if (i < rep.length()) {
          editOps.add(new Insertion(pos, rep.charAt(i)));
        } else {
          editOps.add(new Deletion(pos, old.charAt(i)));
        }
      }
      value.replace(start, end, rep);
      setCursorPos(start + rep.length());
      return currentUpdate = new Update(editOps);
    }

    /**
     * Simulates a drag & drop of selected text as a single event.
     *
     * NOTE: a modern browser would issue 2 separate input events for this ('deleteByDrag', 'insertFromDrop')
     *
     * @param start the starting index of selection (inclusive)
     * @param end the ending index of selection (exclusive)
     * @param offset how far to shift the selection (shift right when offset > 0, otherwise left)
     * @return a command object for submitting this update to {@link TextInputModel}
     * @see StringBuilder#replace(int, int, String)
     */
    public Update shift(int start, int end, int offset) {
      checkStateAndPrint("shift", start, end, offset);
      String oldValue = value.toString();
      String sub = value.substring(start, end);
      // TODO: these edit ops not necessarily the same as Levenshtein.editSequence:
      deleteImpl(start, end);
      int destPos = start + offset;
      insertImpl(destPos, sub);
      setCursorPos(destPos);
      return currentUpdate = new Update(Levenshtein.editSequence(
          oldValue, getValue(), true, false).getOperations());
    }

    private void checkStateAndPrint(String methodName, Object... args) {
      System.out.println(toString() + "." + StringUtils.methodCallToString(methodName, args));
      checkState(currentUpdate == null, "Previous update not applied");
    }

    public int length() {
      return value.length();
    }

    public String getValue() {
      return value.toString();
    }

    public int getCursorPos() {
      return cursorPos;
    }

    public InputField setCursorPos(int cursorPos) {
      assertTrue("Invalid cursor position: " + cursorPos,
          NumberRange.inRange(0, length(), cursorPos));
      this.cursorPos = cursorPos;
      return this;
    }

    /**
     * Adjusts the cursor position by the given number of chars.
     * @param offset relative to the current cursor position.
     */
    public InputField moveCursor(int offset) {
      assertTrue(NumberRange.inRange(0, length(), cursorPos));
      setCursorPos(cursorPos + offset);
      return this;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .addValue(StringUtils.valueToString(value))
          .addValue(cursorPos)
          .toString();
    }

    /**
     * A command object returned by the {@link InputField} methods that are intended to submit an update to {@link TextInputModel}.
     * Caller must invoke {@link #applyUpdate(int)} on this command before attempting to create any additional updates.
     */
    class Update {
      private final int time;
      private final List<EditOperation> editOps;

      private TextInputUpdate result;


      Update(List<EditOperation> editOps) {
        this.time = (int)duration.incrementAndGet(10);
        this.editOps = editOps;
        currentUpdate = this;
      }

      /**
       * Submits this update to the {@link TextInputModel} after updating {@link #expectedCharTimings}
       * according to the given argument.
       *
       * @param nCorrect the number of leading chars in the input that should be accepted by the model as a result
       *     of applying this update; this will be used to update {@link #expectedCharTimings} accordingly
       * @return this instance, for chaining assertions with {@link #verifyResult(int, String, int)};
       *     use {@link #getResult()} to obtain the result returned by {@link TextInputModel#update(String)}
       */
      private Update applyUpdate(int nCorrect) {
        checkState(result == null, "Update already applied.");

        // update the expectations for the result
        for (int i = 0; i < nCorrect; i++) {
          int idx = charCursor + i;
          if (expectedCharTimings[idx] == 0)  // TODO: can extract method for updating expectedCharTimings
            expectedCharTimings[idx] = time;
        }
        expectedEditLog.add(new TypingEdit(charCursor, editOps, time));

        TextInputUpdate result = submitUpdate(value.toString());
        int acceptedPrefixLength = result.getAcceptedInputPrefixLength();
        if (acceptedPrefixLength > 0) {
          value.delete(0, acceptedPrefixLength);
          cursorPos = length();  // put the cursor at the end of whatever remains in the text field TODO: maybe better to just shift the current cursor position to the left?
          // make sure our text is in-sync with the model's expectation
          assertEquals(value.toString(), result.getNewInputValue());
        }
        System.out.println("inputField = " + inputField);
        System.out.println();
        currentUpdate = null;  // finished processing; must set to null to allow more updates
        this.result = result;
        return this;
      }

      /**
       * @return the result returned by {@link TextInputModel#update(String)}
       */
      public TextInputUpdate getResult() {
        checkState(result != null, "Result not available until update is applied");
        return result;
      }

      /**
       * Can be chained after {@link #applyUpdate(int)} to to verify assertions
       * on the result returned by {@link TextInputModel#update(String)}.
       * <p>
       * All arguments are passed through to {@link #verifyUpdateResult(TextInputUpdate, int, String, int)}.
       *
       * @return the result returned by {@link TextInputModel#update(String)}
       */
      public TextInputUpdate verifyResult(int acceptedInputPrefixLength, String newInputValue, int correctInputPrefixLength) {
        TextInputUpdate result = getResult();
        verifyUpdateResult(result, acceptedInputPrefixLength, newInputValue, correctInputPrefixLength);
        return result;
      }
    }

  }

}