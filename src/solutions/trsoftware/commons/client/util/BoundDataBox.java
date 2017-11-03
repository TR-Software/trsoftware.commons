package solutions.trsoftware.commons.client.util;

import solutions.trsoftware.commons.shared.util.TakesValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: Nov 14, 2008 Time: 2:33:34 PM
 *
 * @author Alex
 */
public class BoundDataBox<V> extends Box<V> {

  /** These display widgets will be updated whenever the boxed value changes */
  private List<TakesValue<V>> boundWidgets = new ArrayList<TakesValue<V>>();

  public BoundDataBox() {
  }

  public BoundDataBox(V value) {
    super(value);
  }

  @Override
  public void setValue(V value) {
    super.setValue(value);
    for (TakesValue<V> boundWidget : boundWidgets) {
      boundWidget.setValue(value);
    }
  }

  public void addBoundDisplayWidget(TakesValue<V> displayWidget) {
    boundWidgets.add(displayWidget);
    displayWidget.setValue(getValue());
  }

}
