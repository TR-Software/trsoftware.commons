package solutions.trsoftware.commons.server.servlet.filters.config;

import com.google.common.primitives.Primitives;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static solutions.trsoftware.commons.server.servlet.filters.config.FilterParameters.Param;
import static solutions.trsoftware.commons.server.servlet.filters.config.FilterParameters.ParameterParser;

/**
 * Populates the fields of a {@link FilterParameters} instance from the {@code init-param} values in a {@link FilterConfig}.
 *
 * <p>
 * Each field of the {@link FilterParameters} instance will be assigned the value of the {@code init-param} having
 * the same name as the field, parsed using the field type's {@code static valueOf(String)} method.
 * </p>
 * <p>
 * The {@link Param @Param} annotation can be used to override the default parsing behavior for any field of the
 * {@link FilterParameters} instance.
 *
 * <em>This annotation is mandatory (required to specify {@link Param#parser() parser}) iff the field's type is neither
 * {@link String} nor primitive, and does not have a {@code static valueOf(String)} method.</em>
 * </p>
 *
 * <p style="color: green; font-style: italic;">
 *   <b>TODO:</b> extract a more general superclass called WebConfigParser for parsing init params from any
 *   webapp config object that declares a {@code getInitParameter(String)} method, such as
 *   {@link FilterConfig} and {@link ServletConfig} (which both use {@code init-param} values) or
 *   {@link ServletContext} (which uses {@code context-param} values).  We could introduce a decorator called {@code HasInitParameters}, which provides a
 *   {@code getInitParameter(String)} method that delegates to a wrapped {@link FilterConfig} or {@link ServletContext}
 *   object.
 * </p>
 * @author Alex
 * @since 1/2/2018
 */
public class FilterConfigParser {

  /**
   * Populates the fields of the given {@link FilterParameters} instance from the {@code init-param} values
   * in the given {@link FilterConfig}.
   * @param filterConfig the object received by {@link Filter#init(FilterConfig)},
   * containing the {@code init-param} values
   * @param parseResult a new instance of the class wich declares the desired fields
   * to be populated from the {@code init-param} values
   * @return the same {@code parseResult} instance that was passed in,
   * populated with the {@code init-param} values in {@code filterConfig}
   * @throws ParseException if any {@code init-param} value could not be bound to a field
   * in the given {@link FilterParameters} instance, or if a required {@code init-param} is missing
   */
  public <P extends FilterParameters> P parse(FilterConfig filterConfig, P parseResult) throws ParseException {
    try {
      return bindParamsToFields(filterConfig, parseResult);
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException(e);  // should never happen
    }
  }

  private <P extends FilterParameters> P bindParamsToFields(FilterConfig filterConfig, P parseResult) throws IllegalAccessException, ParseException {
    Field[] declaredFields = parseResult.getClass().getDeclaredFields();
    for (Field field : declaredFields) {
      String name = field.getName();
      if (name.startsWith("this$")) {
        continue;  // cls must be an inner class that's not static, and this field is a reference to its parent class instance
      }
      new FieldParser(field, filterConfig, parseResult).parseAndSetValue();
    }
    return parseResult;
  }

  protected static class FieldParser {
    private final Field field;
    private final FilterParameters parseResult;
    private final Param paramAnnotation;
    private final Class<?> type;
    private FilterConfig filterConfig;

    protected FieldParser(Field field, FilterConfig filterConfig, FilterParameters parseResult) {
      this.field = field;
      this.parseResult = parseResult;
      field.setAccessible(true);
      paramAnnotation = field.getAnnotation(Param.class);
      Class<?> type = field.getType();
      if (type.isPrimitive())
        type = Primitives.wrap(type);
      this.type = type;
      this.filterConfig = filterConfig;
    }

    String getParamName() {
      if (paramAnnotation != null) {
        String nameFromAnn = paramAnnotation.name();
        if (StringUtils.notBlank(nameFromAnn))
          return nameFromAnn;
      }
      return field.getName();
    }

    boolean isRequired() {
      return paramAnnotation != null && paramAnnotation.required();
    }

    private void setValue(Object value) throws IllegalAccessException {
      field.set(parseResult, value);
    }

    void parseAndSetValue() throws IllegalAccessException, ParseException {
      String name = getParamName();
      String strValue = filterConfig.getInitParameter(name);
      if (strValue == null) {
        if (isRequired())
          throw new RequiredParameterMissing(name, field.getType(), filterConfig);
        return;  // don't set a value for a field whose parameter is missing
      }
      // 1) if this is a string field, we just set a string value of the param
      if (type == String.class) {
        setValue(strValue);
        return;
      }
      // 2) otherwise, attempt to use the type's static valueOf(String) method, if available (this is the case for all primitive wrappers)
      {
        Method valueOfMethod = null;
        try {
          valueOfMethod = type.getMethod("valueOf", String.class);
        }
        catch (NoSuchMethodException e) {
          // ignore this exception; will attempt to use the parser defined by annotation
        }
        if (valueOfMethod != null && Modifier.isStatic(valueOfMethod.getModifiers())) {
          try {
            setValue(valueOfMethod.invoke(null, strValue));
            return;
          }
          catch (InvocationTargetException e) {
            throw new ParameterParseException(name, strValue, field.getType(), e, filterConfig);
          }
        }
      }
      // 3) attempt to use the parser defined by annotation
      {
        Class<? extends ParameterParser> parserClass = null;
        if (paramAnnotation != null) {
          // check that parserClass is not the default value of the annotation member
          if (paramAnnotation.parser() != ParameterParser.class) {
            parserClass = paramAnnotation.parser();
          }
        }
        if (parserClass == null)
          throw new ParameterParserNotAvailable(field, name, strValue, field.getType(), filterConfig);
        else {
          try {
            // attempt to instantiate the parser
            ParameterParser parser = parserClass.newInstance();
            setValue(parser.parse(strValue));
          }
          catch (Exception e) {
            throw new ParameterParseException(name, strValue, field.getType(), e, filterConfig);
          }
        }
      }
    }
  }

}
