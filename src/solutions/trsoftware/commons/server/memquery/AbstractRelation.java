package solutions.trsoftware.commons.server.memquery;

/**
 * A relation (aka "table").
 *
 * @author Alex, 1/15/14
 */
public abstract class AbstractRelation implements Relation {

  /** The schema of this relation */
  protected final RelationSchema schema;

  protected AbstractRelation(RelationSchema schema) {
    this.schema = schema;
  }

  @Override
  public RelationSchema getSchema() {
    return schema;
  }

  @Override
  public String getName() {
    return schema.getName();
  }
}
