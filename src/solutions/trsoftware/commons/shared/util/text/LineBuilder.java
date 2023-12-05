package solutions.trsoftware.commons.shared.util.text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper for building multiline strings.
 */
public class LineBuilder {
  /**
   * The line builders created by each {@link #newLine()} invocation
   */
  private final ArrayList<StringBuilder> lines = new ArrayList<>();
  /**
   * The current line returned by {@link #line()}
   */
  private StringBuilder line;

  /**
   * Creates a new instance containing a single line
   */
  public LineBuilder() {
    newLine();
  }

  /**
   * Creates a {@link StringBuilder} for the next line and sets it as the {@linkplain #line() current line}
   */
  public void newLine() {
    lines.add(line = new StringBuilder());
  }

  /**
   * @return the {@link StringBuilder} for the current line
   */
  public StringBuilder line() {
    return line;
  }

  /**
   * @return the {@link StringBuilder} for the specified line
   * @throws IndexOutOfBoundsException if the index is out of range
   *         (<tt>index &lt; 0 || index &gt;= {@link #lineCount}()</tt>)
   */
  public StringBuilder line(int index) {
    return lines.get(index);
  }

  /**
   * @return the number of lines used so far
   */
  public int lineCount() {
    return lines.size();
  }

  /**
   * Invokes {@link StringBuilder#toString()} for each {@linkplain #lines line} and collects the results in a list.
   *
   * @return the {@linkplain StringBuilder#toString() string values} of all the lines
   * @see #buildLines(Collector)
   */
  public List<String> buildLines() {
    Collector<String, ?, List<String>> collector = Collectors.toList();
    return streamLines().collect(collector);
  }

  /**
   * Invokes {@link StringBuilder#toString()} for each {@linkplain #lines line} and joins the resulting strings
   * using the given delimiter.
   *
   * @param delim the delimiter sequence for joing the lines
   * @return the lines joined by the given delimiter
   */
  public String joinLines(String delim) {
    return streamLines().collect(Collectors.joining(delim));
  }

  private Stream<String> streamLines() {
    return lines.stream().map(StringBuilder::toString);
  }
}
