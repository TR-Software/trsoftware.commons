package solutions.trsoftware.commons.server.memquery.schema;

import solutions.trsoftware.commons.server.memquery.Row;

/**
 * @author Alex, 6/2/2014
 */
public class RenamedColSpec<T> extends NameAccessorColSpec<T> {  // TODO: get rid of this class

  private final ColSpec<T> delegate;

  public RenamedColSpec(String newName, ColSpec delegate) {
    super(newName, delegate.getType());
    this.delegate = delegate;
  }

  @Override
  protected Object doGetValue(Row row) {
    return row.getValue(delegate.getName());
  }
}
