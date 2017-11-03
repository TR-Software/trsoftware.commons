package solutions.trsoftware.commons.client.validation;

/**
 * Date: May 31, 2008 Time: 4:36:39 PM
 *
 * @author Alex
 */
public class ValidationResult {
  private boolean valid;
  private String errorMessage;

  private static final ValidationResult SUCCESS = new ValidationResult(true, null);

  private ValidationResult(boolean valid, String errorMessage) {
    this.valid = valid;
    this.errorMessage = errorMessage;
  }

  public boolean isValid() {
    return valid;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public static ValidationResult success() {
    return SUCCESS;
  }

  public static ValidationResult error(String errorMessage) {
    return new ValidationResult(false, errorMessage);
  }

}
