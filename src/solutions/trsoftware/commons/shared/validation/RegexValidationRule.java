/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.commons.shared.validation;

/**
 * Validates a string against one or more regexes.
 *
 * @author Alex
 */
public class RegexValidationRule extends BaseStringValidationRule {

  private String[] regExps;
  private String errorMsg;

  public RegexValidationRule(String fieldName, String errorMsg, boolean acceptNull, String... regExps) {
    super(fieldName, acceptNull);
    this.errorMsg = errorMsg;
    this.regExps = regExps;
  }

  public RegexValidationRule(String fieldName, boolean acceptNull, String... regExps) {
    this(fieldName, "contains invalid characters", acceptNull, regExps);
  }

  @Override
  protected ValidationResult applyValidationLogic(String value) {
    for (String regex : regExps) {
      if (!value.matches(regex))
        return error();
    }
    return success();
  }

  protected ValidationResult error() {
    return error(errorMsg);
  }

  public String[] getRegExps() {
    return regExps;
  }

}
