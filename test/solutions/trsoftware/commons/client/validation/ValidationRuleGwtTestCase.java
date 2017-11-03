package solutions.trsoftware.commons.client.validation;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;

/**
 * Feb 28, 2011
 *
 * @author Alex
 */
public abstract class ValidationRuleGwtTestCase extends CommonsGwtTestCase {

  protected void assertValidity(ValidationRule validator, String testString, boolean valid) throws Exception {
    String msg = "Asserting that " + validator.getFieldName() + " = " + testString + " is " + (valid ? "accepted" : "rejected");
    System.out.println(msg);

    // 1). check the validator
    ValidationResult validationResult = validator.validate(testString);
    assertEquals(msg, valid, validationResult.isValid());
    if (valid)
      assertNull(validationResult.getErrorMessage());
    else
      assertNotNull(validationResult.getErrorMessage());
  }

}
