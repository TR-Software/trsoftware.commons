package solutions.trsoftware.commons.server.servlet.config;


/**
 * @author Alex
 * @since 1/3/2018
 */
public class RequiredInitParameterMissing extends WebConfigException {
  public RequiredInitParameterMissing(String paramName, Class<?> expectedType, HasInitParameters config) {
    super("Missing value for required init-param", paramName, null, expectedType, config);
  }
}
