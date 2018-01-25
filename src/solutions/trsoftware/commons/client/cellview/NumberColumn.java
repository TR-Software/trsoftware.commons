package solutions.trsoftware.commons.client.cellview;

import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.Column;
import solutions.trsoftware.commons.shared.util.StringUtils;

/**
 * @author Alex, 9/19/2017
 */
public abstract class NumberColumn<T> extends Column<T, Number> {

  public NumberColumn() {
    super(new NumberCell());
  }

  public NumberColumn(int decimalPlaces) {
    this(getFormat(decimalPlaces));
  }

  public NumberColumn(NumberFormat format) {
    super(new NumberCell(format));
  }

  public static NumberFormat getFormat(int decimalPlaces) {
    String pattern;
    if (decimalPlaces == 0)
      pattern = "#";
    else if (decimalPlaces > 0)
      pattern = "." + StringUtils.repeat('#', decimalPlaces);
    else
      throw new IllegalArgumentException("decimalPlaces < 0");
    return NumberFormat.getFormat(pattern);
  }

}
