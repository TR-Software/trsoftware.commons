/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Date: Oct 24, 2007
 * Time: 12:09:43 PM
 *
 * @author Alex
 */
public class CompositeValidationRule extends ValidationRule {

  List<ValidationRule> rules = new ArrayList<ValidationRule>();

  public CompositeValidationRule(List<ValidationRule> rules) {
    this.rules.addAll(rules);
  }

  public CompositeValidationRule(ValidationRule... rules) {
    this.rules.addAll(Arrays.asList(rules));
  }

  protected ValidationResult applyValidationLogic(String value) {
    for (ValidationRule rule : rules) {
      ValidationResult validationResult = rule.applyValidationLogic(value);
      if (!validationResult.isValid())
        return validationResult;
    }
    return success();
  }
}
