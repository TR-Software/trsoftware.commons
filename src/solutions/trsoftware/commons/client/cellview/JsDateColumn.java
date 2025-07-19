package solutions.trsoftware.commons.client.cellview;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;

import java.util.Date;

/**
 * A column using {@link JsDateCell}.
 *
 * @author Alex
 * @since 6/13/2025
 */
public abstract class JsDateColumn<T> extends Column<T, Date> {

  /**
   * Construct a new Column with a given {@link Cell}.
   */
  public JsDateColumn() {
    this(new JsDateCell());
  }

  /**
   * Construct a new Column with a given {@link Cell}.
   *
   * @param cell the Cell used by this Column
   */
  public JsDateColumn(Cell<Date> cell) {
    super(cell);
  }
}
