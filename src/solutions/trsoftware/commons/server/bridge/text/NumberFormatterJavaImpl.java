package solutions.trsoftware.commons.server.bridge.text;

import solutions.trsoftware.commons.client.bridge.text.AbstractNumberFormatter;

import java.math.RoundingMode;
import java.text.NumberFormat;

/**
 * Java serverside implementation of NumberFormatter.
 *
 * @author Alex
 */
public class NumberFormatterJavaImpl extends AbstractNumberFormatter {
  private NumberFormat delegate;

  public NumberFormatterJavaImpl(int minIntegerDigits, int minFractionalDigits, int maxFractionalDigits, boolean digitGrouping, boolean percent) {
    super(minIntegerDigits, minFractionalDigits, maxFractionalDigits, digitGrouping);
    if (percent)
      delegate = NumberFormat.getPercentInstance();
    else
      delegate = NumberFormat.getNumberInstance();
    delegate.setMinimumFractionDigits(minFractionalDigits);
    delegate.setMaximumFractionDigits(maxFractionalDigits);
    delegate.setGroupingUsed(digitGrouping);
    delegate.setRoundingMode(RoundingMode.HALF_UP);  // set rounding to match GWT's NumberFormat
    delegate.setMinimumIntegerDigits(minIntegerDigits);
  }

  /**
   * Formats the given number using the constructor parameters to control how
   * the number will appear.
   */
  public String format(double number) {
    return delegate.format(number);
  }


}