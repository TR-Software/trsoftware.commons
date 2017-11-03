package solutions.trsoftware.commons.server.memquery.aggregations;

import solutions.trsoftware.commons.server.memquery.Row;

/**
* @author Alex, 1/9/14
*/
public class Count extends RowAggregation<Integer> {

  private int count;

  @Override
  public Integer get() {
    return count;
  }

  @Override
  public void update(Row x) {
    count++;
  }
}
