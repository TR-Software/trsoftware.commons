/*
 * Copyright 2021 TR Software Inc.
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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.bundle.CommonsClientBundleFactory;
import solutions.trsoftware.commons.client.bundle.CommonsCss;
import solutions.trsoftware.commons.client.event.CapsLockDetector;
import solutions.trsoftware.commons.client.event.SpecificKeyDownHandler;
import solutions.trsoftware.commons.client.widgets.popups.ModalDialog;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.validation.ValidationResult;
import solutions.trsoftware.commons.shared.validation.ValidationRule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.lenientFormat;
import static java.util.Objects.requireNonNull;
import static solutions.trsoftware.commons.client.widgets.Widgets.flowPanel;
import static solutions.trsoftware.commons.client.widgets.Widgets.html;
import static solutions.trsoftware.commons.shared.util.ListUtils.isEmpty;
import static solutions.trsoftware.commons.shared.util.StringUtils.joinEnumerated;

/**
 * A convenience class for building user input forms with validation.
 * <p>
 * Input widgets can be added by calling either
 * <ul>
 * <li>
 *   {@link #addTextField(Label, TextBox)}, {@link #addTextField(String, TextBox)},
 *   {@link #addTextField(Label, TextBox, ValidationRule)}, or {@link #addTextField(String, TextBox, ValidationRule)}
 *   for {@link TextBox} and {@link PasswordTextBox} fields.
 * </li>
 * <li>
 *   {@link #addInputWidget(Widget)} for all other fields.
 * </li>
 * </ul>
 * <p>
 * A submit button added by {@link #addSubmitButton(Button)}, will have a {@link ClickHandler}
 * that validates all inputs and invokes {@link #onValidatedSubmit()} if all inputs are valid.
 * <p>
 * Subclasses should implement {@link #onValidatedSubmit()}, which will be invoked when the submit button is clicked
 * or the {@code Enter} key is pressed on one of the input fields.
 *
 * If any {@link ValidationRule}s were added with {@link #addTextField(Label, TextBox, ValidationRule)}
 * or {@link #addTextField(String, TextBox, ValidationRule)}, then those will be invoked prior to calling {@link #onValidatedSubmit()}.
 *
 * Subclasses may also override {@link #validate(boolean)} to provide additional validation logic not covered by the added
 * {@link ValidationRule}s (just don't forget to call <code>super.{@link #validate(boolean)}</code>)
 * <p>
 * Uses a {@link CapsLockDetector} to show a warning whenever a contained {@link PasswordTextBox}
 * receives a keystroke while the {@code Caps Lock} key is on.
 * </p>
 * @author Alex
 * @since 11/15/2017
 */
public abstract class BasicInputForm extends FlowPanel {
  private static final CommonsCss CSS = CommonsClientBundleFactory.INSTANCE.getCss();

  public static final String FIELD_ERROR_STYLE = CSS.fieldErrorMsg();
  private final Layout layout;

  private final FlexTable tblForm = new FlexTable();
  private int nextRow;

  private final SpecificKeyDownHandler enterKeyHandler;
  private final List<TextInput> inputFields = new ArrayList<>();

  public enum Layout {
    /**
     * Input field will be displayed next to the label in the same row
     */
    HORIZONTAL,
    /**
     * Input field will be displayed below the label in a separate row
     */
    VERTICAL
  }

  public BasicInputForm() {
    this(Layout.HORIZONTAL);
  }

  /**
   * @param layout orientation of the input fields against their labels
   * @see Layout#HORIZONTAL
   * @see Layout#VERTICAL
   */
  public BasicInputForm(Layout layout) {
    this.layout = layout;
    enterKeyHandler = new SpecificKeyDownHandler(KeyCodes.KEY_ENTER, this::submit);
    add(tblForm);
    setStyleName(CSS.BasicInputForm());
  }

  protected void submit() {
    List<String> failedFieldNames = validate(true);
    if (isEmpty(failedFieldNames))
      onValidatedSubmit();
    else
      onInvalidSubmit(failedFieldNames);
  }

  /**
   * Invoked when the submit button is clicked or the {@code Enter} key is pressed on one of the input fields.
   */
  protected abstract void onValidatedSubmit();

  /**
   * Invoked when the submit button is clicked but one or more fields fails validation.
   *
   * @param failedFieldNames names of the fields that failed to validate
   */
  protected void onInvalidSubmit(List<String> failedFieldNames) {
    ModalDialog.softAlert(lenientFormat("Please fix your input%s for %s",
        failedFieldNames.size() > 1 ? "s" : "",
        joinEnumerated(",", "and", failedFieldNames)));

  }

  /**
   * Validates all the form inputs and returns a list of field names that failed validation,
   * or empty list if all fields are valid.
   *
   * @param onSubmit {@code true} if invoked from {@link #submit()}
   */
  protected List<String> validate(boolean onSubmit) {
    return inputFields.stream().filter(input -> !input.validate(onSubmit))
        .map(TextInput::getFieldName).collect(Collectors.toList());
  }

