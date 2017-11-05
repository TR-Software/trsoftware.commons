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

package solutions.trsoftware.commons.shared.validation;

/**
 * Checks the length of the string.
 *
 * @author Alex
 */
public class StringValidationRule extends ValidationRule {
  private int minLength;
  private int maxLength;

  public StringValidationRule(String fieldName, int minLength, int maxLength, boolean acceptNull) {
    super(fieldName, acceptNull);
    this.minLength = minLength;
    this.maxLength = maxLength;
  }

  public int getMaxLength() {
    return maxLength;
  }

  public int getMinLength() {
    return minLength;
  }

  @Override
  protected ValidationResult applyValidationLogic(String value) {
    if (value.length() >= minLength && value.length() <= maxLength)
      return success();
    return error("must be between " + minLength + " and " + maxLength + " characters long");
  }
}
