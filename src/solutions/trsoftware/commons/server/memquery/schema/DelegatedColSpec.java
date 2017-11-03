package solutions.trsoftware.commons.server.memquery.schema;

import solutions.trsoftware.commons.server.memquery.Row;

/**
 * Delegates all methods to the encapsulated ColSpec.
 *
 * @author Alex, 1/5/14
 */
public class DelegatedColSpec<T> extends ColSpec<T> {

  // TODO: remove this class if it's no longer needed

  private final ColSpec<T> delegate;

  public DelegatedColSpec(ColSpec<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public T getValue(Row row) {
    return delegate.getValue(row);
  }

  @Override
  public Class<T> getType() {
    return delegate.getType();
  }
}
