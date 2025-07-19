package solutions.trsoftware.commons.client.jso;

import com.google.common.base.Preconditions;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import solutions.trsoftware.commons.shared.util.NumberRange;
import solutions.trsoftware.commons.shared.util.TimeUnit;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.EnumSet;

import static solutions.trsoftware.commons.client.jso.JsDateFormat.Component.*;
import static solutions.trsoftware.commons.client.jso.JsDateFormat.Value.*;

/**
 * @author Alex
 * @since 6/8/2025
 */
public class JsDateFormat {

  public static String smartFormat(JsDate date) {
    JsDate now = JsDate.create();
    Options timeOptions = Options.create()
        .hour(NUMERIC).minute(TWO_DIGIT);
    if (date.isToday()) {
      return "Today " + date.toLocaleString(timeOptions);
    }
    if (date.isYesterday()) {
      return "Yesterday " + date.toLocaleString(timeOptions);
    }
    if (date.compareTo(now.add(TimeUnit.DAYS, -6).truncateTime()) > 0) {
      // if less than 7 days ago, prefix the time with just the name of weekday instead of full date
      return date.toLocaleString(timeOptions.copy().weekday(LONG));
    }
    // if more than a week ago, will omit the time component
    // TODO: maybe write special case to include time when multiple rows in data table have same date
    if (date.getFullYear() == now.getFullYear()) {
      // if this year, omit year and use verbose month name (e.g. January)
      return date.toLocaleString(Options.create().month(LONG).day(NUMERIC));
    }
    // otherwise, if older than current year, show the full date in a compact numeric format
    return date.toLocaleString(Options.create().year(NUMERIC).month(NUMERIC).day(NUMERIC));
  }

  interface Option {}

  /**
   * Date-time component options
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Intl/DateTimeFormat/DateTimeFormat#date-time_component_options">
   *   MDN</a>
   */
  public enum Component {
    // TODO: document each constant
    weekday(LONG, SHORT, NARROW),
    era(LONG, SHORT, NARROW),
    year(NUMERIC, TWO_DIGIT),
    month(LONG, SHORT, NARROW, NUMERIC, TWO_DIGIT),
    day(NUMERIC, TWO_DIGIT),
    dayPeriod(LONG, SHORT, NARROW),
    hour(NUMERIC, TWO_DIGIT),
    minute(NUMERIC, TWO_DIGIT),
    second(NUMERIC, TWO_DIGIT),
    fractionalSecondDigits,  // TODO: int 1-3
    timeZoneName(LONG, SHORT, SHORT_OFFSET, LONG_OFFSET, SHORT_GENERIC, LONG_GENERIC);  // TODO: has additional values not currently present in Value enum

    private EnumSet<Value> possibleValues;

    Component() {
    }

    Component(Value... possibleValues) {
      this.possibleValues = EnumSet.noneOf(Value.class);
      this.possibleValues.addAll(Arrays.asList(possibleValues));
    }

    public boolean allowsValue(@Nonnull Object value) {
      //noinspection ConstantConditions
      if (value == null)
        return false;  // null not allowed for any component (will throw "RangeError: Value null out of range...")
      else if (value instanceof Value)
        return possibleValues != null && possibleValues.contains(value);
      else if (this == fractionalSecondDigits)
        return value instanceof Number && NumberRange.inRange(1d, 3d, ((Number)value).doubleValue());
      return false;
    }

  }

  public enum Value {
    LONG("long"), SHORT("short"), NARROW("narrow"),
    NUMERIC("numeric"), TWO_DIGIT("2-digit"),
    // special options for timeZoneName:
    SHORT_OFFSET("shortOffset"), LONG_OFFSET("longOffset"), SHORT_GENERIC("shortGeneric"), LONG_GENERIC("longGeneric"),
    ;

    /* TODO: for fractionalSecondDigits the value is an int 1-3, and timeZoneName has additional values (e.g. "shortOffset") */
    
    Value(String value) {
      this.value = value;
    }

    private final String value;

    @Override
    public String toString() {
      return value;
    }
  }

  public static class Options extends JavaScriptObject {
    
    protected Options() {
    }

    /**
     * Factory method.
     * @return a new (empty) options object
     */
    public static Options create() {
      return JavaScriptObject.createObject().cast();
    }

    public final Options weekday(Value value) {
      return set(weekday, value);
    }

    public final Options era(Value value) {
      return set(era, value);
    }

    public final Options year(Value value) {
      return set(year, value);
    }

    public final Options month(Value value) {
      return set(month, value);
    }

    public final Options day(Value value) {
      return set(day, value);
    }

    public final Options dayPeriod(Value value) {
      return set(dayPeriod, value);
    }

    public final Options hour(Value value) {
      return set(hour, value);
    }

    public final Options minute(Value value) {
      return set(minute, value);
    }

    public final Options second(Value value) {
      return set(second, value);
    }

    public final Options fractionalSecondDigits(Number digits) {
      return set(fractionalSecondDigits, digits);
    }

    public final Options timeZoneName(Value value) {
      return set(timeZoneName, value);
    }

    private Options set(Component component, Object value) {
      assert component != null;
      String name = component.toString();
      if (value == null)
        JsObject.as(this).delete(name);
      else {
        Preconditions.checkArgument(component.allowsValue(value), "%s is not allowed for %s", value, component);
        JsObject.as(this).set(name, value.toString());
      }
      return this;
    }

    public final Options copy() {
      return JsonUtils.safeEval(toJson()).cast();
    }

    public final String toJson() {
      return JsonUtils.stringify(this);
    }
  }

}
