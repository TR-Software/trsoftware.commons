package solutions.trsoftware.commons.shared.validation;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.RandomUtils;

/**
 * @author Alex
 * @since 1/1/2018
 */
public class StringLengthValidationRuleTest extends TestCase {

  public void testValidate() throws Exception {
    StringLengthValidationRule rule = new StringLengthValidationRule("foo", 5, 10, false);
    assertEquals(ValidationResult.error("Foo must be specified"), rule.validate(null));
    assertEquals(ValidationResult.error("Foo must be specified"), rule.validate(""));
    assertEquals(ValidationResult.error("Foo must be between 5 and 10 characters long"), rule.validate("x"));
    assertEquals(ValidationResult.error("Foo must be between 5 and 10 characters long"), rule.validate(RandomUtils.randString(4)));
    assertEquals(ValidationResult.error("Foo must be between 5 and 10 characters long"), rule.validate(RandomUtils.randString(11)));
    for (int i = 5; i < 11; i++) {
      assertEquals(ValidationResult.success(), rule.validate(RandomUtils.randString(i)));
    }
  }
}