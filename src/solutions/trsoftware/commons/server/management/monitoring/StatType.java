package solutions.trsoftware.commons.server.management.monitoring;

import java.text.NumberFormat;

/**
 * Mar 26, 2011
 *
 * @author Alex
 */
public interface StatType {
  NumberFormat getPrintFormatter();

  String getName();

  String format(double value);
}
