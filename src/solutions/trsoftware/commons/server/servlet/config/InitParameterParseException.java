package solutions.trsoftware.commons.server.servlet.config;

/**
 * @author Alex
 * @since 1/3/2018
 */
public class InitParameterParseException extends WebConfigException {
  public InitParameterParseException(String paramName, String paramValue, Class<?> expectedType, Throwable cause, HasInitParameters config) {
    super("Unable to parse init-param", cause, paramName, paramValue, expectedType, config);
  }
}
