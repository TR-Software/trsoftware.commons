package java.text;

import com.google.gwt.i18n.client.NumberFormat;

/**
 * The purpose of this class is to allow client/shared code referring to {@link java.text.DecimalFormat} to compile
 * with the GWT compiler.  The only usage of this is from {@link solutions.trsoftware.commons.shared.util.text.SharedNumberFormat}
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