package solutions.trsoftware.commons.client.util.geometry;

import com.google.common.base.Preconditions;
import com.google.gwt.dom.client.Element;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nullable;

/**
 * Represents an absolute quantity in pixels or a relative offset based on the dimensions of an element.
 *
 * @author Alex
 * @since 12/25/2024
 */
public class Offset {
  /**
   * 0 is a special case that doesn't require a CSS unit.
   */
  public static final Offset ZERO = new Offset(0, null);

  private final double value;
  @Nullable
  private final Unit unit;

  /**
   * Note: prefer using the factory methods, {@link #px(double)} or {@link #pct(double)}, instead of constructor.
   *
   * @param value the value
   * @param unit required if {@code value} is not {@code 0}
   * @throws IllegalArgumentException if {@code unit} is {@code null} when {@code value} is not {@code 0}
   */
  Offset(double value, @Nullable Unit unit) {
    this.value = value;
    Preconditions.checkArgument(unit != null || value == 0);
    this.unit = unit;
  }

  /**
   * Computes the offset, in pixels, relative to the given element.
   *
   * @return {@link #value} if {@link #unit} is {@link Unit#PX PX},
   *   or {@code (value/100)*element.offsetWidth} if {@link #unit} is {@link Unit#PCT PCT}
   */
  public double getX(Element element) {
    if (unit == Unit.PCT)
      return (value / 100) * element.getOffsetWidth();
    return value;
  }

  /**
   * Computes the offset, in pixels, relative to the given element.
   *
   * @return {@link #value} if {@link #unit} is {@link Unit#PX PX},
   *   or {@code (value/100)*element.offsetHeight} if {@link #unit} is {@link Unit#PCT PCT}
   */
  public double getY(Element element) {
    if (unit == Unit.PCT)
      return (value / 100) * element.getOffsetHeight();
    return value;
  }

  public double getValue() {
    return value;
  }

  @Nullable
  public Unit getUnit() {
    return unit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Offset offset = (Offset)o;

    if (Double.compare(offset.value, value) != 0)
      return false;
    return unit == offset.unit;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(value);
    result = (int)(temp ^ (temp >>> 32));
    result = 31 * result + (unit != null ? unit.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return unit != null ? unit.toString(value) : "0";
  }

  // Factory methods:

  public static Offset px(double value) {
    return value != 0 ? new Offset(value, Unit.PX) : ZERO;
  }

  public static Offset pct(double value) {
    return value != 0 ? new Offset(value, Unit.PCT) : ZERO;
  }

  public static Offset zero() {
    return ZERO;
  }

  /**
   * @param input a numeric string ending with either "px" or "%"
   * @throws IllegalArgumentException if the string doesn't match the expected format
   */
  public static Offset parse(String input) {
    RegExp regExp = RegExp.compile("([-0-9.]+)(px|%)?", "i");
    MatchResult matchResult = regExp.exec(input);
    if (matchResult != null) {
      String valStr = matchResult.getGroup(1);
      String unitStr = matchResult.getGroup(2);
      Unit unit = Unit.parse(unitStr);
      double value = Double.parseDouble(valStr);
      return new Offset(value, unit);
    }
    throw new IllegalArgumentException("Invalid Offset: " + input);
  }

  public enum Unit {
    /**
     * An absolute offset, in pixels
     */
    PX("px"),
    /**
     * A relative offset, based on a percentage of an element's width or height
     */
    PCT("%");

    private final String abbrev;

    Unit(String abbrev) {
      this.abbrev = abbrev;
    }

    /**
     * @param unitString {@code "px"} or {@code "%"}
     * @return the value corresponding to the given unit abbreviation
     * @throws IllegalArgumentException if the string is neither {@code "px"} nor {@code "%"}
     */
    @Nullable
    public static Unit parse(String unitString) {
      if (StringUtils.isBlank(unitString))
        return null;  // unit can be null when value is 0
      for (Unit unit : values()) {
        if (unit.abbrev.equalsIgnoreCase(unitString))
          return unit;
      }
      throw new IllegalArgumentException("Unrecognized unit: " + unitString);
    }

    public String toString(double value) {
      return value + abbrev;
    }
  }
}
