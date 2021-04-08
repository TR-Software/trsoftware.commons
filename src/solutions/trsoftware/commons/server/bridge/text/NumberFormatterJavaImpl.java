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

package solutions.trsoftware.commons.server.bridge.text;

import solutions.trsoftware.commons.client.bridge.text.AbstractNumberFormatter;

import java.math.RoundingMode;
import java.text.NumberFormat;

/**
 * Java serverside implementation of NumberFormatter.
 *
 * @author Alex
 */
public class NumberFormatterJavaImpl extends AbstractNumberFormatter {
  private NumberFormat delegate;

  public NumberFormatterJavaImpl(int minIntegerDigits, int minFractionalDigits, int maxFractionalDigits, boolean digitGrouping, boolean percent) {
    super(minIntegerDigits, minFractionalDigits, maxFractionalDigits, digitGrouping);
    if (percent)
      delegate = NumberFormat.getPercentInstance();
    else
      delegate = NumberFormat.getNumberInstance();
    delegate.setMinimumFractionDigits(minFractionalDigits);
    delegate.setMaximumFractionDigits(maxFractionalDigits);
    delegate.setGroupingUsed(digitGrouping);
    delegate.setRoundingMode(RoundingMode.HALF_UP);  // set rounding to match GWT's NumberFormat
    delegate.setMinimumIntegerDigits(minIntegerDigits);
  }

  /**
   * Formats the given number using the constructor parameters to control how
   * the number will appear.
   */
  public String format(double number) {
    return delegate.format(number);
  }


}