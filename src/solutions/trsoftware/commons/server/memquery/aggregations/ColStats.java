package solutions.trsoftware.commons.server.memquery.aggregations;

import solutions.trsoftware.commons.client.util.stats.NumberSampleOnlineDouble;

/**
 * @author Alex, 6/4/2014
 */
public abstract class ColStats extends ColAggregation<Double, Number> {

  protected NumberSampleOnlineDouble numberSample = new NumberSampleOnlineDouble();

  @Override
  public void update(Number x) {
    numberSample.update(x.doubleValue());
  }
}
