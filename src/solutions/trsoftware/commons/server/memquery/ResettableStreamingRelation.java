package solutions.trsoftware.commons.server.memquery;

import solutions.trsoftware.commons.client.util.Resettable;
import solutions.trsoftware.commons.client.util.iterators.ResettableCachingIterator;

import java.util.Iterator;

/**
 * A {@link StreamingRelation} whose iterator can be reset.
 * @author Alex, 10/15/2016
 */
public class ResettableStreamingRelation extends StreamingRelation implements Resettable {

  public ResettableStreamingRelation(RelationSchema schema, Iterator<Row> rowIterator) {
    super(schema, new ResettableCachingIterator<Row>(rowIterator));
  }

  @Override
  public void reset() {
    ((Resettable)rowIterator).reset();
  }
}
