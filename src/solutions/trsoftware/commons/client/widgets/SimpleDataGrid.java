package solutions.trsoftware.commons.client.widgets;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.shared.util.MathUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * A simpler version of {@link CellTable} / {@link DataGrid}, based on a plain {@link Grid} widget.
 *
 * @param <T> row type
 * @author Alex
 * @since 9/16/2024
 */
public class SimpleDataGrid<K, T> extends Composite {

  private final List<Column<T, ?, ?>> columns;
  private final Map<K, Row> rowsByKey = new LinkedHashMap<>();
  private final Map<K, T> itemsByKey = new LinkedHashMap<>();
  private ArrayList<Row> rows = new ArrayList<>();
  private final Grid table;
  private Map<K, T> unmodifiableEntries;
  /**
   * Will be passed the item and {@code <tr>} element whenever a new row is added to the table
   */
  private RowListener<T> rowListener;

  public SimpleDataGrid(List<? extends Column<T, ?, ?>> columns) {
    this.columns = ImmutableList.copyOf(requireNonNull(columns, "columns"));
    table = new Grid(1, columns.size());
    for (int i = 0; i < columns.size(); i++) {
      Column<T, ?, ?> column = columns.get(i);
      column.colIndex = i;
      table.setText(0, i, column.getLabel());
      column.initHeaderCell(table.getCellFormatter().getElement(0, i));
    }
    initWidget(table);
  }

  public void insertOrUpdate(K key, T item) {
    insertOrUpdate(key, item, rows.size());
  }

  public void insertOrUpdate(K key, T item, int preferredRowIndex) {
    requireNonNull(key, "key");
    requireNonNull(item, "item");
    if (itemsByKey.containsKey(key)) {
      rowsByKey.get(key).update(item);
    } else {
      int listIndex = MathUtils.restrict(preferredRowIndex, 0, rows.size());
      Row row = insertRow(listIndex, item);
      itemsByKey.put(key, item);
      rowsByKey.put(key, row);
      // TODO: can probably get rid of one of these maps
    }
  }

  public boolean remove(K key) {
    Row row = rowsByKey.remove(key);
    if (row != null) {
      removeRow(row.rowIndex - 1);
      itemsByKey.remove(key);
      return true;
    }
    return false;
  }

  private Row insertRow(int listIndex, T item) {
    int tableRowIndex = listIndex + 1;  // +1 for header row
    table.insertRow(tableRowIndex);
    // Note: have to call table.insertRow before the Row constructor to avoid IOOBE when Row() tries to access the cell elements
    Row row = new Row(tableRowIndex, item);
    rows.add(listIndex, row);
    updateRowIndices();
    return row;
  }

  private void removeRow(int index) {
    table.removeRow(index + 1); // +1 for header row
    rows.remove(index);
    updateRowIndices();
  }

  private void updateRowIndices() {
    for (int i = 0; i < rows.size(); i++) {
      Row row = rows.get(i);
      row.rowIndex = 1 + i;  // +1 for the header row
    }
  }

  public Map<K, T> getItemsByKey() {
    // TODO: temp for testing
    return unmodifiableEntries != null
        ? unmodifiableEntries
        : (unmodifiableEntries = Collections.unmodifiableMap(itemsByKey));
  }

  public Set<K> keySet() {
    return getItemsByKey().keySet();
  }

  public SimpleDataGrid<K, T> setRowListener(RowListener<T> rowListener) {
    this.rowListener = rowListener;
    return this;
  }

  public Row getRow(K key) {
    return rowsByKey.get(key);
  }

  @VisibleForTesting
  public Grid getTable() {
    return table;
  }

  public abstract static class Column<T, V, R> {

    @Nullable
    protected final String label;
    @Nullable
    protected String styleName;
    protected final Function<T, V> valueExtractor;
    protected final BiFunction<T, V, R> valueRenderer;
    protected ValueChangeListener<T, V> valueChangeListener;
    /** Column index in {@link #table}.  Initialized by {@link #SimpleDataGrid(List)} based on the index of this column in the column list. */
    private int colIndex;
    /** Whether the cell associated with this column should be updated every time the row object is updated */
    private boolean modifiable = true;

    public Column(@Nullable String label, Function<T, V> valueExtractor, BiFunction<T, V, R> valueRenderer) {
      this.label = label;
      this.valueExtractor = requireNonNull(valueExtractor, "valueExtractor");
      this.valueRenderer = requireNonNull(valueRenderer, "valueRenderer");
    }

    public Column(@Nullable String label, Function<T, V> valueExtractor, Function<V, R> valueRenderer) {
      this.label = label;
      this.valueExtractor = requireNonNull(valueExtractor, "valueExtractor");
      requireNonNull(valueRenderer, "valueRenderer");
      this.valueRenderer = (t, v) -> valueRenderer.apply(v);
    }

    @Nullable
    public String getLabel() {
      return label;
    }

    public V getValue(T t) {
      return valueExtractor.apply(t);
    }

    public R render(T t, V v) {
      return valueRenderer.apply(t, v);
    }

    /**
     * Applies styles to the cell corresponding to this column in the table header row
     * @param element the {@code <td>} element representing this column in the table header row
     */
    public void initHeaderCell(Element element) {
      if (styleName != null)
        element.setClassName(styleName);
      String tooltip = getTooltip();
      if (tooltip != null) {
        element.setTitle(tooltip);
        element.getStyle().setCursor(Style.Cursor.HELP);
      }
    }
    /**
     * Applies styles to the cell corresponding to this column when a new row is added.
     * @param item the data object represented by the row
     * @param element the {@code <td>} element representing this column in the new row
     */
    public void initRowCell(T item, Element element) {
      // TODO: maybe replace with call to initHeaderCell, assuming the header cell also wants to apply the same styleName and tooltip
      if (styleName != null)
        element.setClassName(styleName);
      String tooltip = getTooltip();
      if (tooltip != null) {
        element.setTitle(tooltip);
        element.getStyle().setCursor(Style.Cursor.HELP);
      }
    }

