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

/**
 * Date: May 31, 2008 Time: 4:36:39 PM
 *
 * @author Alex
 */
public class ValidationResult {
  private boolean valid;
  private String errorMessage;

  private static final ValidationResult SUCCESS = new ValidationResult(true, null);

  private ValidationResult(boolean valid, String errorMessage) {
    this.valid = valid;
    this.errorMessage = errorMessage;
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

}
