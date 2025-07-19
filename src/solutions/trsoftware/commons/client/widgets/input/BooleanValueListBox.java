package solutions.trsoftware.commons.client.widgets.input;

import com.google.gwt.user.client.ui.ValueListBox;

import java.util.Arrays;

/**
 * @author Alex
 * @since 4/10/2025
 */
public class BooleanValueListBox extends ValueListBox<Boolean> {

  public BooleanValueListBox() {
    setAcceptableValues(Arrays.asList(true, false));
  }
}
