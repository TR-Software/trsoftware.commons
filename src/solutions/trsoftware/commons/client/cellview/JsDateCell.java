package solutions.trsoftware.commons.client.cellview;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import solutions.trsoftware.commons.client.jso.JsDate;
import solutions.trsoftware.commons.client.jso.JsDateFormat;

import java.util.Date;

import static solutions.trsoftware.commons.client.jso.JsDateFormat.smartFormat;

/**
 * A cell that uses {@link JsDateFormat#smartFormat} to render {@link Date} values.
 *
 * @author Alex
 * @since 6/13/2025
 */
public class JsDateCell extends AbstractCell<Date> {

  @Override
  public void render(Context context, Date value, SafeHtmlBuilder sb) {
    if (value != null) {
      sb.appendEscaped(smartFormat(JsDate.create(value)));
    }
  }
}
