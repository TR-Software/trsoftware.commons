package solutions.trsoftware.commons.server.memquery;

import solutions.trsoftware.commons.server.memquery.struct.MutableNamedTuple;
import solutions.trsoftware.commons.server.memquery.struct.MutableOrderedTuple;

/**
 * @author Alex
 * @since 1/8/2019
 */
public interface MutableRow extends Row, MutableOrderedTuple, MutableNamedTuple {
}
