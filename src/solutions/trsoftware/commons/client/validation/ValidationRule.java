package solutions.trsoftware.commons.client.validation;

import solutions.trsoftware.commons.client.util.StringUtils;

/**
 * Date: Jun 24, 2007
 * Time: 3:00:16 PM
 *
 * @author Alex
 */
public abstract class ValidationRule {
  private String fieldName;
  private boolean acceptNull;

  protected ValidationRule() {
  }

  protected ValidationRule(String fieldName, boolean acceptNull) {
    this.fieldName = fieldName;
    this.acceptNull = acceptNull;
  }

  public String getFieldName() {
    return fieldName;
  }

  public boolean acceptsNull() {
    return acceptNull;
  }

  public final ValidationResult validate(String value) {
    return validate(value, acceptNull);
  }

  /**
   * Determines if the given value for this field is valid.
   * @param value The string input value
   * @param acceptBlank Whether a null or empty string is allowed (an override
   * for the acceptNull field value of this object).
   */
  public final ValidationResult validate(String value, boolean acceptBlank) {
    if (StringUtils.isBlank(value)) {
      if (acceptBlank)
        return success();
      else
        return errorMissing();
    }
    return applyValidationLogic(value);
  }

  /** Subclasses should implement */
  protected abstract ValidationResult applyValidationLogic(String value);

  protected ValidationResult success() {
    return ValidationResult.success();
  }

  /** Returns an error result indicating that the field is missing */
  public ValidationResult errorMissing() {
    return error("must be specified");
  }

  /** Returns an error result with the given message */
  public ValidationResult error(String message) {
    return ValidationResult.error(formatErrorMessage(message));
  }

  /**
   * Prepends the field name, if any, otherwise capitalizes the error message.
   *
   * @param errorMessage Must be lowercase.
   * @return
   */
  private String formatErrorMessage(String errorMessage) {
    if (fieldName == null)
      return StringUtils.capitalize(errorMessage);
    else
      return StringUtils.capitalize(fieldName) + " " + errorMessage;
  }
}
