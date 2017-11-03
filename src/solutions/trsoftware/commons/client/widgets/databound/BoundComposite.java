package solutions.trsoftware.commons.client.widgets.databound;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.shared.util.TakesValue;

/**
 * A mix-in HasValue version of Composite.
 *
 * @author Alex
 */
public abstract class BoundComposite<W extends Widget, V> extends Composite implements TakesValue<V> {

  protected W widget;
  private V value;

  public BoundComposite(W widget) {
    this.widget = widget;
    initWidget(widget);
  }

  public final V getValue() {
    return value;
  }

  public final void setValue(V value) {
    this.value = value;
    customRender(value);
  }

  /**
   * Must be overridden by subclasses to provide rendering of the value within
   * the widget.
   */
  protected abstract void customRender(V value);
}