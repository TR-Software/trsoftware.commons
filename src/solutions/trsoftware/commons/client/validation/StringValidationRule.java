package solutions.trsoftware.commons.client.validation;

/**
 * Checks the length of the string.
 *
 * @author Alex
 */
public class StringValidationRule extends ValidationRule {
  private int minLength;
  private int maxLength;

  public StringValidationRule(String fieldName, int minLength, int maxLength, boolean acceptNull) {
    super(fieldName, acceptNull);
    this.minLength = minLength;
    this.maxLength = maxLength;
  }

  public int getMaxLength() {
    return maxLength;
  }

  public int getMinLength() {
    return minLength;
  }

  @Override
  protected ValidationResult applyValidationLogic(String value) {
    if (value.length() >= minLength && value.length() <= maxLength)
      return success();
    return error("must be between " + minLength + " and " + maxLength + " characters long");
  }
}
