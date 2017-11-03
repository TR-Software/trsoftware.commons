package solutions.trsoftware.commons.server.memquery.aggregations;

import solutions.trsoftware.commons.server.memquery.Row;

/**
 * Marker interface for all aggregations that take an entire row as an argument.
 *
 * @author Alex, 1/13/14
 */
public abstract class RowAggregation<R> implements Aggregation<R, Row> {

}
