package solutions.trsoftware.commons.server.memquery.aggregations;

/**
 * @author Alex, 1/9/14
 */
public class Variance extends ColStats {

  @Override
  public Double get() {
    return numberSample.variance();
  }

}
