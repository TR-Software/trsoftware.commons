package solutions.trsoftware.commons.server.memquery;

import java.util.List;

/**
 * An in-memory relation that can be iterated more than once and its size is always known.
 *
 * @author Alex, 1/16/14
 */
public interface MaterializedRelation extends Relation {

  List<Row> getRows();

  int size();

}
