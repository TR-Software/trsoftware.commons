package solutions.trsoftware.commons.server.memquery.schema;

import solutions.trsoftware.commons.server.memquery.HasName;
import solutions.trsoftware.commons.server.memquery.HasType;
import solutions.trsoftware.commons.server.memquery.ValueAccessor;

/**
 * A schema for a column in a table: has a name and a type, and is able to access its value for any row in the table.
 *
 * @author Alex, 1/5/14
 */
public abstract class ColSpec<T> implements ValueAccessor<T>, HasName, HasType<T> {


  @Override
  public String toString() {
    return String.format("('%s', %s)", getName(), getType().getSimpleName());
  }
}
