package solutions.trsoftware.commons.server.management.monitoring;

import java.text.NumberFormat;

/**
 * Mar 29, 2011
 *
 * @author Alex
 */
public class IntegerStatType implements StatType {
  private final static NumberFormat intFormatter = NumberFormat.getIntegerInstance();
  private String name;

  public IntegerStatType(String name) {
    this.name = name;
  }

  public NumberFormat getPrintFormatter() {
    return intFormatter;
  }

  public String format(double value) {
    return intFormatter.format(value);
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }
}
