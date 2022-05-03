/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util.text;

import com.google.common.base.MoreObjects;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * Allows formatting and parsing numbers using the same class in both client-side GWT code and server-side Java code.
 * 
 * <h3>Implementation Note</h3>  
 * Delegates to {@link java.text.DecimalFormat}, which we're emulating for GWT using
 * {@link com.google.gwt.i18n.client.NumberFormat} (see this module's {@code super-source} directory:
 * {@code src/solutions/trsoftware/commons/translatable/})
 *
 * <h3>Rounding Behavior</h3>
 * GWT's {@link com.google.gwt.i18n.client.NumberFormat} always does <em>half-up</em> rounding), with no way to change
 * this behavior, therefore we're setting {@link DecimalFormat}'s rounding mode to {@link RoundingMode#HALF_UP} to
 * emulate GWT's behavior while running in a JVM.
 *
 * However, the rounding behavior of {@link #format(double)} might still differ when the argument can't be represented
 * exactly as a {@code double} (e.g. <a href="https://stackoverflow.com/q/45479713/1965404">1.15</a>), for example:
 * <pre>
 *   // in client-side GWT:
 *   com.google.gwt.i18n.client.NumberFormat.getFormat("#.##").format(1.005);  // returns "1.01"
 *   // in JVM:
 *   DecimalFormat decimalFormat = new DecimalFormat("#.##");
 *   decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
 *   decimalFormat.format(1.005);  // returns "1"
 * </pre>
 * That's because the actual value of {@code 1.005} in IEEE floating-point is actually something like {@code 1.004999...},
 * but {@link com.google.gwt.i18n.client.NumberFormat} performs the rounding on the string representation
 * of the given {@code double}, while {@link DecimalFormat} performs the rounding using IEEE 754 floating-point arithmetic.
 * To get the same behavior from both, instead of {@link #format(double)}, call {@link #format(Number)}
 * with a {@link java.math.BigDecimal} argument (the "exact" representation of any double can be obtained via
 * {@link java.math.BigDecimal#valueOf(double)}).
 *
 * @see <a href="http://www.gwtproject.org/doc/latest/RefJreEmulation.html">JRE emulation library</a>
 * @author Alex, 10/31/2017
 */
public class SharedNumberFormat {

  // TODO: use this class to replace solutions.trsoftware.commons.client.bridge.text.AbstractNumberFormatter

  private final DecimalFormat format;

  /**
   * Creates a {@link DecimalFormat} with the given pattern.
   * @param pattern a formatting string as described in the doc for {@link DecimalFormat}
   */
  public SharedNumberFormat(String pattern) {
    format = new DecimalFormat(pattern);
    format.setRoundingMode(RoundingMode.HALF_UP);
  }

  /**
   * Creates a {@link DecimalFormat} from a pattern string obtained by invoking {@link #buildPattern(int, int, int, boolean)}
   * with the given parameters.
   * @see #buildPattern(int, int, int, boolean)
   */
  public SharedNumberFormat(int minIntegerDigits, int minFractionDigits, int maxFractionDigits, boolean percent) {
    this(buildPattern(minIntegerDigits, minFractionDigits, maxFractionDigits, percent));
  }

  /**
   * Creates a {@link DecimalFormat} from a pattern string obtained by invoking
   * {@link #buildPattern(int, int, int, boolean) <code>buildPattern(0, 0, maxFractionDigits, false)</code>}
   * @see #buildPattern(int, int, int, boolean)
   */
  public SharedNumberFormat(int maxFractionDigits) {
    this(buildPattern(0, 0, maxFractionDigits, false));
  }

  /**
   * Creates a format pattern string suitable for the {@link DecimalFormat} constructor based on the given parameters.
   * <p style="color: #0073BF; font-weight: bold;">
   *   TODO: add support for digit grouping (e.g. {@code #,###.##} to group thousands, or {@code #,##.##} to group hundreds, etc.)
   * </p>
   * @return a string suitable for {@link DecimalFormat#DecimalFormat(String)}
   */
  public static String buildPattern(int minIntegerDigits, int minFractionDigits, int maxFractionDigits, boolean percent) {
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
    if (maxFractionDigits > 0) {
      int nOptional = maxFractionDigits - minFractionDigits;
      if (nOptional > 0)
        patternBuffer.append(optionalDigitsPattern(nOptional));
    }
    if (percent)
      patternBuffer.append('%');
    return patternBuffer.toString();
  }

  private static String requiredDigitsPattern(int nDigits) {
    return StringUtils.repeat('0', nDigits);
  }

  private static String optionalDigitsPattern(int nDigits) {
    return StringUtils.repeat('#', nDigits);
  }

  /**
   * NOTE: to get more accurate rounding behavior for {@code double} values with no exact IEEE representation,
   * call {@link #format(Number)} with a {@link java.math.BigDecimal} argument instead (see explanation in the 
   * javadoc for this class: {@link SharedNumberFormat})
   * @see <a href="https://stackoverflow.com/q/45479713/1965404">DecimalFormat with HALF_UP rounding mode</a>
   */
  public String format(double value) {
    return format.format(value);
  }

  public String format(Number number) {
    return format.format(number);
  }

  /**
   * Parses the given string into a number according to this formatting pattern.
   * Returns a {@code double} instead of {@link Number} (as in {@link DecimalFormat#parse(String)})
   * because on the client-side, our emulated version of {@link DecimalFormat}
   * ({@code src/solutions/trsoftware/commons/translatable/java/text/DecimalFormat.java})
   * uses {@link com.google.gwt.i18n.client.NumberFormat#parse(String)}, which returns a {@code double}.
   */
  public double parse(String source) throws ParseException {
    return format.parse(source).doubleValue();
  }

  /**
   * @return the pattern used by this number format.
   */
  public String getPattern() {
    return format.toPattern();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .addValue(StringUtils.quote(getPattern()))
        .toString();
  }
}
