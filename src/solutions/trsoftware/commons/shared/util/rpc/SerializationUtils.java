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

package solutions.trsoftware.commons.shared.util.rpc;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStreamReader;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStreamWriter;
import solutions.trsoftware.commons.shared.util.MathUtils;
import solutions.trsoftware.commons.shared.util.text.SharedNumberFormat;

import java.io.Serializable;

/**
 * Utilities for implementing
 * <a href="https://www.gwtproject.org/doc/latest/DevGuideServerCommunication.html#DevGuideCustomSerialization">
 *   custom serialization logic</a> for the GWT RPC protocol.
 *
 * @see CustomFieldSerializer
 * @author Alex
 * @since 4/23/2022
 */
public class SerializationUtils {

  /**
   * Writes the given {@code double} as an integer that represents the original value rounded to the given
   * number of decimal places, in order to save space over-the-wire by omitting the decimal point and any
   * fractional digits not needed on the client side.
   * <p>
   * <strong>Warnings</strong>:
   * <ol>
   * <li>This is a lossy conversion that reduces the already-limited precision
   * of floating point values even further, which may result in surprising behavior when working with the deserialized
   * result.
   * For example, consider a use-case of the clientside code rendering a {@code double} value from the server using
   * an instance of {@link NumberFormat} or {@link SharedNumberFormat} that displays the number with up to 2 fractional
   * digits. If the original serverside value happens to be something like {@code 74.53531691085458}, it might be serialized
   * (with precision=3) as {@code "74535"} and deserialized as {@code 74.535}. One might expect it to be rendered as
   * {@code "74.54"}, which is exactly what happens with {@code new SharedNumberFormat(2).format(74.53531691085458)}.
   * However, when given {@code 74.535}, {@link SharedNumberFormat#format(double)} actually returns {@code "74.53"},
   * because {@code 74.535} doesn't have an exact binary floating-point representation.  Examining it as a
   * {@link java.math.BigDecimal#BigDecimal(double) BigDecimal} reveals that {@code 74.535d} is actually represented
   * as something like {@code 74.534999...}, which explains the counterintuitive result produced by {@link SharedNumberFormat}.
   * Therefore, care should be taken to ensure that the {@code precision} argument is sufficiently high for any given
   * use-case (in the above example, increasing the precision to 4 digits achieves the desired result of {@code "74.54"}).
   * <li>
   * The space savings offered by this method are highly dependant upon the data being serialized.
   * For example, {@code writeRoundedDouble(28.48680753904732, 4)} saves 11 bytes
   * (by emitting {@code "284868"} instead of {@code "28.48680753904732"}), but {@code writeRoundedDouble(28.5, 4)} actually
   * wastes 2 bytes (by emitting {@code "285000"} instead of {@code "28.5"}).
   * Therefore it would be prudent to test with some actual application data before deciding whether it's worth using
   * this method.
   * <em>Note</em>: Another option would be to use {@link SerializationStreamWriter#writeDouble(double) writeDouble(double)}
   * in conjunction with {@link MathUtils#round(double, int)}
   * <li>
   * The rounding of negative values will be "half-down" because {@link Math#round(double)} rounds everything toward
   * positive infinity (i.e. {@code Math.round(1.5) == 2}, but {@code Math.round(-1.5) == -1}).
   * As a consequence, this method will output {@code "135"} when given {@code (1.345, 2)} and {@code "-134"} when
   * given {@code (-1.345, 2)}.  If that's a problem, use {@link SerializationStreamWriter#writeInt} directly
   * with the negated result of {@link #doubleToScaledInt(double, int)} on the negated value (i.e.
   * <nobr><code>-{@link #doubleToScaledInt}(1.345, 2) == -135</code></nobr>)
   * <li>
   * The caller must ensure that {@code value} can't be {@code NaN}, infinite, and that that
   * <code>value * 10<sup>precision</sup></code> would fit into a 32-bit integer.  Otherwise, should
   * use {@link SerializationStreamWriter#writeDouble(double) writeDouble(value)} instead.
   * </ol>
   *
   * @param value the original {@code double}; must be finite, not {@code NaN}, and
   *              <code>value * 10<sup>precision</sup></code> must fit into a 32-bit integer
   * @param precision the desired number of digits to retain after the decimal point (must be >= 0)
   * @throws AssertionError if assertions are enabled and the {@code value} arg is either {@code NaN} or infinite,
   * the {@code precision} arg is negative, or the result doesn't fit into an {@code int}.
   *
   * @see #readRoundedDouble(SerializationStreamReader, int)
   * @see #doubleToScaledInt(double, int)
   * @see MathUtils#round(double, int)
   */
  public static void writeRoundedDouble(SerializationStreamWriter writer, double value, int precision) throws SerializationException {
    writer.writeInt(doubleToScaledInt(value, precision));
    // TODO: maybe inline this method?

  }

