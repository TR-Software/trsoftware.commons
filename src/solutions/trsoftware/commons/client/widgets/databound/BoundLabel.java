package solutions.trsoftware.commons.client.widgets.databound;

import com.google.gwt.user.client.ui.Label;
import solutions.trsoftware.commons.shared.util.TakesValue;

/**
 * A mix-in HasValue version of Label.
 *
 * @author Alex
 */
public class BoundLabel<V> extends Label implements TakesValue<V> {

  private V value;

  public final V getValue() {
    return value;
  }

  public final void setValue(V value) {
    this.value = value;
    if (!customRender(value)) {
      setText(String.valueOf(value));
    }
  }

  /**
   * Can be overridden by subclasses to provide rendering that differs
   * from setText(value.toString).
   * @param value
   * @return Whether this method was overridden (custom rendering was used
   * and setText() should not be called for the value).
   */
  protected boolean customRender(V value) {
    return false;
  }
}
