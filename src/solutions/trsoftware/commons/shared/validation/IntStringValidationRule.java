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
 * Checks the min/max value of a string that represents an integer.
 *
 * Date: Jun 5, 2008 Time: 6:31:35 PM
 *
 * @author Alex
 */
public class IntStringValidationRule extends BaseStringValidationRule {
  private int minValue;
  private int maxValue;

  public IntStringValidationRule(String fieldName, boolean acceptNull, int minValue, int maxValue) {
    super(fieldName, acceptNull);
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  /** Accepts all numbers in the int range */
  public IntStringValidationRule(String fieldName, boolean acceptNull) {
    this(fieldName, acceptNull, Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

  public int getMinValue() {
    return minValue;
  }

  public int getMaxValue() {
    return maxValue;
  }

  protected ValidationResult applyValidationLogic(String value) {
    boolean valid;
    try {
      int number = Integer.parseInt(value);
      valid = number >= minValue && number <= maxValue;
    }
    catch (NumberFormatException e) {
      valid = false;
    }
    
    if (valid)
      return success();
    else {
      String errorMsg = "must be a number";
      if (minValue != Integer.MIN_VALUE && maxValue != Integer.MAX_VALUE)
        errorMsg += StringUtils.template(" between $1 and $2", minValue, maxValue);
      else if (minValue == Integer.MIN_VALUE)
        errorMsg += " less than or equal to " + maxValue;
      else // maxValue == Integer.MAX_VALUE
        errorMsg += " greater than or equal to " + minValue;
      return error(errorMsg);
    }
  }
}
