package solutions.trsoftware.commons.server.memquery.aggregations;

import solutions.trsoftware.commons.client.util.stats.MaxComparable;

/**
 * @author Alex, 1/9/14
 */
public class MaxInteger extends ColAggregation<Integer, Integer> {

  private MaxComparable<Integer> delegate = new MaxComparable<Integer>();

  @Override
  public Integer get() {
    return delegate.get();
  }

  @Override
  public void update(Integer x) {
    delegate.update(x);
  }
}
