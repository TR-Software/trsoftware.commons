package solutions.trsoftware.commons.server.memquery.struct;

import java.util.List;

/**
 * A tuple whose elements can be accessed by name.
 *
 * @author Alex, 1/15/14
 */
public interface NamedTuple extends Tuple {
  <T> T getValue(String name);
  List<String> getNames();
}
