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
import com.google.common.collect.ImmutableList;
import solutions.trsoftware.commons.shared.util.Levenshtein;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * A typing log is a complete record of a user's inputs while typing a particular text, so that it can be
 * {@linkplain TypingLogAnalyzer analyzed} or played back visually.
 * <p>
 * Consists of two primary components:
 * <ol>
 *   <li>An array of timings when each character in the text was correctly typed by the user (see {@link #charTimings})</li>
 *   <li>A list of the {@linkplain Levenshtein.EditOperation edits} made during each time slice (see {@link #editLog})</li>
 * </ol>
 *
 * @see TypingEdit
 * @see TextInputModel#update(String)
 * @since Nov 23, 2012
 * @author Alex
 */
public class TypingLog {

  /**
   * Represents the time (in milliseconds) since the start of the input session
   * when each character in the text was "accepted" (i.e. typed correctly).
   * @see TextInputModel#update(String)
   */
  private final int[] charTimings;  // TODO: consider using ImmutableIntArray for this field

  /**
   * The underlying text that the user was supposed to type.  Each char in this string corresponds to an element
   * in the {@link #charTimings} array.
   */
  private final String text;

  /** The language of {@link #text} */
  private final Language textLanguage;

  /**
   * A full log of the edits made to the text input field while typing the {@link #text}.
   * <p>
   * Each entry represents the {@linkplain Levenshtein.EditOperation diffs} with the previous value of the text input
   * field recorded in a particular quantum of time since the start of the input session, and may have one or more
   * corresponding entries in the {@link #charTimings} array.
   */
  private final ImmutableList<TypingEdit> editLog;

  public TypingLog(@Nonnull String text, @Nonnull Language textLanguage,
                   @Nonnull int[] charTimings, @Nonnull List<TypingEdit> editLog) {
    this.text = requireNonNull(text);
    this.textLanguage = requireNonNull(textLanguage);
    this.charTimings = requireNonNull(charTimings);
    this.editLog = ImmutableList.copyOf(requireNonNull(editLog));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TypingLog typingLog = (TypingLog)o;

    if (!Arrays.equals(charTimings, typingLog.charTimings)) return false;
    if (!text.equals(typingLog.text)) return false;
    if (textLanguage != typingLog.textLanguage) return false;
    return editLog.equals(typingLog.editLog);
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(charTimings);
    result = 31 * result + text.hashCode();
    result = 31 * result + textLanguage.hashCode();
    result = 31 * result + editLog.hashCode();
    return result;
  }

  @Nonnull
  public String getText() {
    return text;
  }

  @Nonnull
  public Language getTextLanguage() {
    return textLanguage;
  }

  /**
   * Returns an array specifying the time (in millis since the start of the typing session) when each character in
   * the text was accepted (i.e. typed correctly).
   */
  @Nonnull
  public int[] getCharTimings() {
    return charTimings;
  }

  /**
   * Returns the number of typed characters that were <em>accepted</em> (i.e. typed correctly).
   * If the user didn't finish typing the entire text, this will be less than the length of {@link #charTimings}.
   * <p>
   * <strong>NOTE:</strong> this may not be the same as the number of chars that were <em>actually typed</em>,
   * which can be estimated by analyzing the {@linkplain #getEditLog() edit log}.
   *
   * @return the length of the longest prefix of the overall text that was <em>accepted</em> (i.e. typed correctly).
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

  @Nonnull
  public List<TypingEdit> getEditLog() {
    return editLog;
  }

  /**
   * @return the {@link TypingLogFormatV1} encoding of this log
   */
  @Override
  public String toString() {
    return TypingLogFormatV1.formatTypingLog(this);
  }

  public String toDebugString() {
    return MoreObjects.toStringHelper(this)
        .add("charTimings", charTimings)
        .add("editLog", editLog)
        .toString();
  }
}
