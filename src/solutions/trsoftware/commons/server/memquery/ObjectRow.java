package solutions.trsoftware.commons.server.memquery;

/**
 * Represents an object in an O-R mapping.
 * @author Alex, 1/16/14
 */
public class ObjectRow extends AbstractRow {

  private final Object object;

  public ObjectRow(RelationSchema schema, Object object) {
    super(schema);
    this.object = object;
  }

  @Override
  public Object getRawData() {
    return object;
  }

  @Override
  public String toString() {
    return object.toString();
  }
}
