package solutions.trsoftware.commons.server.memquery;

import java.util.ArrayList;
import java.util.Iterator;

import static solutions.trsoftware.commons.client.util.CollectionUtils.asList;

/**
 * A relation that is materialized as an ArrayList of rows.
 *
 * @author Alex, 1/15/14
 */
public class ArrayListRelation extends AbstractRelation implements MaterializedRelation {

  protected final ArrayList<Row> rows;

  protected ArrayListRelation(RelationSchema schema, ArrayList<Row> rows) {
    super(schema);
    rows.trimToSize();
    this.rows = rows;
  }

  public ArrayListRelation(RelationSchema schema, Iterator<Row> rowIter) {
    this(schema, asList(rowIter));
  }

  public ArrayListRelation(Relation relation) {
    this(relation.getSchema(), relation.iterator());
  }

  @Override
  public Iterator<Row> iterator() {
    return rows.iterator();
  }

  @Override
  public ArrayList<Row> getRows() {
    return rows;
  }

  @Override
  public int size() {
    return rows.size();
  }
}