  /**
   * @param label a {@link String} or {@link Widget} to display before the input widget
   * @return
   */
  protected TextInput addTextInput(Object label, TextInput inputWidget) {
    inputFields.add(inputWidget);
    inputWidget.addKeyDownHandler(enterKeyHandler);
    if (label instanceof String)
      tblForm.setText(nextRow, 0, (String)label);
    else if (label instanceof Widget)
      tblForm.setWidget(nextRow, 0, (Widget)label);
    else
      throw new IllegalArgumentException("label");
    if (layout == Layout.VERTICAL)
      tblForm.setWidget(++nextRow, 0, inputWidget);
    else
      tblForm.setWidget(nextRow, 1, inputWidget);
    nextRow++;
    return inputWidget;
  }

  public BasicInputForm addInputWidget(Widget inputWidget) {
    if (layout == Layout.VERTICAL)
      tblForm.setWidget(nextRow++, 0, inputWidget);
    else
      tblForm.setWidget(nextRow++, 1, inputWidget);
    return this;
  }

  public BasicInputForm addInputWidget(Widget inputWidget, int colSpan) {
    // TODO(6/19/2026): experimental colSpan
    int row = nextRow++;
    if (layout == Layout.VERTICAL)
      tblForm.setWidget(row, 0, inputWidget);
    else {
//      Preconditions.checkArgument(NumberRange.inRange(0, 2, colSpan));
      int column = 1;
      if (colSpan > 1) {
        column = 0;
        tblForm.getFlexCellFormatter().setColSpan(row, column, 2);
      }
      tblForm.setWidget(row, column, inputWidget);
    }
    return this;
  }

  public BasicInputForm addSubmitButton(Button submitButton) {
    submitButton.addClickHandler(click -> submit());
    addInputWidget(submitButton);
    return this;
  }

  public TextInput addTextField(Label label, TextBox textBox) {
    return addTextField(label, textBox, null);
  }

  public TextInput addTextField(String label, TextBox textBox) {
    return addTextField(label, textBox, null);
  }

  public TextInput addTextField(Label label, TextBox textBox, ValidationRule<String> validator) {
    return addTextInput(label, new TextInput(textBox, validator));
  }

  public TextInput addTextField(String label, TextBox textBox, ValidationRule<String> validator) {
    return addTextInput(label, new TextInput(textBox, validator));
  }

  /**
   * A widget wrapping a {@link TextBox} to perform validation and display validation error messages
   */
  protected static class TextInput extends Composite implements HasKeyDownHandlers {
    private final TextBox textBox;
    @Nullable
    private final ValidationRule<String> validator;
    private HTML lblError;
    protected final FlowPanel container;

    protected TextInput(@Nonnull TextBox textBox, @Nullable ValidationRule<String> validator) {
      this.textBox = requireNonNull(textBox, "textBox");
      this.validator = validator;
      container = flowPanel(textBox);
      if (validator != null) {
        lblError = html("", FIELD_ERROR_STYLE);
        lblError.setVisible(false);
        container.add(lblError);
      }
      if (textBox instanceof PasswordTextBox) {
        final Label lblCapsLockWarning = html("Your <em>Caps Lock</em> key is on", FIELD_ERROR_STYLE);
        // show a "Caps Lock" warning when entering password
        textBox.addKeyPressHandler(new CapsLockDetector() {
          @Override
          protected void onCapsLockStatus(boolean on) {
            lblCapsLockWarning.setVisible(on);
          }
        });
        lblCapsLockWarning.setVisible(false);
        container.add(lblCapsLockWarning);
      }
      initWidget(container);
      // validate input automatically, to hide the error when a valid input is entered
      textBox.addChangeHandler(event -> validate(false));
    }

    /**
     * Invokes this field's {@link ValidationRule} and shows or hides {@link #lblError} based on whether the input is valid.
     *
     * @param onSubmit {@code true} if invoked from {@link #submit()}, or {@code false} if invoked for value change
     * @return {@code true} iff the input is valid
     */
    public boolean validate(boolean onSubmit) {
      if (validator == null)
        return true;
      ValidationResult result = validator.validate(textBox.getText());
      boolean valid = result.isValid();
      setErrorMessage(valid ? null : "&uarr; " + result.getErrorMessage());
      return valid;
    }

    protected void setErrorMessage(@Nullable String errorMsg) {
      if (StringUtils.notBlank(errorMsg)) {
        lblError.setHTML(errorMsg);
        lblError.setVisible(true);
        textBox.addStyleName(CSS.fieldErrorHighlight());
      } else {
        lblError.setHTML("");
        lblError.setVisible(false);
        textBox.removeStyleName(CSS.fieldErrorHighlight());
      }
    }

    @Override
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
      return textBox.addKeyDownHandler(handler);
    }

    public TextBox getTextBox() {
      return textBox;
    }

    public String getText() {
      return textBox.getText();
    }

    public void setText(String text) {
      textBox.setText(text);
    }

    public String getFieldName() {
      // TODO(6/27/2026): maybe require non-null validator?  currently no usages where it's actually null
      return validator != null ? validator.getFieldName() : null;
    }
  }
}
