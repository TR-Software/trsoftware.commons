package solutions.trsoftware.commons.client.widgets.input;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;

/**
 * Jan 18, 2010
 *
 * @author Alex
 */
public class MonthSelectorTest extends CommonsGwtTestCase {

  public void testMonthSelectorWithoutInitialValue() throws Exception {
    MonthSelector m = new MonthSelector("choose month", null);
    assertEquals(0, m.getSelectedIndex());
    assertEquals("choose month", m.getItemText(0));
    assertEquals("January", m.getItemText(1));
    assertEquals("December", m.getItemText(12));
    assertFalse(m.isDirty());
    m.setText("5");
    assertEquals("5", m.getText());
    assertEquals("May", m.getItemText(m.getSelectedIndex()));
    assertTrue(m.isDirty());
    m.setText("");  // select the empty row again
    assertEquals(0, m.getSelectedIndex());
    assertFalse(m.isDirty());
  }

  public void testMonthSelectorWithInitialValue() throws Exception {
    MonthSelector m = new MonthSelector("choose month", "4");
    assertEquals(4, m.getSelectedIndex());
    assertEquals("April", m.getItemText(m.getSelectedIndex()));
    assertEquals("January", m.getItemText(1));
    assertEquals("December", m.getItemText(12));
    assertFalse(m.isDirty());
    m.setText("5");
    assertEquals("5", m.getText());
    assertEquals("May", m.getItemText(m.getSelectedIndex()));
    assertTrue(m.isDirty());
    m.setText("4");  // select April again
    assertEquals(4, m.getSelectedIndex());
    assertFalse(m.isDirty());
  }

  public void testInitialValue() throws Exception {
    // check inidices which don't represent valid months
    assertEquals(0, new MonthSelector("choose month", null).getSelectedIndex());
    assertEquals(0, new MonthSelector("choose month", "").getSelectedIndex());
    assertEquals(0, new MonthSelector("choose month", "0").getSelectedIndex());
    assertEquals(0, new MonthSelector("choose month", "13").getSelectedIndex());

    // check indices of valid months
    assertEquals(1, new MonthSelector("choose month", "1").getSelectedIndex());
    assertEquals(2, new MonthSelector("choose month", "2").getSelectedIndex());
    assertEquals(12, new MonthSelector("choose month", "12").getSelectedIndex());
  }
}