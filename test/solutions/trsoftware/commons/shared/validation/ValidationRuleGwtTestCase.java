/*
 * Copyright 2018 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.shared.validation;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;

/**
 * Feb 28, 2011
 *
 * @author Alex
 */
public abstract class ValidationRuleGwtTestCase extends CommonsGwtTestCase {

  protected void assertValidity(BaseValidationRule validator, String testString, boolean valid) throws Exception {
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
