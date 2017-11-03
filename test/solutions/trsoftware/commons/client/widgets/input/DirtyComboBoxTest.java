package solutions.trsoftware.commons.client.widgets.input;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;

/**
 * Jan 18, 2010
 *
 * @author Alex
 */
public class DirtyComboBoxTest extends CommonsGwtTestCase {

  public void testDirtyComboBox() throws Exception {
    DirtyComboBox box = new DirtyComboBox(new String[][]{
        {"foo", "x"},
        {"bar", "y"},
        {"baz", "z"}}, "y");

    assertEquals("y", box.getText());  // should start with "y" selected
    assertEquals(1, box.getSelectedIndex());
    assertFalse(box.isDirty());

    // should be able to use setText to select an item by value
    box.setText("z");
    assertEquals("z", box.getText());
    assertEquals(2, box.getSelectedIndex());
    assertTrue(box.isDirty());

    box.setText("y");
    assertEquals("y", box.getText());  // back to "y" selected
    assertEquals(1, box.getSelectedIndex());
    assertFalse(box.isDirty());
  }

}