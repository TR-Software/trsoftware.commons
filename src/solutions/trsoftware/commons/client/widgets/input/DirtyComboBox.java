package solutions.trsoftware.commons.client.widgets.input;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.ListBox;

/**
 * A list box with only one visible row (combo box).  Keeps track of its
 * dirty state and implements HasText to facilitate bulk form validation
 * and submission (i.e. it's easy when all the input elements implement
 * HasText).
 *
 * Jan 18, 2010
 * @author Alex
 */
public class DirtyComboBox extends ListBox implements DirtyInput, HasText, ChangeHandler {
  private final int initialSelectedIndex;

  /**
   * Creates an empty combo box in single selection mode.
   *
   * @param items An array of 2-item string arrays representing name-value
   * pairs for the entries in the combo box.
   */
  public DirtyComboBox(String[][] items, String initialValue) {
    setVisibleItemCount(1);
    int initialIndex = 0;
    for (int i = 0; i < items.length; i++) {
      String[] item = items[i];
      String value = item[1];
      addItem(item[0], value);
      if (initialIndex == 0 && value.equals(initialValue))
        initialIndex = i; 
    }
    this.initialSelectedIndex = initialIndex;
    setSelectedIndex(initialSelectedIndex);
    setStyleName("DirtyComboBox");
    adjustStyle();
    addChangeHandler(this);
  }

  public void onChange(ChangeEvent event) {
    adjustStyle();
  }

  private void adjustStyle() {
    String styleSuffix = AdvancedTextBox.UNFOCUSED_STYLE_DEPENDENT_NAME;
    if (isDirty())
      removeStyleDependentName(styleSuffix);
    else
      addStyleDependentName(styleSuffix);
  }

  public boolean isDirty() {
    return getSelectedIndex() != initialSelectedIndex;
  }

  /** Sets the selected item whose value matches the argument, or the first item if no match found */
  public void setText(String value) {
    for (int i = 0; i < getItemCount(); i++) {
      if (getValue(i).equals(value)) {
        setSelectedIndex(i);
        return;
      }
    }
    setSelectedIndex(0);  // no match was found
  }

  /** Returns the value of the currently selected item */
  public String getText() {
    return getValue(getSelectedIndex());
  }
}
