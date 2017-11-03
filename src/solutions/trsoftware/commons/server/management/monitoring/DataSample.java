package solutions.trsoftware.commons.server.management.monitoring;

/**
 * Mar 26, 2011
 *
 * @author Alex
 */
public interface DataSample {
  Number getByStatType(StatType statType);
  StatType[] getStatTypes();
  long getTime();
  String getName();
}
