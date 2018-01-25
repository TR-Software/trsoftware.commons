package solutions.trsoftware.commons.server.servlet.filters.config;

import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;

import javax.servlet.FilterConfig;
import java.lang.reflect.Field;

/**
 * @author Alex
 * @since 1/3/2018
 */
class ParameterParserNotAvailable extends ParseException {
  private Field field;

  ParameterParserNotAvailable(Field field, String paramName, String paramValue, Class<?> expectedType, FilterConfig filterConfig) {
    super(ReflectionUtils.toString(field) + " must contain a @" + FilterParameters.Param.class.getSimpleName()
            + " annotation specifying a " + FilterParameters.ParameterParser.class.getSimpleName()
            + "<" + expectedType.getSimpleName() + "> subclass",
        paramName, paramValue, expectedType, filterConfig);
    this.field = field;
  }

  public Field getField() {
    return field;
  }

}
