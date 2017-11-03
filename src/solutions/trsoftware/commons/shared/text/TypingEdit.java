package solutions.trsoftware.commons.shared.text;

import solutions.trsoftware.commons.client.util.Levenshtein;

import java.util.List;

/**
 * Nov 21, 2012
*
* @author Alex
*/
public class TypingEdit {
  /**
   * The char position in the overall text where the text input field starts at the
   * time this entry was recorded.  This value added to EditOperation.pos gives
   * the exact position of each edit in the overall text.
   * We store this value because some GIP implementations, namely those for
   * standard languages will remove correct words from the input field,
   * so this value keeps track of where we are.  Other implementations
   * (e.g. for logographic languages) never clear the input field, so this
   * value will always be 0 for those. 
   */
  private int offset;
  /** The edits to the input made in this quantum of time */
  private List<Levenshtein.EditOperation> edits;
  /** The time since the start of the race when this edit was recorded */
  private int time;

  public TypingEdit(int offset, List<Levenshtein.EditOperation> edits, int time) {
    this.offset = offset;
    this.edits = edits;
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
//      sb.append("TypingEdit");
    sb.append("(").append(offset);
    sb.append(", ").append(time);
    sb.append(", ").append(edits);
    sb.append(')');
    return sb.toString();
  }
}
