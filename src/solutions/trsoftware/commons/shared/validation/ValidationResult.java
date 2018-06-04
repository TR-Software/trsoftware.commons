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

import java.io.Serializable;

/**
 * @since May 31, 2008
 * @author Alex
 */
public class ValidationResult implements Serializable {
  private boolean valid;
  private String errorMessage;

  public static final ValidationResult SUCCESS = new ValidationResult(true, null);

  private ValidationResult(boolean valid, String errorMessage) {
    this.valid = valid;
    this.errorMessage = errorMessage;
  }

  private ValidationResult() {
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

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    ValidationResult that = (ValidationResult)o;

    if (valid != that.valid)
      return false;
    return errorMessage != null ? errorMessage.equals(that.errorMessage) : that.errorMessage == null;
  }

  @Override
  public int hashCode() {
    int result = (valid ? 1 : 0);
    result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ValidationResult{");
    sb.append("valid=").append(valid);
    sb.append(", errorMessage='").append(errorMessage).append('\'');
    sb.append('}');
    return sb.toString();
  }

  /**
   * @throws ValidationError iff this instance represents an error
   * @since 1/2/2018
   */
  public void assertValid() throws ValidationError {
    if (!valid)
      throw new ValidationError(this);
  }
}
