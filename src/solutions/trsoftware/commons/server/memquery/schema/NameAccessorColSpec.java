package solutions.trsoftware.commons.server.memquery.schema;

import solutions.trsoftware.commons.server.memquery.Row;
import solutions.trsoftware.commons.server.memquery.aggregations.Aggregation;

/**
 * A col spec that fetches its values from a given row by attribute name.
 *
 * @param <T> the data type of the values in this column
 * @author Alex, 1/10/14
 */
public class NameAccessorColSpec<T> extends NamedTypedColSpec<T> {

  public NameAccessorColSpec(String name, Class<T> type) {
    super(name, type);
  }

  public NameAccessorColSpec(ColSpec inputCol) {
    this(inputCol.getName(), inputCol.getType());
  }

  @Override
  public T getValue(Row row) {
    Object value = doGetValue(row);
    if (value instanceof Aggregation)  // unpack aggregations
      value = ((Aggregation)value).get();
    return (T)value;
  }

  /** Subclasses can override this method */
  protected Object doGetValue(Row row) {
    return row.getValue(getName());
  }

}
