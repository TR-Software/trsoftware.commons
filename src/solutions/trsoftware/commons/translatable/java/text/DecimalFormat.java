package java.text;

import com.google.gwt.i18n.client.NumberFormat;

/**
 * The purpose of this class is to allow Decimal format to exist in Shared code, even though it is never called.
 */
public class DecimalFormat {

  private final NumberFormat format;

  public DecimalFormat(String pattern) {
    format = NumberFormat.getFormat(pattern);
  }

  public String format(double value) {
    return format.format(value);
  }

  public Number parse(String source) throws ParseException {
    try {
      return format.parse(source);
    }
    catch (NumberFormatException e) {
      throw new ParseException(e.getMessage(), 0);
    }
  }
}