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

package solutions.trsoftware.commons.client.text;

import com.google.gwt.i18n.client.NumberFormat;
import solutions.trsoftware.commons.shared.util.MathUtils;

/**
 * Decorator for {@link NumberFormat} that attempts to correct some of its bugs and errors.
 *
 * @author Alex
 * @since 4/23/2018
 */
public class CorrectedNumberFormat {
  /*
    Reported in https://github.com/gwtproject/gwt/issues/9611
    TODO: contribute these bug-fixes to these to the GWT project (https://github.com/gwtproject/gwt/)
   */

  /**
   * The delegate of this decorator.
   */
  private NumberFormat numberFormat;
  /**
   * The pattern string used by the {@link #numberFormat} delegate.
   * @see NumberFormat#getPattern()
   */
  private final String pattern;

  public CorrectedNumberFormat(NumberFormat numberFormat) {
    this.numberFormat = numberFormat;
    this.pattern = numberFormat.getPattern();
  }

  public String format(double number) {
    return numberFormat.format(number);
  }

  public String format(Number number) {
    return numberFormat.format(number);
  }

  public double parse(String text) throws NumberFormatException {
    return maybeCorrectParseResult(numberFormat.parse(text));
  }

  private double maybeCorrectParseResult(double parseResult) {
    return correctMultiplierError(parseResult);
  }

  /**
   * Attempts to correct the bug that {@link NumberFormat#parse(String)} fails to divide by the multiplier it
   * used in {@link NumberFormat#format(double)} (which happens when formatting percent, per mille, etc.)
   *
   * @param parseResult the value returned by {@link NumberFormat#parse(String)}
   * @return the result divided by the appropriate multiplier, if any
   * @see NumberFormat#multiplier
   * @see <a href="https://github.com/gwtproject/gwt/issues/9611">NumberFormat bug when parsing percentages</a>
   */
  private double correctMultiplierError(double parseResult) {
    // first, check that the multiplier error is actually present
    double multiplier = numberFormat.parse(numberFormat.format(1));
    if (!MathUtils.equal(1, multiplier, MathUtils.EPSILON)) {
      // the parse method probably failed to divide by the multiplier
      parseResult /= multiplier;
    }
    return parseResult;
  }

  public String getPattern() {
    return pattern;
  }
}
