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

import solutions.trsoftware.commons.shared.util.NumberRange;
import solutions.trsoftware.commons.shared.util.NumberUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

/**
 * Checks the min/max value of a number (which can be any subclass of {@link Number}).
 *
 * @author Alex
 */
public class NumberValidationRule<V extends Number & Comparable> extends BaseObjectValidationRule<V> {
  private final NumberRange<V> acceptableRange;

  public NumberValidationRule(String fieldName, NumberRange<V> acceptableRange, boolean acceptNull) {
    super(fieldName, acceptNull);
    this.acceptableRange = acceptableRange;
  }

  public NumberValidationRule(String fieldName, V min, V max) {
    this(fieldName, new NumberRange<V>(min, max), false);
  }

  @Override
  protected ValidationResult applyValidationLogic(V value) {
    if (acceptableRange == null || acceptableRange.contains(value)) {
      return success();
    }
    else {
      String errorMsg = "must be a number";
      V minValue = acceptableRange.min();
      V maxValue = acceptableRange.max();
      Number absoluteMin = NumberUtils.minValue(value.getClass());
      Number absoluteMax = NumberUtils.maxValue(value.getClass());
      if (!minValue.equals(absoluteMin) && !maxValue.equals(absoluteMax))
        errorMsg += StringUtils.template(" between $1 and $2", formatValue(minValue), formatValue(maxValue));
      else if (minValue.equals(absoluteMin))
        errorMsg += " less than or equal to " + formatValue(maxValue);
      else // maxValue.equals(absoluteMax)
        errorMsg += " greater than or equal to " + formatValue(minValue);
      return error(errorMsg);
    }
  }

  /**
   * Can override to allow custom formatting of values for {@link ValidationResult#errorMessage}.
   * @return the result of {@link Object#toString()} for the given value, but subclasses may override to provide custom
   * formatting.
   */
  protected String formatValue(V value) {
    return String.valueOf(value);
  }

  public NumberRange<V> getAcceptableRange() {
    return acceptableRange;
  }
}
