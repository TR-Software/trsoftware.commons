package solutions.trsoftware.commons.client.widgets;

import com.google.common.base.Strings;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.ValueListBox;
import solutions.trsoftware.commons.shared.util.HasValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static solutions.trsoftware.commons.client.widgets.Widgets.flowPanel;

/**
 * A composite input widget that displays a dropdown list of preset options with an option to enter a custom value,
 * which will display an input field next to the list when selected.
 * The {@link #getValue()} method of this widget returns either the value of the selected preset option or the custom value,
 * depending on the user's selection.
 * <p>
 * The constructor takes an external instance of {@link ValueBoxBase} for the custom value input and creates an internal
 * instance of {@link ValueListBox} for the presets.  Whenever the list box value is changed, the custom value input box
 * is updated with the new value, thus the caller can attach a {@link ValueChangeHandler} to that widget in order to
 * implement any required validation logic or interactions with other form elements.
 * Note: any value entered into the custom value input box is not retained if the user subsequently chooses a different
 * item in the list.
 *
 * @param <P> the preset value type for the list box
 * @param <V> the output value type for the custom value input box (can be the same as {@link P}), which is also
 *     the type returned by this widget's {@link #getValue()} method.
 * @author Alex
 * @since 3/15/2024
 */
public class CustomValueListBox<P, V> extends Composite implements HasValue<V>, HasFocusTarget {

  private final ValueBoxBase<V> customValueInput;
  private final ValueListBox<P> presetListBox;
  private final Function<P, V> valueFromPreset;

  /**
   * See the {@linkplain CustomValueListBox class description} for details.
   *
   * @param name will be used to describe the custom option in the list of presets
   * @param customValueInput input element for entering a custom value, which is displayed when the custom option is selected in the list
   * @param presetValues the options to display in the {@link #presetListBox}
   * @param defaultPreset the default selection in  {@link #presetListBox} (must be one of the elements in the values list)
   * @param presetRenderer returns a string representation of the preset values
   * @param valueFromPreset converts a preset value to the input value type
   */
  public CustomValueListBox(String name, ValueBoxBase<V> customValueInput,
                            List<P> presetValues, P defaultPreset,
                            Function<P, String> presetRenderer, Function<P, V> valueFromPreset) {
    this.customValueInput = customValueInput;
    this.valueFromPreset = valueFromPreset;
    customValueInput.setVisible(false);  // start hidden until the "Customize" option is selected in the presets list
    this.presetListBox = new ValueListBox<>(new AbstractRenderer<P>() {
      @Override
      public String render(P value) {
        if (value == null)
          // the null option triggers the custom input element to be displayed
          return Strings.lenientFormat("-- Custom %s --", name);
        return presetRenderer.apply(value);
      }
    });
    initPresetValues(presetValues, defaultPreset);
    initWidget(flowPanel(
        presetListBox,
        customValueInput
    ));
  }

  private void initPresetValues(List<P> presetValues, P defaultValue) {
    if (!presetValues.contains(null)) {
      // add a null option, for which we'll display the custom value input field
      presetValues = new ArrayList<>(presetValues);  // defensive copy before mutation
      presetValues.add(null);
    }
    presetListBox.setAcceptableValues(presetValues);
    presetListBox.setValue(defaultValue, true);
    presetListBox.addValueChangeHandler(event -> {
      P selectedOption = event.getValue();
      if (selectedOption == null) {
        // selected the option to enter a custom value
        customValueInput.setValue(null);  // clear the input
        customValueInput.setVisible(true);
        customValueInput.setFocus(true);
      }
      else {
        // selected a valid preset option
        customValueInput.setVisible(false);
        // update the custom size text to keep in sync with the select box, since the submitted value will be read from the customSizeInput, not the list box
        customValueInput.setValue(valueFromPreset.apply(selectedOption), true);
        /* Note: passing fireEvents=true above to trigger any custom logic implemented by the ValueChangeHandler
           of the customValueInput, which may include validation and/or interaction with other form widgets that we don't know about here
         */
      }
    });
  }

  public ValueListBox<P> getPresetListBox() {
    return presetListBox;
  }

  public ValueBoxBase<V> getCustomValueInput() {
    return customValueInput;
  }

  @Override
  public V getValue() {
    return customValueInput.isVisible() ? customValueInput.getValue() : valueFromPreset.apply(presetListBox.getValue());
  }

  @Override
  public Focusable getFocusTarget() {
    return customValueInput.isVisible() ? customValueInput : presetListBox;
  }

}
