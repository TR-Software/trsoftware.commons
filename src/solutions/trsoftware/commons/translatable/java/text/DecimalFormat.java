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

package java.text;

import com.google.gwt.i18n.client.NumberFormat;
import solutions.trsoftware.commons.client.text.CorrectedNumberFormat;

import java.math.RoundingMode;

/**
 * The purpose of this class is to allow client/shared code referring to {@link java.text.DecimalFormat} to compile
 * with the GWT compiler.
 * @see solutions.trsoftware.commons.shared.util.text.SharedNumberFormat
 */
public class DecimalFormat {

  private final CorrectedNumberFormat format;

  public DecimalFormat(String pattern) {
    format = new CorrectedNumberFormat(NumberFormat.getFormat(pattern));
  }

  public String format(double value) {
    return format.format(value);
  }

  /**
   * @param obj should be an instance of {@link Number}, otherwise will throw {@link IllegalArgumentException}
   */
  public final String format(Object obj) {
    if (obj instanceof Number) {
      Number number = (Number)obj;
      return format.format(number);
    }
    else
      throw new IllegalArgumentException("Cannot format given Object as a Number");
  }

  public Number parse(String source) throws ParseException {
    try {
      return format.parse(source);
    }
    catch (NumberFormatException e) {
      throw new ParseException(e.getMessage(), 0);
    }
  }

  /**
   * Synthesizes a pattern string that represents the current state
   * of this Format object.
   *
   * @return a pattern string
   * @see java.text.DecimalFormat#applyPattern(String)
   */
  public String toPattern() {
    return format.getPattern();
  }

  /**
   * No-op ({@link NumberFormat} doesn't support rounding modes).
   */
  public void setRoundingMode(RoundingMode roundingMode) {}

}