  /**
   * Reads a value written with {@link #writeRoundedDouble(SerializationStreamWriter, double, int)}.
   *
   * @param precision the number of retained digits after the decimal point; should be the same as the corresponding arg
   *   that was used to {@linkplain #writeRoundedDouble(SerializationStreamWriter, double, int) write the value}
   *
   */
  public static double readRoundedDouble(SerializationStreamReader reader, int precision) throws SerializationException {
    return doubleFromScaledInt(reader.readInt(), precision);
  }

  private static final int MAX_CACHED_POW_10 = 10;
  /**
   * Pre-computed results of {@code Math.pow(10, i)} for {@code i} &isin; [0,10).
   * <p>
   * Our benchmarks showed that this optimization doubles the throughput
   * of {@link #doubleToScaledInt(double, int)} (see {@code SerializationUtilsBenchmark2} in the test source tree).
   * @see #pow10(int)
   */
  private static final double[] powersOf10 = new double[MAX_CACHED_POW_10];

  static {
    for (int i = 0; i < powersOf10.length; i++) {
      powersOf10[i] = Math.pow(10, i);
    }
  }

  /**
   * @return <code>10<sup>n</sup></code>.
   */
  private static double pow10(int n) {
    return n < MAX_CACHED_POW_10 ? powersOf10[n] : Math.pow(10, n);
  }

  /**
   * Returns an integer representing the given {@code double} rounded to the given number of decimal places.
   * This method is used for implementing {@link #writeRoundedDouble(SerializationStreamWriter, double, int)}.
   * <p>
   * <em>Note:</em>
   * The rounding of the scaled decimal is done with {@link Math#round(double)}, which results in "half-down"
   * rounding for negative values.  For example: {@code Math.round(1.5) == 2},
   * but {@code Math.round(-1.5) == -1}.
   *
   *
   * @param value the original {@code double}; must be finite, not {@code NaN}, and
   *              <code>value * 10<sup>precision</sup></code> must fit into a 32-bit integer
   * @param precision the desired number of digits to retain after the decimal point (must be >= 0)
   * @return <code>value * 10<sup>precision</sup></code>, rounded to the nearest integer with {@link Math#round(double)}
   * @throws AssertionError if assertions are enabled and the {@code value} arg is either {@code NaN} or infinite,
   * the {@code precision} arg is negative, or the result doesn't fit into an {@code int}.
   * @see #writeRoundedDouble(SerializationStreamWriter, double, int)
   */
  public static int doubleToScaledInt(double value, int precision) {
    assert precision >= 0;
    assert !Double.isNaN(value) && Double.isFinite(value);
    double scaled;
    if (value == 0.0 || precision == 0) {
      scaled = value;  // fast path
    }
    else {
      scaled = value * pow10(precision);
      /* Assert that the scaled value won't overflow an int
         (Note: we could take the same approach as Math.round, and return Integer.MIN_VALUE or Integer.MAX_VALUE
          in this case, but that's probably not what the caller wanted).
       */
      assert scaled >= Integer.MIN_VALUE && scaled <= Integer.MAX_VALUE;
    }
    //
    return (int)Math.round(scaled);
    // TODO: maybe extract this method to MathUtils?
  }

  /**
   * Inverse of {@link #doubleToScaledInt(double, int)}.
   * Used as a helper for {@link #readRoundedDouble(SerializationStreamReader, int)}.
   *
   * @param scaledInt an integer produced by {@link #doubleToScaledInt(double, int)}
   * @param precision the number of retained digits after the decimal point; should be the same as the corresponding arg
   *   that was used with {@link #doubleToScaledInt(double, int)} to produce the given integer
   * @return <code>scaledInt * 10<sup>-precision</sup></code>
   */
  public static double doubleFromScaledInt(int scaledInt, int precision) {
    return (double)scaledInt / pow10(precision);
  }

  public static String serialize(Serializable obj, AbstractSerializationStreamWriter writer) throws SerializationException {
    writer.prepareToWrite();
    writer.writeObject(obj);
    return writer.toString();
  }

  @SuppressWarnings("unchecked")
  public static <T> T deserialize(String encoded, AbstractSerializationStreamReader reader) throws SerializationException {
    reader.prepareToRead(encoded);
    return (T)reader.readObject();
  }

}
