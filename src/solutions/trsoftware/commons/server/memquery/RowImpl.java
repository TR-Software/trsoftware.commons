package solutions.trsoftware.commons.server.memquery;

import solutions.trsoftware.commons.server.memquery.schema.ColSpec;
import solutions.trsoftware.commons.server.memquery.struct.MutableNamedTuple;
import solutions.trsoftware.commons.server.memquery.struct.MutableOrderedTuple;

import java.util.Arrays;

/**
* @author Alex, 1/14/14
*/
public class RowImpl extends AbstractRow implements MutableOrderedTuple<RowImpl>, MutableNamedTuple<RowImpl> {

  private final Object[] data;

  public RowImpl(RelationSchema schema) {
    super(schema);
    this.data = new Object[size()];
  }

  @Override
  public <V> V getValue(int colIndex) {
    return (V)data[colIndex];
  }

  @Override
  public <V> V getValue(String colName) {
    return getValue(getColIndex(colName));
  }

  @Override
  public <V, T> T setValue(int colIndex, V value) {
    data[colIndex] = value;
    return (T)this; // for method chaining
  }

  @Override
  public <V, T> T setValue(String colName, V value) {
    setValue(getColIndex(colName), value);
    return (T)this; // for method chaining
  }

  protected int getColIndex(String colName) {
    return schema.getColIndex(colName);
  }

  @Override
  public Object getRawData() {
    return data;
  }

  /** Factory method for creating a new instance representing the given row transformed by the given schema */
  public static RowImpl transform(RelationSchema outputSchema, Row inputRow) {
    RowImpl outputRow = new RowImpl(outputSchema);
    for (ColSpec outputCol : outputSchema) {
      String name = outputCol.getName();
      outputRow.setValue(name, outputCol.getValue(inputRow));
    }
    return outputRow;
  }

  @Override
  public String toString() {
    return Arrays.toString(data);
  }

}
