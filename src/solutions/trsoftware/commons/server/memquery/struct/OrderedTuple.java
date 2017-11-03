package solutions.trsoftware.commons.server.memquery.struct;

/**
 * A collection of items indexed by their ordinal number.
 *
 * @author Alex, 1/10/14
 */
public interface OrderedTuple extends Tuple {
  <T> T getValue(int index);
}
