package solutions.trsoftware.commons.server.servlet.config;

import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;

import java.lang.reflect.Field;

import static solutions.trsoftware.commons.server.servlet.config.InitParameters.Param;
import static solutions.trsoftware.commons.server.servlet.config.InitParameters.ParameterParser;

/**
 * @author Alex
 * @since 1/3/2018
 */
public class InitParameterParserNotAvailable extends WebConfigException {
  private Field field;

  public InitParameterParserNotAvailable(Field field, String paramName, String paramValue, Class<?> expectedType, HasInitParameters config) {
    super(ReflectionUtils.toString(field) + " must contain a @" + Param.class.getSimpleName()
            + " annotation specifying a " + ParameterParser.class.getSimpleName()
            + "<" + expectedType.getSimpleName() + "> subclass",
        paramName, paramValue, expectedType, config);
    this.field = field;
  }

  public Field getField() {
    return field;
  }

}
