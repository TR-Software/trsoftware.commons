package solutions.trsoftware.commons.client.cellview;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;

/**
 * TODO: document this
 * @author Alex
 * @since 5/8/2023
 */
public class SortableCellTable<T> extends BaseCellTable<T> {
  // TODO: experimental

  private final ListDataProvider<T> dataProvider;
  private final ListHandler<T> sortEventHandler;


  /**
   * @see ListDataProvider#ListDataProvider()
   */
  public SortableCellTable() {
    this(new ListDataProvider<>());
  }

  /**
   * @see ListDataProvider#ListDataProvider(ProvidesKey)
   */
  public SortableCellTable(ProvidesKey<T> keyProvider) {
    this(new ListDataProvider<>(keyProvider));
  }

  /**
   * @see ListDataProvider#ListDataProvider(List, ProvidesKey)
   */
  public SortableCellTable(List<T> listToWrap, ProvidesKey<T> keyProvider) {
    this(new ListDataProvider<>(listToWrap, keyProvider));
  }

  public SortableCellTable(@Nonnull ListDataProvider<T> dataProvider) {
    this.dataProvider = dataProvider;
    this.sortEventHandler = new ListHandler<>(dataProvider.getList());
    addColumnSortHandler(sortEventHandler);
    dataProvider.addDataDisplay(this);
  }

  /**
   * Adds the given column and makes it sortable using the given comparator.
   * @param comparator compares the values returned by {@link Column#getValue(Object)}
   * @param <C> the column type
   * @param <V> the column value type
   * @return the given column instance (for chaining)
   */
  protected <C extends Column<T, V>, V> C addSortableColumn(String name, C column, Comparator<V> comparator) {
    addColumn(name, column);
    column.setSortable(true);
    sortEventHandler.setComparator(column, Comparator.comparing(column::getValue, comparator));
    return column;
  }

  /**
   * Adds the given column and makes it sortable using the natural order of its value type.
   * @param <C> the column type
   * @param <V> the column value type
   * @return the given column instance (for chaining)
   */
  protected <C extends Column<T, V>, V extends Comparable<? super V>> C addSortableColumn(String name, C column) {
    addColumn(name, column);
    column.setSortable(true);
    sortEventHandler.setComparator(column, Comparator.comparing(column::getValue));
    return column;
  }

  /**
   *
   * @see ListDataProvider#setList(List)
   * @see ListHandler#setList(List)
   */
  public void setData(List<T> listToWrap) {
    dataProvider.setList(listToWrap);
    dataProvider.refresh();
    sortEventHandler.setList(dataProvider.getList());
    // re-apply the current sort after data is modified (see https://stackoverflow.com/a/33674657)
    ColumnSortEvent.fire(this, getColumnSortList());
  }
}
