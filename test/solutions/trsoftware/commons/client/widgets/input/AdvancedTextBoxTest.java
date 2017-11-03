/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.widgets.input;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.RootPanel;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;

import static solutions.trsoftware.commons.client.widgets.input.AdvancedTextBox.UNFOCUSED_STYLE_DEPENDENT_NAME;

/**
 * Jan 17, 2010
 *
 * @author Alex
 */
public class AdvancedTextBoxTest extends CommonsGwtTestCase {
  private int visibleChars = 20;
  private int maxChars = 50;
  private String initialText = "foo";
  private AdvancedTextBox boxWithPrompt;
  private AdvancedTextBox boxWithoutPrompt;

  /**
   * A replacement for JUnit's {@link #setUp()} method. This method runs once
   * per test method in your subclass, just before your each test method runs
   * and can be used to perform initialization. Override this method instead of
   * {@link #setUp()}.
   */
  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    boxWithPrompt = new AdvancedTextBox(visibleChars, maxChars, initialText, true, null);
    RootPanel.get().add(boxWithPrompt);  // add the box to the DOM in order for the focus listeners to work
    boxWithoutPrompt = new AdvancedTextBox(visibleChars, maxChars, initialText, false, null);
    RootPanel.get().add(boxWithoutPrompt);  // add the box to the DOM in order for the focus listeners to work
  }

  /**
   * A replacement for JUnit's {@link #tearDown()} method. This method runs once
   * per test method in your subclass, just after your each test method runs and
   * can be used to perform cleanup. Override this method instead of {@link
   * #tearDown()}.
   */
  @Override
  protected void gwtTearDown() throws Exception {
    boxWithPrompt = null;
    RootPanel.get().remove(boxWithPrompt);
    boxWithoutPrompt = null;
    RootPanel.get().remove(boxWithoutPrompt);
    super.gwtTearDown();
  }

  public void testBoxWithPrompt() throws Exception {
    checkBoxWithPrompt(visibleChars, maxChars, initialText, boxWithPrompt);
  }

  public void testRemovingPrompt() throws Exception {
    boxWithPrompt.setPromptText(null);
    checkBoxWithoutPrompt(visibleChars, maxChars, initialText, boxWithPrompt);
  }

  public void testWithoutPrompt() throws Exception {
    checkBoxWithoutPrompt(visibleChars, maxChars, initialText, boxWithoutPrompt);
  }

  public void testAddingPrompt() throws Exception {
    boxWithoutPrompt.setPromptText(initialText);
    checkBoxWithPrompt(visibleChars, maxChars, initialText, boxWithoutPrompt);
  }

  public void testInitializingWithEmptyPromptBehavesLikeNoPrompt() throws Exception {
    // we pass empty text to the constructor, and expect the box to behave as if it has no prompt
    String text = "";
    checkBoxWithoutPrompt(visibleChars, maxChars, text,
        new AdvancedTextBox(visibleChars, maxChars, text, true, null));
  }

  private void checkBoxWithPrompt(int visibleChars, int maxChars, final String initialText, final AdvancedTextBox textBox) {
    assertTrue(textBox.hasPrompt());
    assertEquals(initialText, textBox.getPromptText());

    assertEquals(visibleChars, textBox.getVisibleLength());
    assertEquals(maxChars, textBox.getMaxLength());
    assertEquals("", textBox.getText());  // initial text not modified

    // the box should have the unfocused style until it gains focus
    assertTrue(textBox.getStyleName().endsWith(UNFOCUSED_STYLE_DEPENDENT_NAME));
    textBox.setFocus(true);
    // the focus handler will be invoked in a separate thread, so we have to go into async mode
    delayTestFinish(5000);
    Scheduler.get().scheduleDeferred(new Command() {
      public void execute() {
        assertFalse(textBox.getStyleName().endsWith(UNFOCUSED_STYLE_DEPENDENT_NAME));

        // do the rest of the checks
        textBox.setText("bar");
        // change the text
        assertEquals("bar", textBox.getText());
        // the box should not return the prompt text if we change it back to the prompt
        textBox.setText(initialText);
        assertEquals("", textBox.getText());
        finishTest();
      }
    });
  }

  private void checkBoxWithoutPrompt(int visibleChars, int maxChars, final String initialText, final AdvancedTextBox textBox) {
    assertFalse(textBox.hasPrompt());
    assertNull(textBox.getPromptText());

    RootPanel.get().add(textBox);  // add the box to the DOM in order for the focus listeners to work
    assertEquals(visibleChars, textBox.getVisibleLength());
    assertEquals(maxChars, textBox.getMaxLength());
    assertEquals(initialText, textBox.getText());  // initial text should be returned by getText() since we're not using a prompt
    // the box should not have the "-unfocused" style to begin with since its not using a prompt
    assertFalse(textBox.getStyleName().endsWith(UNFOCUSED_STYLE_DEPENDENT_NAME));
    textBox.setFocus(true);
    // the focus handler will be invoked in a separate thread, so we have to go into async mode
    delayTestFinish(5000);
    Scheduler.get().scheduleDeferred(new Command() {
      public void execute() {
        assertFalse(textBox.getStyleName().endsWith(UNFOCUSED_STYLE_DEPENDENT_NAME));
        // change the text
        textBox.setText("bar");
        assertEquals("bar", textBox.getText());
        textBox.setText(initialText);
        assertEquals(initialText, textBox.getText());
        finishTest();
      }
    });
  }
}