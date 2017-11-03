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

package solutions.trsoftware.commons.shared.util.text;

import solutions.trsoftware.commons.client.bridge.text.AbstractNumberFormatter;
import solutions.trsoftware.commons.client.util.StringUtils;

import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * Allows formatting and parsing numbers using the same class in both clientside GWT code and serverside Java code.
 *
 *<p>
 *  <b>NOTE</b>: this requires {@link DecimalFormat} to be emulated in GWT (it's not part of GWT's
 *  <a href="http://www.gwtproject.org/doc/latest/RefJreEmulation.html">JRE emulation library</a>).
 *
 *  Our emulated version of {@link DecimalFormat} is located in our module's {@code super-source} directory
 *  ({@code src/solutions/trsoftware/commons/translatable})
 *</p>
 * <span color="green">TODO: use this class to replace {@link AbstractNumberFormatter}</span>
 * @author Alex, 10/31/2017
 */
public class SharedNumberFormat {

  private DecimalFormat format;

  /**
   * Creates a {@link DecimalFormat} with the given pattern.
   * @param pattern a formatting string as described in the doc for {@link DecimalFormat}
   */
  public SharedNumberFormat(String pattern) {
    format = new DecimalFormat(pattern);
  }

  /**
   * Creates a {@link DecimalFormat} from a pattern string obtained by invoking {@link #buildPattern(int, int, int)}
   * with the given parameters.
   * @see #buildPattern(int, int, int)
   */
  public SharedNumberFormat(int minIntegerDigits, int minFractionDigits, int maxFractionDigits) {
    this(buildPattern(minIntegerDigits, minFractionDigits, maxFractionDigits));
  }

  /**
   * Creates a {@link DecimalFormat} from a pattern string obtained by invoking
   * {@link #buildPattern(int, int, int) <code>buildPattern(0, 0, maxFractionDigits)</code>}
   * @see #buildPattern(int, int, int)
   */
  public SharedNumberFormat(int maxFractionDigits) {
    this(buildPattern(0, 0, maxFractionDigits));
  }

  /**
   * Creates a format pattern string suitable for the {@link DecimalFormat} constructor based on the given parameters.
   * @return a string suitable for {@link DecimalFormat#DecimalFormat(String)}
   */
  public static String buildPattern(int minIntegerDigits, int minFractionDigits, int maxFractionDigits) {
    StringBuilder patternBuffer = new StringBuilder();
    if (minIntegerDigits <= 0)
      patternBuffer.append('#');
    else
      patternBuffer.append(requiredDigitsPattern(minIntegerDigits));
    maxFractionDigits = Math.max(minFractionDigits, maxFractionDigits);  // ensure that maxFD >= minFD
    if (minFractionDigits > 0 || maxFractionDigits > 0)
      patternBuffer.append('.');
    if (minFractionDigits > 0)
      patternBuffer.append(requiredDigitsPattern(minFractionDigits));
    if (maxFractionDigits > 0)
      patternBuffer.append(optionalDigitsPattern(maxFractionDigits - minFractionDigits));
    return patternBuffer.toString();
  }

  private static String requiredDigitsPattern(int nDigits) {
    return StringUtils.repeat('0', nDigits);
  }

  private static String optionalDigitsPattern(int nDigits) {
    return StringUtils.repeat('#', nDigits);
  }

  public String format(double value) {
    return format.format(value);
  }

  public Number parse(String source) throws ParseException {
    return format.parse(source);
  }
}
