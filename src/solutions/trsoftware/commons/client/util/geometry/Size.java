package solutions.trsoftware.commons.client.util.geometry;

import com.google.gwt.dom.client.Style;

/**
 * Immutable class encapsulating a dimension expressed in CSS units.
 *
 * @author Alex, 6/21/2016
 */
public class Size {

  private final double value;
  private final Style.Unit unit;

  public Size(double value, Style.Unit unit) {
    this.value = value;
    this.unit = unit;
  }

  public double getValue() {
    return value;
  }

  public Style.Unit getUnit() {
    return unit;
  }

  public Size scale(double factor) {
    return new Size(value * factor, unit);
  }

  @Override
  public String toString() {
    return String.valueOf(value) + unit.getType();
  }

  // factory methods:
  public static Size pct(double value) {
    return new Size(value, Style.Unit.PCT);
  }

  public static Size px(double value) {
    return new Size(value, Style.Unit.PX);
  }

}
