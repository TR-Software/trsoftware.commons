package solutions.trsoftware.commons.server.memquery;

/**
 * A sort order defined on a column.
 *
 * @author Alex, 1/12/14
 */
public class SortOrder implements HasName {

  private final String name;
  private final boolean reversed;

  public SortOrder(String name, boolean reversed) {
    this.name = name;
    this.reversed = reversed;
  }

  public String getName() {
    return name;
  }

  public boolean isReversed() {
    return reversed;
  }

  @Override
  public String toString() {
    String ret = name;
    if (reversed)
      ret += " DESC";
    return ret;
  }
}
