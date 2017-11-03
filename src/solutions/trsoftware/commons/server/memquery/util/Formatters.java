package solutions.trsoftware.commons.server.memquery.util;

import solutions.trsoftware.commons.server.memquery.Formatter;
import solutions.trsoftware.commons.server.memquery.aggregations.Aggregation;
import solutions.trsoftware.commons.server.memquery.schema.SprintfColFormatter;
import solutions.trsoftware.commons.server.memquery.schema.SprintfFloatFormatter;
import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
* @author Alex, 1/9/14
*/
public abstract class Formatters {

  /** Simply delegates to the each value object's own toString method */
  public static final Formatter DEFAULT = new Formatter() {
    @Override
    public String format(Object value) {
      return value.toString();    }
  };

  /**
   * Registry of formatters for each of the basic printable types (CharSequences and primitives);
   * created on-demand.
   */
  private static Map<Class, Formatter> defaults;

  /**
   * Registry of formatters for Aggregation types. Created on-demand.
   */
  private static Map<Class, Formatter> aggregationDefaults;

  private static Class normalizeType(Class type) {
    if (ReflectionUtils.isPrimitiveWrapper(type))
      return ReflectionUtils.primitiveTypeFor(type);
    return type;
  }

  private static Formatter getOrCreateSprintfDefault(Class cls, String formatSpec) {
    return getOrCreateSprintfDefault(cls, new SprintfColFormatter(formatSpec));
  }

  private static Formatter getOrCreateSprintfDefault(Class cls, SprintfColFormatter formatter) {
    if (defaults == null)
      defaults = new HashMap<Class, Formatter>();
    Formatter ret = defaults.get(cls);
    if (ret == null) {
      ret = formatter;
      defaults.put(cls, ret);
    }
    return ret;
  }

  private static class AggregationFormatter implements Formatter {
    private Formatter delegate;

    private AggregationFormatter(Formatter delegate) {
      this.delegate = delegate;
    }

    @Override
    public String format(Object value) {
      return delegate.format(((Aggregation)value).get());
    }
  }

  private static Formatter getOrCreateAggregationDefault(Class valueType) {
    if (aggregationDefaults == null)
      aggregationDefaults = new HashMap<Class, Formatter>();
    Formatter ret = aggregationDefaults.get(valueType);
    if (ret == null) {
      ret = new AggregationFormatter(getFor(valueType));
      aggregationDefaults.put(valueType, ret);
    }
    return ret;
  }


  public static Formatter getFor(Class type) {
    if (Aggregation.class.isAssignableFrom(type)) {
      // use the aggregation value type
      return getOrCreateAggregationDefault(AggregationUtils.getAggregationValueType(type));
    }
    type = normalizeType(type); // to reduce the number of choices, we first transform wrappers to primitives
    if (type == char.class || CharSequence.class.isAssignableFrom(type))
      return getOrCreateSprintfDefault(type, "%s");
    else if (type == int.class || type == long.class || type == short.class || type == byte.class)
      return getOrCreateSprintfDefault(type, "%,d");
    else if (type == float.class || type == double.class)
      return getOrCreateSprintfDefault(type, new SprintfFloatFormatter(true, null, 2, true)); // TODO: have the # of fractional digits as configurable setting
    else if (type == boolean.class)
      return getOrCreateSprintfDefault(type, "%b");
    // default to formatting the value using its toString method
    return DEFAULT;
  }

}
