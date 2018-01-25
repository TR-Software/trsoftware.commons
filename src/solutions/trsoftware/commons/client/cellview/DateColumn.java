package solutions.trsoftware.commons.client.cellview;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.Column;

import java.util.Date;

/**
 * @author Alex, 9/19/2017
 */
public abstract class DateColumn<T> extends Column<T, Date> {

  public DateColumn() {
    super(new DateCell());
  }

  public DateColumn(DateTimeFormat format) {
    super(new DateCell(format));
  }

  // TODO: allow specifying a date format

}
