/*
 * Copyright 2022 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.commons.client.widgets.input;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.TextBox;
import solutions.trsoftware.commons.client.event.EventHandlers;
import solutions.trsoftware.commons.shared.util.StringUtils;

/**
 * A text box containing some prompt text which disappears when the box gains focus.
 * This default prompt text is not returned by {@link #getText()}.
 * <p>
 * The CSS class {@code AdvancedTextBox-unfocused} can be used to style the prompt text.
 * <p>
 * <b>Update</b> (11/30/2021): The HTML standard now includes a {@code placeholder} attribute
 * (for {@code <input>} / {@code <textarea>}) and a {@code ::placeholder} pseudo-element (in CSS).
 * When combined, those two features make this widget somewhat obsolete.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input#attr-placeholder">
 *   HTMLInputElement.placeholder</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/::placeholder">
 *   ::placeholder (CSS)</a>
 * @see <a href="https://caniuse.com/?search=placeholder">
 *   Can I use "placeholder"?</a>
 *
 * @deprecated Made obsolete by the {@code placeholder} attribute for {@code <input>} / {@code <textarea>} elements
 * @author Alex
 */
public class AdvancedTextBox extends TextBox implements FocusHandler, BlurHandler {
  protected String promptText;
  protected boolean usePrompt;
  private boolean focusAndBlurHandlersAdded;
  protected static final String UNFOCUSED_STYLE_DEPENDENT_NAME = "unfocused";

  /**z
   * Shorthand for creating a text box with the given properties.
   * The properties whose values are null will not be applied.
   * @param usePrompt If true, the given text will only be used for prompting,
   * i.e. a gray version of it will be displayed whenever the text box doesn't
   * have focus.
   */
  public AdvancedTextBox(Integer visibleChars, Integer maxChars, String text,
                         boolean usePrompt, String styleName) {
    this(visibleChars, maxChars, text, usePrompt, null, null, styleName);
  }

  /**
   * Shorthand for creating a text box with the given properties.
   * The properties whose values are null will not be applied.
   * @param usePrompt If true, the given text will only be used for prompting,
   * i.e. a gray version of it will be displayed whenever the text box doesn't
   * have focus.
   */
  public AdvancedTextBox(Integer visibleChars, Integer maxChars, String text, boolean usePrompt,
                         Command onEnterKey, Command onEscapeKey, String styleName) {
    setStylePrimaryName("AdvancedTextBox");
    if (visibleChars != null)
      setVisibleLength(visibleChars);
    if (maxChars != null)
      setMaxLength(maxChars);
    if (styleName != null)
      addStyleName(styleName);
    if (onEnterKey != null || onEscapeKey != null)
      EventHandlers.addEnterAndEscapeKeyHandlers(this, onEnterKey, onEscapeKey);

    if (usePrompt && text != null)
      setPromptText(text);
    else if (text != null)
      setText(text);
  }

  public void setPromptText(final String promptText) {
    // TODO(9/14/2021): can try setting the "placeholder" attribute instead of this ad-hoc implementation (see https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input#attr-placeholder)
    if (StringUtils.isBlank(promptText)) {
      // setting an empty prompt is equivalent to having no prompt
      this.promptText = null;
      this.usePrompt = false;
      removeStyleDependentName(UNFOCUSED_STYLE_DEPENDENT_NAME);
      return;
    }
    this.promptText = promptText;
    this.usePrompt = true;
    addStyleDependentName(UNFOCUSED_STYLE_DEPENDENT_NAME);
    setText(promptText);

    if (!focusAndBlurHandlersAdded) {
      addFocusHandler(this);
      addBlurHandler(this);
      focusAndBlurHandlersAdded = true;
    }    
  }

  public boolean hasPrompt() {
    return usePrompt;
  }

  public String getPromptText() {
    return promptText;
  }

  public void onFocus(FocusEvent event) {
    if (promptText != null && promptText.equals(getTextUnfiltered())) {
      removeStyleDependentName(UNFOCUSED_STYLE_DEPENDENT_NAME);
      setText("");
    }
  }

  public void onBlur(BlurEvent event) {
    if (StringUtils.isBlank(getTextUnfiltered())) {
      addStyleDependentName(UNFOCUSED_STYLE_DEPENDENT_NAME);
      setText(promptText);
    }
  }


  /**
   * @return the entered input text or an empty string if the current value is equal to the original prompt.
   * @see #getTextUnfiltered()
   */
  public String getText() {
    String actualText = getTextUnfiltered();
    if (!usePrompt || !actualText.equals(promptText))
      return actualText;
    else
      return "";
  }

  protected String getTextUnfiltered() {
    return super.getText();
  }
}
