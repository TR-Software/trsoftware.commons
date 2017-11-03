package solutions.trsoftware.commons.client.validation;

import junit.framework.TestCase;

/**
 * Feb 28, 2011
 *
 * @author Alex
 */
public class EmailAddressValidatorJavaTest extends TestCase {
  EmailAddressValidatorGwtTest delegate = new EmailAddressValidatorGwtTest();

  public void testEmailValidation() throws Exception {
    delegate.testEmailValidation();
  }
}