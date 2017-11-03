package solutions.trsoftware.commons.client.validation;

/**
 * Validates a string against one or more regexes.
 *
 * @author Alex
 */
public class RegexValidationRule extends ValidationRule {

  private String[] regExps;
  private String errorMsg;

  public RegexValidationRule(String fieldName, String errorMsg, boolean acceptNull, String... regExps) {
    super(fieldName, acceptNull);
    this.errorMsg = errorMsg;
    this.regExps = regExps;
  }

  public RegexValidationRule(String fieldName, boolean acceptNull, String... regExps) {
    this(fieldName, "contains invalid characters", acceptNull, regExps);
  }

  @Override
  protected ValidationResult applyValidationLogic(String value) {
    for (String regex : regExps) {
      if (!value.matches(regex))
        return error();
    }
    return success();
  }

  protected ValidationResult error() {
    return error(errorMsg);
  }

  public String[] getRegExps() {
    return regExps;
  }

}
