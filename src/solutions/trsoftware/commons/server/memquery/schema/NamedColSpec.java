package solutions.trsoftware.commons.server.memquery.schema;

/**
 * A partially-specified ColSpec (provides just the name of the column, but not its type or value for a row).
 *
 * @author Alex, 1/5/14
 */
public abstract class NamedColSpec<T> extends ColSpec<T> {

  private final String name;

  protected NamedColSpec(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

}
