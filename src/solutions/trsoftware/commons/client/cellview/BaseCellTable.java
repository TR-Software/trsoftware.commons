package solutions.trsoftware.commons.client.cellview;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.view.client.NoSelectionModel;

/**
 * @author Alex, 9/19/2017
 */
public class BaseCellTable<T> extends CellTable<T> {

  protected void addColumn(String heading, Column<T, ?> col) {
    addColumn(col, heading);
  }

  protected void addColumn(Header<?> header, Column<T, ?> col) {
    addColumn(col, header);
  }

  protected void preventSelection() {
    setSelectionModel(new NoSelectionModel<T>());
    setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
  }
}
