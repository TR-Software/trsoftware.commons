package solutions.trsoftware.commons.client.cellview;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.cellview.client.Column;

/**
 * @author Alex, 9/19/2017
 */
public class HtmlColumn<T> extends Column<T, T> {

  public HtmlColumn(final SafeHtmlRenderer<T> renderer) {
    super(new AbstractSafeHtmlCell<T>(renderer) {
      @Override
      protected void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
        // NOTE: it's unclear why AbstractSafeHtmlCell doesn't provide a default implementation of this method
        if (data != null) {
          sb.append(data);
        }
      }
    });
  }

  @Override
  public T getValue(T object) {
    return object;
  }

}
