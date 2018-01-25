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

import solutions.trsoftware.commons.shared.util.StringUtils;

/**
 * Date: Jun 24, 2007
 * Time: 3:00:16 PM
 *
 * @author Alex
 */
public abstract class BaseValidationRule<V> implements ValidationRule<V> {
  private String fieldName;
  private boolean acceptNull;

  protected BaseValidationRule(String fieldName, boolean acceptNull) {
    this.fieldName = fieldName;
    this.acceptNull = acceptNull;
  }

  @Override
  public String getFieldName() {
    return fieldName;
  }

  public boolean acceptsNull() {
    return acceptNull;
  }

  @Override
  public final ValidationResult validate(V value) {
    return validate(value, acceptNull);
  }

  /**
   * Determines if the given value for this field is valid.
   * @param value The string input value
   * @param acceptBlank Whether a null or empty string is allowed (an override
   * for the acceptNull field value of this object).
   */
  public final ValidationResult validate(V value, boolean acceptBlank) {
    if (isNull(value)) {
      if (acceptBlank)
        return success();
      else
        return errorMissing();
    }
    return applyValidationLogic(value);
  }

  /**
   * @param value the input being validated
   * @return {@code true} if {@code value} can be considered "null" (e.g. {@code null} or {@code ""} if it's a string)
   */
  protected abstract boolean isNull(V value);

  /** Subclasses should implement */
  protected abstract ValidationResult applyValidationLogic(V value);

  protected ValidationResult success() {
    return ValidationResult.success();
  }

  /** @return an error result indicating that the field value is missing (as defined by {@link #isNull(Object)}) */
  public ValidationResult errorMissing() {
    return error("must be specified");
  }

  /** @return an error result with the given message */
  public ValidationResult error(String message) {
    return ValidationResult.error(formatErrorMessage(message));
  }

  /**
   * Prepends the field name, if any, otherwise capitalizes the error message.
   *
   * @param errorMessage Must be lowercase.
   */
  private String formatErrorMessage(String errorMessage) {
    if (fieldName == null)
      return StringUtils.capitalize(errorMessage);
    else
      return StringUtils.capitalize(fieldName) + " " + errorMessage;
  }
}