    /**
     * Subclasses can override to specify a tooltip ({@code title} attribute) to be displayed for all cells in this column
     * (including the header cell)
     * @return the tooltip text to display or {@code null} if unspecified.
     */
    @Nullable
    public String getTooltip() {
      return null;
    }

    /**
     * Invoked when the value of the cell corresponding to the given row item in this column was updated.
     * @param item
     * @param element
     * @param oldValue
     * @param newValue
     */
    public void onValueChanged(T item, Element element, @Nullable V oldValue, V newValue) {
      if (valueChangeListener != null)
        valueChangeListener.onValueChange(item, element, oldValue, newValue);
    }

    public Column<T, V, R> setValueChangeListener(ValueChangeListener<T, V> valueChangeListener) {
      this.valueChangeListener = valueChangeListener;
      return this;
    }

    public int getColIndex() {
      return colIndex;
    }

    public boolean isModifiable() {
      return modifiable;
    }

    public Column<T, V, R> setModifiable(boolean modifiable) {
      this.modifiable = modifiable;
      return this;
    }

    @Nullable
    public String getStyleName() {
      return styleName;
    }

    public Column<T, V, R> setStyleName(@Nullable String styleName) {
      this.styleName = styleName;
      return this;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("i", colIndex)
          .add("label", label)
          .toString();
    }
  }

  public interface ValueChangeListener<T, V> {
    /**
     * Invoked when the value of a table data cell in a particular column was updated.
     * @param item the row item that was updated
     * @param element the cell element that was updated
     * @param oldValue the old value displayed in the cell
     * @param newValue the new value displayed in the cell
     */
    void onValueChange(T item, Element element, V oldValue, V newValue);
  }


  public static class TextColumn<T, V> extends Column<T, V, String> {

    public TextColumn(String label, Function<T, V> valueExtractor, BiFunction<T, V, String> valueRenderer) {
      super(label, valueExtractor, valueRenderer);
    }

    public TextColumn(String label, Function<T, V> valueExtractor, Function<V, String> valueRenderer) {
      super(label, valueExtractor, valueRenderer);
    }

    public TextColumn(String label, Function<T, V> valueExtractor) {
      super(label, valueExtractor, (Function<V, String>)String::valueOf);
    }

  }


  public class Row {
    /** Row index in {@link #table} */
    private int rowIndex;
    /** The data object currently displayed in this row. */
    private T item;

    public Row(int rowIndex, T item) {
      this.rowIndex = rowIndex;
      requireNonNull(item, "item");
      // init the cell values for this row (apply styles, etc.)
      for (Column<T, ?, ?> column : columns) {
        column.initRowCell(item, getCellElement(column));
      }
      // init the row element, if a rowListener was provided (e.g. to add particular styles or attributes to the row element based on the item being displayed in this row)
      if (rowListener != null)
        rowListener.rowAdded(item, getRowElement(), rowIndex);
      update(item);
    }

    private void update(T item) {
      requireNonNull(item, "item");
      for (Column<T, ?, ?> column : columns) {
        if (column.isModifiable() || this.item == null) {
          // if the column isn't modifiable, then the cell value will be updated only the the first time (this.item == null)
          maybeUpdateValue(column, item);
        }
      }
      this.item = item;
    }

    public T getItem() {
      return item;
    }

    public Element getCellElement(Column<T, ?, ?> column) {
      return getCellElement(column.getColIndex());
    }

    /**
     * Updates the text in the corresponding table cell only if its value has changed.
     * This is a perf optimization for DevMode, where every JSNI call incurs substantial overhead.
     *
     * @param <V> the type of value for this item attribute
     * @param column the column to update
     * @param item the object from which to obtain a new value for the cell
     * @return {@code true} if a new value was rendered
     *   (either the value actually changed or this is the first time it's being rendered)
     */
    private <V, R> boolean maybeUpdateValue(Column<T, V, R> column, T item) {
      // Note: if this.item == null, then this is the first time the value being rendered (update invoked by constructor)
      V oldValue = this.item != null ? column.getValue(this.item) : null;
      V newValue = column.getValue(item);
      boolean changed = false;
      if (!Objects.equals(newValue, oldValue)) {
        // this is either the first time the value being rendered (this.item == null: update invoked by constructor)
        // or the value has changed
        R textOrWidget = column.render(item, newValue);
        if (textOrWidget instanceof Widget) {
          table.setWidget(rowIndex, column.colIndex, (Widget)textOrWidget);
        } else {
          table.setText(rowIndex, column.colIndex, String.valueOf(textOrWidget));
        }
        changed = true;
        if (this.item != null) {
          // notify if the value actually changed (i.e. not the first time it's being rendered)
          column.onValueChanged(item, getCellElement(column), oldValue, newValue);
        }
      }
      return changed;
    }

    /**
     * @return the {@code <tr>} element representing this row in the table
     */
    public Element getRowElement() {
      return table.getRowFormatter().getElement(rowIndex);
    }

    /**
     * @return the {@code <td>} element representing the given column in this row
     */
    public Element getCellElement(int colIndex) {
      return table.getCellFormatter().getElement(rowIndex, colIndex);
    }
  }


  public interface RowListener<T> {

    default void rowAdded(T rowItem, Element rowElement, int rowIndex) {}

    default void rowRemoved(T item) {}
  }

}
