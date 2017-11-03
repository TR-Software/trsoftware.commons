package solutions.trsoftware.commons.server.memquery;

import java.util.Iterator;

/**
 * A relation that is not fully stored in memory.  Instead, the rows are streamed using the provided
 * {@link #iterator()} method, which always returns the same iterator object for the stream of rows.
 * This is a good alternative to {@link ArrayListRelation} for representing the results of an operation
 * that can be pipelined.
 *
 * @author Alex, 1/15/14
 */
public class StreamingRelation extends AbstractRelation {

  protected final Iterator<Row> rowIterator;

  public StreamingRelation(RelationSchema schema, Iterator<Row> rowIterator) {
    super(schema);
    this.rowIterator = rowIterator;
  }

  @Override
  public Iterator<Row> iterator() {
    return rowIterator;
  }
}
