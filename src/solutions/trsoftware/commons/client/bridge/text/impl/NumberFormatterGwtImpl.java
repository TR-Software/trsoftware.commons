package solutions.trsoftware.commons.client.bridge.text.impl;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.client.constants.NumberConstants;
import solutions.trsoftware.commons.client.bridge.text.AbstractNumberFormatter;
import solutions.trsoftware.commons.client.util.StringUtils;

/**
 * A superclass for two different implementations: the GWT and Java serverside.
 *
 * @author Alex
 */
public class NumberFormatterGwtImpl extends AbstractNumberFormatter {

  private NumberFormat delegate;

  private String pattern;
  /** True if this instance is formatting percentages */
  private final boolean percent;

  // ----- Constants -----
  /**
   * The number of characters to be separated by a digit grouping char
   * (example 3 in "1,000")
   */
  private static final int DIGIT_GROUP_SIZE = 3;
  /** Sets of constants as defined for the default locale. GWT's NumberFormat uses this under the hood. */
  private static final NumberConstants defaultNumberConstants = LocaleInfo.getCurrentLocale().getNumberConstants();

  // Localized versions of common symbols

  private static final String comma = defaultNumberConstants.groupingSeparator();
  private static final String dot = defaultNumberConstants.decimalSeparator();
  private static final String zero = "0";
  private static final String minus = defaultNumberConstants.minusSign();
  private static final String percentSymbol = defaultNumberConstants.percent();
  /** "0." (if in the US locale) */
  private static final String zeroDot = zero + dot;
  /** ".0" (if in the US locale) */
  private static final String dotZero = dot + zero;
  /** "-0" (if in the US locale) */
  private static final String minusZero = minus + zero;
  /** "-0." (if in the US locale) */
  private static final String minusZeroDot = minus + zeroDot;
  /** "-," (if in the US locale) */
  private static final String minusComma = minus + comma;

  public NumberFormatterGwtImpl(int minIntegerDigits, int minFractionalDigits, int maxFractionalDigits, boolean digitGrouping, boolean percent) {
    super(minIntegerDigits, minFractionalDigits, maxFractionalDigits, digitGrouping);
    this.percent = percent;
    StringBuilder formatString = new StringBuilder(StringUtils.repeat('0', minIntegerDigits));
    if (digitGrouping) {
      // need at least 4 leading characters to show digit grouping (e.g. #,##0)
      // pad the '0' string with '#' in front to make it length 4
      if (formatString.length() < 4)
        formatString.insert(0, StringUtils.repeat('#', 4 - formatString.length()));
      // insert grouping charaters every 3 digits from right to left
      for (int i = formatString.length() - DIGIT_GROUP_SIZE; i > 0; i -= DIGIT_GROUP_SIZE) {
        formatString.insert(i, ',');
      }
    }
    if (maxFractionalDigits > 0 || minFractionalDigits > 0) {
      formatString.append(".")
          .append(StringUtils.repeat('0', minFractionalDigits))
          .append(StringUtils.repeat('#', maxFractionalDigits - minFractionalDigits));
    }
    if (percent)
      formatString.append(percentSymbol);
    pattern = formatString.toString();
    delegate = NumberFormat.getFormat(pattern);
  }

  private native double round(double number) /*-{
    return Math.round(number);
  }-*/;

  public String format(double number) {
    // bug: GWT's NumberFormat.subformatFixed method has a rounding bug (using subtraction and Math.floor) loses precision
    // example: GWT yields "37.9612" when formatting 37.96125 to 4 places, whereas Java's fomatting correctly yields "37.9613"
    // so we round the number ourselves here, but only if the number is small enough to avoid overflow
    if (-1048576 < number && number < 1048576) {  // allow 20 (out of 64) bits for the integer part of the number
      // if the number is too big, we don't want to do this, because we'll introduce rounding error due to overflow
      int exponent = Math.max(minFractionalDigits, maxFractionalDigits);
      if (percent)
        exponent += 2;  // multiply by 100 if the number represents a percentage
      double multiplier = Math.pow(10d, exponent);
      // we can't use Java's Math.round because it either returns a long (which needs to be emulated) or its argument needs to be cast to float which will lose precision
      number = round(number * multiplier) / multiplier; // so we use native JS Math.round here
    }

    // now let GWT do its thing
    String str = delegate.format(number);

    // Fix bugs with GWT's formatting of certain kinds of numbers
    // see http://code.google.com/p/google-web-toolkit/issues/detail?id=3140

    // WARNING: these fixes will only work with left-to-right languages

    // bug 1: incorrectly prints the leading zero when minIntegerDigits=0, minFractionalDigits=0, digitGrouping=true
    if (minIntegerDigits == 0 && minFractionalDigits == 0 && digitGrouping) {
      if (str.startsWith(zeroDot))
        str = str.substring(zero.length());  // strip the leading 0
      else if (str.startsWith(minusZeroDot))
        str = minus + str.substring(minusZero.length()); // strip the leading "-0", but stick the minus back in front
    }

    // bug 2: incorrectly omits the digit grouping character among leading zeros
    if (digitGrouping && (str.startsWith(zero) || str.startsWith(minusZero))) {
      // fill in the digit grouping char: scan the string backwards either the end or the decimal separator
      int endPoint = str.indexOf(dot);
      if (endPoint < 0) {
        endPoint = str.length();
        // compensate for the string ending in a non-digit, such as the percent symbol
        for (int i = endPoint-1; i >= 0; i--) {
          if (Character.isDigit(str.charAt(i)))
            break;
          else
            endPoint--;
        }
      }
      StringBuilder temp = new StringBuilder(str);
      int step = DIGIT_GROUP_SIZE + 1;
      for (int i = endPoint - step; i >= 0; i -= step) {
        // WARNING: this might not work if groupingSeparator is longer than 1 char
        if (str.charAt(i) != comma.charAt(0)) {
          temp.insert(++i, comma);
        }
      }
      str = temp.toString();
    }

    // bug 3: .##(0.0015516379242909162) = .0, even when minFractionDigits == 0
    if (str.equals(dotZero) && minFractionalDigits == 0)
      str = "0";

    // bug 4: .#(0.968475310297362) = 1.0, when minFractionDigits=0 and digitGrouping=false
    if (!digitGrouping && minFractionalDigits == 0 && str.endsWith(dotZero))
      str = str.substring(0, str.length() - dotZero.length());

    // bug 5: java's NumberFormatter only prints as many digits as you get in the Java engineering notation representation of the number, even if maxFractionalDigits is more
    // but this is too hard to fix, so we just punt on this one

    // bug 6: checkFormatting(-4.007134372012819, 3, 4, 5, true): expected=-004.00713, actual=-,004.00713; pattern: #,000.0000#
    // the formatted number should never start with the digit grouping character
    if (digitGrouping && str.startsWith(comma))
      str = str.substring(comma.length());
    if (digitGrouping && str.startsWith(minusComma))
      str = minus + str.substring(minusComma.length());  // leave just the minus sign, remove the comma

    // bug 7: when pattern is ".#%" incorrectly prints a 0 after the decimal point, even though the fractional digit is optional
    if (minFractionalDigits == 0 && maxFractionalDigits == 1) {
      // delete the fractional part
      int fractionStart = str.indexOf(dotZero);
      if (fractionStart >= 0)
        str = str.substring(0, fractionStart) + str.substring(fractionStart+dotZero.length());
    }

    return str;
  }


  private int countDigits(String str) {
    int count = 0;
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (c >= '0' && c <= '9')
        count++;
    }
    return count;
  }


  public String getPattern() {
    return pattern;
  }
}

