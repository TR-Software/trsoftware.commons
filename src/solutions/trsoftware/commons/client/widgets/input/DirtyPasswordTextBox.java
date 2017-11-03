package solutions.trsoftware.commons.client.widgets.input;

import com.google.gwt.user.client.ui.PasswordTextBox;
import solutions.trsoftware.commons.client.util.StringUtils;

/**
 * Jan 18, 2010
 *
 * @author Alex
 */
public class DirtyPasswordTextBox extends PasswordTextBox implements DirtyInput {

  private final String initialValue;

  public DirtyPasswordTextBox(Integer visibleChars, Integer maxChars, String initialValue) {
    this(initialValue);
    if (visibleChars != null)
      setVisibleLength(visibleChars);
    if (maxChars != null)
      setMaxLength(maxChars);
  }

  /** Creates an empty password text box. */
  public DirtyPasswordTextBox(String initialValue) {
    if (!StringUtils.isBlank(initialValue)) {
      this.initialValue = initialValue;
      setText(initialValue);
    }
    else {
      this.initialValue = getText();
    }
  }

  public boolean isDirty() {
    return !getText().equals(initialValue);
  }

}
