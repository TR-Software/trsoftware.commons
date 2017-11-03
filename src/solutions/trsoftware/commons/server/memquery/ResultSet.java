package solutions.trsoftware.commons.server.memquery;

import java.io.PrintStream;
import java.util.Map;

/**
* @author Alex, 1/9/14
*/
public interface ResultSet extends MaterializedRelation {

  /** The query that produced this result set */
  MemQuery getQuery();

  void print(PrintStream out);

  /** Transforms this result set so it can be used as an input to another query */
  Map<String, Relation> asQueryInput();
}
