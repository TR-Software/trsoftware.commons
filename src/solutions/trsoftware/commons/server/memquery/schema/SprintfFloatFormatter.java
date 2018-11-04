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

package solutions.trsoftware.commons.server.memquery.schema;

import solutions.trsoftware.commons.shared.util.StringUtils;

/**
 * Enhances {@link SprintfColFormatter} for floating-point numbers: strips trailing zeroes and decimal point if necessary.
 * @author Alex, 11/2/2016
 */
public class SprintfFloatFormatter extends SprintfColFormatter {

  private final Integer fractionalDigits;

  /** This suffix will be stripped to remove trailing zeroes */
  private final String suffixToStrip;

  public SprintfFloatFormatter(boolean thousandsSeparator, Integer width, Integer fractionalDigits, boolean stripTrailingZeroes) {
    super(getFormatSpec(thousandsSeparator, width, fractionalDigits));
    this.fractionalDigits = fractionalDigits;
    if (stripTrailingZeroes)
      suffixToStrip = "." + StringUtils.repeat('0', fractionalDigits);
    else
      suffixToStrip = null;
  }

  /**
   * @return a string that looks like {@code "%,.2f"}, based on the given parameters
   */
  public static String getFormatSpec(boolean thousandsSeparator, Integer width, Integer fractionalDigits) {
    StringBuilder ret = new StringBuilder("%");
    if (thousandsSeparator)
      ret.append(',');
    if (width != null)
      ret.append(width);
    if (fractionalDigits != null)
      ret.append('.').append(fractionalDigits);
    ret.append('f');
    return ret.toString();
  }

  @Override
  public String format(Object value) {
    String ret = super.format(value);
    if (suffixToStrip != null && ret.endsWith(suffixToStrip))
      return ret.substring(0, ret.length() - suffixToStrip.length());
    return ret;
  }

//
//  @Override
//  public String format(Object value) {
//    String formattedValue = super.format(value);
//    double fractionalPart;
//    if (value instanceof Float)
//      fractionalPart = MathUtils.getFractionalPart((Float)value);
//    else if (value instanceof Double)
//      fractionalPart = MathUtils.getFractionalPart((Double)value);
//    else
//      throw new IllegalArgumentException();
//
//    // TODO: cont here: this doesn't work for values like 477981.99999999825 (in which case this method returns "477,982.00")
//
//    if (fractionalPart >= Math.pow(10, -fractionalDigits))
//      return formattedValue;
//    else {
//      // delete trailing zeroes and decimal point (if we have a round number)
//      StringBuilder ret = new StringBuilder(formattedValue);
//      for (int i = ret.length() - 1; i >= 0; i--) {
//        char c = ret.charAt(i);
//        if (c == '0' || c == '.')
//          ret.deleteCharAt(i);
//        else
//          break;
//      }
//      return ret.toString();
//    }
//  }

}
