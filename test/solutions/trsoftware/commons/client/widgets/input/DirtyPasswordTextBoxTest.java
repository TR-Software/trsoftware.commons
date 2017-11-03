package solutions.trsoftware.commons.client.widgets.input;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;

/**
 * Jan 18, 2010
 *
 * @author Alex
 */
public class DirtyPasswordTextBoxTest extends CommonsGwtTestCase {

  public void testIsDirty() throws Exception {
    checkIsDirty("foo");
    checkIsDirty("");
  }

  private void checkIsDirty(String initialValue) {
    DirtyPasswordTextBox box = new DirtyPasswordTextBox(initialValue);
    assertFalse(box.isDirty());
    String newValue = "bar";
    assertFalse(newValue.equals(initialValue));
    box.setText(newValue);
    assertTrue(box.isDirty());
    box.setText(initialValue);
    assertFalse(box.isDirty());
  }
}