package solutions.trsoftware.commons.server.memquery.schema;

import solutions.trsoftware.commons.server.memquery.AggregationSpec;
import solutions.trsoftware.commons.server.memquery.aggregations.Aggregation;
import solutions.trsoftware.commons.server.memquery.util.AggregationUtils;

/**
 * A column in the output relation of an aggregation operation.  Sets its type based on the value type of the
 * aggregation's output.
 *
 * @author Alex, 1/8/14
 */
public class AggregatedColSpec extends NameAccessorColSpec<Aggregation> {

  private final AggregationSpec aggregationSpec;

  public AggregatedColSpec(AggregationSpec aggregationSpec) {
    super(aggregationSpec.getName(), AggregationUtils.getAggregationValueType(aggregationSpec.getType()));
    this.aggregationSpec = aggregationSpec;
  }

  public AggregationSpec getAggregationSpec() {
    return aggregationSpec;
  }
}
