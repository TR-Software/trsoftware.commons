package solutions.trsoftware.commons.client.widgets.input;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.util.StringUtils;

/**
 * Jan 17, 2010
 *
 * @author Alex
 */
public class DirtyAdvancedTextBoxTest extends CommonsGwtTestCase {


  public void testIsDirty() throws Exception {
    int visibleChars = 20;
    int maxChars = 50;
    for (String initialText : new String[]{"foo", ""}) {
      DirtyAdvancedTextBox dirtyTextBox = new DirtyAdvancedTextBox(visibleChars, maxChars, initialText, null);

      assertEquals(visibleChars, dirtyTextBox.getVisibleLength());
      assertEquals(maxChars, dirtyTextBox.getMaxLength());
      assertEquals("", dirtyTextBox.getText());  // initial text not modified

      // the box should not be dirty until we change its text
      assertFalse(dirtyTextBox.isDirty());
      dirtyTextBox.setText("bar");
      // change the text
      assertEquals("bar", dirtyTextBox.getText());
      assertTrue(dirtyTextBox.isDirty());
      // the box should not be dirty if we change its text back to its initial value
      dirtyTextBox.setText(initialText);
      assertFalse(dirtyTextBox.isDirty());
      assertEquals("", dirtyTextBox.getText());

      // try deleting the text, make sure it's dirty iff the initial text was not empty
      dirtyTextBox.setText("");
      assertEquals("", dirtyTextBox.getText());
      assertEquals(StringUtils.notBlank(initialText), dirtyTextBox.isDirty());
    }
  }

}