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

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.NumberRange;

/**
 * @author Alex
 * @since 12/30/2017
 */
public class NumberValidationRuleTest extends TestCase {

  public void testValidate() throws Exception {
    {
      int min = 1;
      int max = 10;
      for (boolean acceptNull : new boolean[]{true, false}) {
        String fieldName = "foo";
        NumberValidationRule<Integer> rule = new NumberValidationRule<Integer>(fieldName, new NumberRange<Integer>(min, max), acceptNull
        );
        String errMsgPrefix = "Foo must be a number";
        ValidationResult nullResult = rule.validate(null);
        assertEquals(acceptNull, nullResult.isValid());
        assertEquals(acceptNull, nullResult.getErrorMessage() == null);
        for (int x = min - 5; x < max + 5; x++) {
          ValidationResult result = rule.validate(x);
          if (x >= min && x <= max) {
            assertTrue(result.isValid());
            assertNull(result.getErrorMessage());
          }
          else if (x < min) {
            assertFalse(result.isValid());
            assertEquals(errMsgPrefix + " between " + min + " and " + max, result.getErrorMessage());
          }
        }
        // now try with one endpoint unbounded
        {
          NumberValidationRule<Integer> ruleNoMin = new NumberValidationRule<Integer>(fieldName, new NumberRange<Integer>(Integer.MIN_VALUE, max), acceptNull
          );
          ValidationResult result = ruleNoMin.validate(max + 1);
          assertFalse(result.isValid());
          assertEquals(errMsgPrefix + " less than or equal to " + max, result.getErrorMessage());
        }
        {
          NumberValidationRule<Integer> ruleNoMax = new NumberValidationRule<Integer>(fieldName, new NumberRange<Integer>(min, Integer.MAX_VALUE), acceptNull
          );
          ValidationResult result = ruleNoMax.validate(min - 1);
          assertFalse(result.isValid());
          assertEquals(errMsgPrefix + " greater than or equal to " + min, result.getErrorMessage());
        }
      }
    }
  }
}