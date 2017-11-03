package solutions.trsoftware.commons.server.memquery.expressions;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Evaluates to true if the value of the given column is one of the given choices.
 *
 * @author Alex, 4/17/2015
 */
public class ColValueIn<T extends Comparable> extends ColValuePredicate<T> {

  private Set<T> choices;

  public ColValueIn(String colName, Collection<T> choices) {
    super(colName);
    this.choices = new LinkedHashSet<T>(choices);
  }

  @Override
  public boolean eval(T value) {
    return choices.contains(value);
  }

  @Override
  public String toString() {
    return new StringBuilder(colName).append(" IN ").append(choices).toString();
  }
}
