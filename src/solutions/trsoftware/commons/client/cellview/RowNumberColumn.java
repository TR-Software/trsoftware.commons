package solutions.trsoftware.commons.client.cellview;

import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;

/**
 * Renders the index (starting with 1) for a row in a {@link CellTable}.
 *
 * Borrowed from <a href="https://stackoverflow.com/questions/4347224/adding-a-row-number-column-to-gwt-celltable">StackOverflow</a>
 *
 * @author Alex, 10/16/2017
 */
public class RowNumberColumn<T> extends Column<T, Number> {

  public RowNumberColumn() {
    super(new NumberCell() {
      @Override
      public void render(Context context, Number value, SafeHtmlBuilder sb) {
        super.render(context, context.getIndex()+1, sb);
      }
    });
  }

  @Override
  public Number getValue(T object) {
    return null;
  }
}
