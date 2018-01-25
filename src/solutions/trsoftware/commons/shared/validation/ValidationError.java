package solutions.trsoftware.commons.shared.validation;

/**
 * Something failed to validate with a {@link ValidationRule}
 *
 * @author Alex
 * @since 1/2/2018
 */
public class ValidationError extends Exception {

  private ValidationResult validationResult;

  public ValidationError(ValidationResult validationResult) {
    super(validationResult.getErrorMessage());
    this.validationResult = validationResult;
  }

  private ValidationError() {
  }

  public ValidationResult getValidationResult() {
    return validationResult;
  }
}
