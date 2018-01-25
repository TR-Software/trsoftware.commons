package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.bundle.CommonsClientBundleFactory;
import solutions.trsoftware.commons.client.event.CapsLockDetector;
import solutions.trsoftware.commons.client.event.EventHandlers;
import solutions.trsoftware.commons.client.event.SpecificKeyDownHandler;
import solutions.trsoftware.commons.shared.validation.ValidationResult;
import solutions.trsoftware.commons.shared.validation.ValidationRule;

import java.util.ArrayList;
import java.util.List;

import static solutions.trsoftware.commons.client.widgets.Widgets.flowPanel;
import static solutions.trsoftware.commons.client.widgets.Widgets.html;

/**
 * A convenience class for building user input forms.
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
 * </p>
 * <p>
 * The submit button should be added by calling {@link #addSubmitButton(Button)}, and it will receive a {@link ClickHandler}
 * that invokes {@link #doValidatedSubmit()} when clicked.
 * </p>
 *
 * <p>
 * Subclasses should implement {@link #doValidatedSubmit()}, which will be invoked when the submit button is clicked
 * or the {@code Enter} key is pressed on one of the input fields.
 *
 * If any {@link ValidationRule}s were added with {@link #addTextField(Label, TextBox, ValidationRule)}
 * or {@link #addTextField(String, TextBox, ValidationRule)}, then those will be invoked prior to calling {@link #doValidatedSubmit()}.
 *
 * Subclasses may also override {@link #validate()} to provide additional validation logic not covered by the added
 * {@link ValidationRule}s (just don't forget to call <code>super.{@link #validate()}</code>)
 * </p>
 *
 * <p>
 * Uses a {@link CapsLockDetector} to show a warning whenever a contained {@link PasswordTextBox}
 * receives a keystroke while the {@code Caps Lock} key is on.
 * </p>
 * @author Alex
 * @since 11/15/2017
 */
public abstract class BasicInputForm extends FlowPanel {

  public static final String FIELD_ERROR_STYLE = CommonsClientBundleFactory.INSTANCE.getCommonsCss().fieldErrorMsg();
  private Layout layout;

  private FlexTable tblForm = new FlexTable();
  private int nextRow;

  private Command submitCommand;
  private SpecificKeyDownHandler enterKeyHandler;
  private List<TextInput> textInputs = new ArrayList<TextInput>();

  public enum Layout { HORIZONTAL, VERTICAL;}

  public BasicInputForm() {
    this(Layout.HORIZONTAL);
  }

  public BasicInputForm(Layout layout) {
    this.layout = layout;
    submitCommand = new Command() {
      @Override
      public void execute() {
        if (validate())
          doValidatedSubmit();
      }
    };
    enterKeyHandler = new SpecificKeyDownHandler(KeyCodes.KEY_ENTER, submitCommand);
    add(tblForm);
    setStyleName(CommonsClientBundleFactory.INSTANCE.getCommonsCss().BasicInputForm());
  }

  /**
   * Invoked when the submit button is clicked or the {@code Enter} key is pressed on one of the input fields.
   */
  protected abstract void doValidatedSubmit();

  protected boolean validate() {
    boolean allValid = true;
    for (TextInput input : textInputs) {
      allValid &= input.validate();
    }
    return allValid;
  }

  private BasicInputForm addTextInput(Object label, TextInput inputWidget) {
    textInputs.add(inputWidget);
    inputWidget.addKeyDownHandler(enterKeyHandler);
    if (label instanceof String)
      tblForm.setText(nextRow, 0, (String)label);
    else if (label instanceof Widget)
      tblForm.setWidget(nextRow, 0, (Widget)label);
    else
      throw new IllegalArgumentException();
    if (layout == Layout.VERTICAL)
      tblForm.setWidget(++nextRow, 0, inputWidget);
    else
      tblForm.setWidget(nextRow, 1, inputWidget);
    nextRow++;
    return this;
  }

  public BasicInputForm addInputWidget(Widget inputWidget) {
    if (layout == Layout.VERTICAL)
      tblForm.setWidget(nextRow++, 0, inputWidget);
    else
      tblForm.setWidget(nextRow++, 1, inputWidget);
    return this;
  }

  public BasicInputForm addSubmitButton(Button submitButton) {
    submitButton.addClickHandler(EventHandlers.clickHandler(submitCommand));
    addInputWidget(submitButton);
    return this;
  }

  public BasicInputForm addTextField(Label label, TextBox textBox) {
    return addTextField(label, textBox, null);
  }

  public BasicInputForm addTextField(String label, TextBox textBox) {
    return addTextField(label, textBox, null);
  }

  public BasicInputForm addTextField(Label label, TextBox textBox, ValidationRule validator) {
    return addTextInput(label, new TextInput(textBox, validator));
  }

  public BasicInputForm addTextField(String label, TextBox textBox, ValidationRule validator) {
    return addTextInput(label, new TextInput(textBox, validator));
  }


  private class TextInput extends Composite implements HasKeyDownHandlers {
    private TextBox textBox;
    private ValidationRule validator;
    private HTML lblError;

    TextInput(final TextBox textBox, ValidationRule validator) {
      this.textBox = textBox;
      this.validator = validator;
      FlowPanel container = flowPanel(textBox);
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
    }

    boolean validate() {
      if (validator == null)
        return true;
      ValidationResult result = validator.validate(textBox.getText());
      boolean valid = result.isValid();
      if (!valid)
        lblError.setHTML("&uarr; " + result.getErrorMessage());
      lblError.setVisible(!valid);
      return valid;
    }

    @Override
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
      return textBox.addKeyDownHandler(handler);
    }
  }
}
