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

import solutions.trsoftware.commons.shared.util.NumberRange;

/**
 * Checks the min/max value of an integer.
 *
 * @author Alex
 * @since 12/31/2017
 */
public class IntegerValidationRule extends NumberValidationRule<Integer> {

  public IntegerValidationRule(String fieldName, int min, int max, boolean acceptNull) {
    super(fieldName, new NumberRange<Integer>(min, max), acceptNull);
  }

  public IntegerValidationRule(String fieldName, int min, int max) {
    this(fieldName, min, max, false);
  }


}
