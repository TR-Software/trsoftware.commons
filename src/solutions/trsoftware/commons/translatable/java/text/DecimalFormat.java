package java.text;

import com.google.gwt.i18n.client.NumberFormat;
import solutions.trsoftware.commons.client.text.CorrectedNumberFormat;

/**
 * The purpose of this class is to allow client/shared code referring to {@link java.text.DecimalFormat} to compile
 * with the GWT compiler.  The only usage of this is from {@link solutions.trsoftware.commons.shared.util.text.SharedNumberFormat}
 */
public class DecimalFormat {

  private final CorrectedNumberFormat format;

  public DecimalFormat(String pattern) {
    format = new CorrectedNumberFormat(NumberFormat.getFormat(pattern));
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

  /**
   * Synthesizes a pattern string that represents the current state
   * of this Format object.
   *
   * @return a pattern string
   * @see #applyPattern
   */
  public String toPattern() {
    return format.getPattern();
  }
}