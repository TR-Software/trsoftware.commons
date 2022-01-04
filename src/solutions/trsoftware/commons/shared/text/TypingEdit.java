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

import com.google.common.collect.ImmutableList;
import solutions.trsoftware.commons.shared.util.Levenshtein;

import java.util.List;

/**
 * Represents a single update to the value of a text field, which includes one or more
 * {@linkplain Levenshtein.EditOperation character diffs}.
 *
 * @see TypingLog
 * @see TextInputModel#update(String)
 * @since Nov 21, 2012
 * @author Alex
 */
public class TypingEdit {
  /**
   * The char position in the overall text where the text input field starts at the
   * time this entry was recorded.  This value added to {@link Levenshtein.EditOperation#pos} gives
   * the exact position of each edit in the overall text.
   * We store this value because some GIP implementations (namely those for
   * standard languages) will remove correct words from the input field,
   * so this value keeps track of where we are.  Other implementations
   * (e.g. for {@linkplain Language#isLogographic() logographic languages}) might never clear the input field, in which
   * case this value will always be {@code 0}).
   */
  private final int offset;
  /** The edits to the input recorded in this quantum of time */
  private final ImmutableList<Levenshtein.EditOperation> edits;
  /** The time since the start of the text input session (e.g. typing race) when this edit was recorded */
  private final int time;

  /**
   * @param offset {@link #offset}
   * @param edits {@link #edits}
   * @param time {@link #time}
   */
  public TypingEdit(int offset, List<Levenshtein.EditOperation> edits, int time) {
    this.offset = offset;
    this.edits = ImmutableList.copyOf(edits);
    this.time = time;
  }

  /**
   * @return {@link #offset}
   */
  public int getOffset() {
    return offset;
  }

  /**
   * @return {@link #time}
   */
  public int getTime() {
    return time;
  }

  /**
   * @return an immutable list of the text input diffs recorded in the time slice represented by this instance
   */
  public List<Levenshtein.EditOperation> getEdits() {
    return edits;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TypingEdit that = (TypingEdit)o;

    if (time != that.time) return false;
    if (offset != that.offset) return false;
    if (edits != null ? !edits.equals(that.edits) : that.edits != null)
      return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = offset;
    result = 31 * result + (edits != null ? edits.hashCode() : 0);
    result = 31 * result + time;
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("(").append(offset);
    sb.append(", ").append(time);
    sb.append(", ").append(edits);
    sb.append(')');
    return sb.toString();
  }
}
