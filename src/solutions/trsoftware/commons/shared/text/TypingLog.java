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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TypingLog is the successor of KeyLog.  It is produced in the UI by TextModel
 * (which replaces the functionality of KeyLogRecorder and more).
 *
 * A typing log consists of two parts:
 *
 * 1) An array of timings when each character in the text was correctly typed
 * by the user
 *
 * 2) A list of edits performed by the user (insertions, deletions, and
 * susbstitutions of characters in the text).
 *
 * Nov 23, 2012
 *
 * @author Alex
 */
public class TypingLog {
  /*
  Implementation decisions:
  1) Store the full text?
     This would add resilience to changes in texts at the price of much storage overhead.
     Answer: don't store the full text - it's better to prevent edits
     to texts (/admin/texts can do a query to check whether or not the text
     has been typed before, and if it has force the admin to create a new text instead)
  */

  /**
   * Represents the time since the start of the race, in milliseconds,
   * when each character in the text was accepted (i.e. typed correctly).
   */
  private int[] charTimings;

  /**
   * The underlying text that the user was supposed to type.  Each char in this string corresponds to an element
   * in the {@link #charTimings} array.
   */
  private String text;

  /** The language of {@link #text} */
  private Language textLanguage;

  /**
   * A full log of each update to the user's input.  Each entry encodes
   * which word the user was typing at the time, a sequence of one or more
   * edit operations (diffs), and the time since the beginning of the race. 
   */
  private List<TypingEdit> editLog = new ArrayList<TypingEdit>();

  public TypingLog(String text, Language textLanguage, int[] charTimings, List<TypingEdit> editLog) {
    this.text = text;
    this.textLanguage = textLanguage;
    this.charTimings = charTimings;
    this.editLog = editLog;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TypingLog typingLog = (TypingLog)o;

    if (!Arrays.equals(charTimings, typingLog.charTimings)) return false;
    if (editLog != null ? !editLog.equals(typingLog.editLog) : typingLog.editLog != null) return false;
    if (text != null ? !text.equals(typingLog.text) : typingLog.text != null) return false;
    if (textLanguage != typingLog.textLanguage) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = charTimings != null ? Arrays.hashCode(charTimings) : 0;
    result = 31 * result + (text != null ? text.hashCode() : 0);
    result = 31 * result + (textLanguage != null ? textLanguage.hashCode() : 0);
    result = 31 * result + (editLog != null ? editLog.hashCode() : 0);
    return result;
  }

  public String getText() {
    return text;
  }

  public Language getTextLanguage() {
    return textLanguage;
  }

  public int[] getCharTimings() {
    return charTimings;
  }

  /**
   * @return The number of characters that the user actually typed.  If the user didn't finish the race, this number will
   * be less than the length of {@link #charTimings}
   */
  public int getNumCharsTyped() {
    int nCharsTyped = 0;
    for (int i = 0; i < charTimings.length; i++) {
      int charTime = charTimings[i];
      if (charTime > 0)
        nCharsTyped++;
      else
        break;
    }
    return nCharsTyped;
  }

  public List<TypingEdit> getEditLog() {
    return editLog;
  }

  @Override
  public String toString() {
    return TypingLogFormatV1.formatTypingLog(this);
  }

}
