package solutions.trsoftware.commons.server.memquery.schema;

/**
 * A partially-specified ColSpec (provides the name and type of the column, but not its value for a row).
 *
 * @author Alex, 1/13/14
 */
public abstract class NamedTypedColSpec<T> extends NamedColSpec<T> {

  private final Class<T> type;

  protected NamedTypedColSpec(String name, Class<T> type) {
    super(name);
    this.type = type;
  }

  @Override
  public Class<T> getType() {
    return type;
  }
}
