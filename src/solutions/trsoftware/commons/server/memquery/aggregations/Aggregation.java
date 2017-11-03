package solutions.trsoftware.commons.server.memquery.aggregations;

import solutions.trsoftware.commons.client.util.stats.Updatable;
import solutions.trsoftware.commons.server.memquery.HasValue;

/**
 * Marker interface for all aggregation functions, which encapsulate their state for all input values processed
 * so far, and can be updated with additional input values.
 *
 * @author Alex, 1/8/14
 */
public interface Aggregation<R, A> extends Updatable<A>, HasValue<R> {
}
