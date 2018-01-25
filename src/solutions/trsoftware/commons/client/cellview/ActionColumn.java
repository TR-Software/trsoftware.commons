package solutions.trsoftware.commons.client.cellview;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;

/**
 * @author Alex, 9/19/2017
 */
public class ActionColumn<T> extends Column<T, T> {

  public ActionColumn(String label, ActionCell.Delegate<T> delegate) {
    super(new ActionCell<T>(label, delegate));
  }

  public ActionColumn(Cell<T> cell) {
    super(cell);
  }

  @Override
  public T getValue(T object) {
    return object;
  }
}
