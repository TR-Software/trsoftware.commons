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

package solutions.trsoftware.commons.shared.util.text;

/**
 * A bare-bones implementation of {@link java.util.Formatter} compatible with GWT.
 * <p>
 * For now, this class is just a placeholder for a potential future implementation.
 *
 * <p style="color: #0073BF; font-weight: bold;">
 *   TODO: either finish this project or get rid of this class
 * </p>
 *
 * @author Alex
 * @since 8/10/2019
 */
public class Formatter {

  /*
  java.util.Formatter spec:

  %[argument_index$][flags][width][.precision]conversion
    - argument_index: is a decimal integer indicating the position of the argument in the argument list.
      (the first argument is referenced by "1$", the second by "2$", etc.)
    - flags: a set of characters that modify the output format. The set of valid flags depends on the conversion.
      '-': The result will be left-justified (supported by all arg types)
           Spaces will be added at the end of the converted value as required to fill the minimum width of the field. If the width is not provided, then a MissingFormatWidthException will be thrown.
           If this flag is not given then the output will be right-justified.
      '#': The result should use a conversion-dependent alternate form (floats: any conversion; ints: only 'o', 'x', and 'X' conversions)
      '+': The result will always include a sign (floats: any conversion; ints: only 'd' if primitive/wrapper, or 'o', 'x', and 'X' if BigInteger)
      ' ': The result will include a leading space for positive values (same conversion restrictions as '+' flag)
      '0': The result will be zero-padded (all floats and ints)
      ',': The result will include locale-specific grouping separators (ints: only 'd'; floats: only 'e', 'E', 'f', 'g', and 'G')
      '(': The result will enclose negative numbers in parentheses (ints: same as '+' flag; floats: same as '+' flag)
    - width: a positive decimal integer indicating the minimum number of characters to be written to the output (not applicable for the line separator conversion)
      If the length of the converted value is less than the width then the output will be padded by spaces until the total number of characters equals the width.
      The padding is on the left by default. If the '-' flag is given, then the padding will be on the right. If the width is not specified then there is no minimum.
    - precision: a non-negative decimal integer usually used to restrict the number of characters. The specific behavior depends on the conversion.
    - conversion: a character indicating how the argument should be formatted. The set of valid conversions for a given argument depends on the argument's data type.
      (upper-case character means that result will be upper-cased with String.toUpperCase)
      - General conversions (may be applied to any argument type); all support the '-' and '#' flags
        'b':  Produces either "true" or "false" as returned by Boolean.toString(boolean).
                If the argument is null, then the result is "false". If the argument is a boolean or Boolean, then the result is the string returned by String.valueOf(). Otherwise, the result is "true".
                If the '#' flag is given, then a FormatFlagsConversionMismatchException will be thrown.
        'h':  Produces a string representing the hash code value of the object.
                If the argument, arg is null, then the result is "null". Otherwise, the result is obtained by invoking Integer.toHexString(arg.hashCode()).
                If the '#' flag is given, then a FormatFlagsConversionMismatchException will be thrown.
        's':  Produces a string.
                If the argument is null, then the result is "null". If the argument implements Formattable, then its formatTo method is invoked. Otherwise, the result is obtained by invoking the argument's toString() method.
                If the '#' flag is given and the argument is not a Formattable , then a FormatFlagsConversionMismatchException will be thrown.
      - Character conversion (only char/Character types); supports the '-' flag and width is defined as for General conversions
        'c':  Formats the argument as a Unicode character as described in Unicode Character Representation. This may be more than one 16-bit char in the case where the argument represents a supplementary character.
                If the '#' flag is given, then a FormatFlagsConversionMismatchException will be thrown.
                The precision is not applicable. If the precision is specified then an IllegalFormatPrecisionException will be thrown.
      - Numeric conversions:
        Byte, Short, Integer, Long and their primitive equivalents (for BigInteger see https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html#dnbint)
        'd':  Formats the argument as a decimal integer. The localization algorithm is applied.
                If the '0' flag is given and the value is negative, then the zero padding will occur after the sign.
                If the '#' flag is given then a FormatFlagsConversionMismatchException will be thrown.
        'o':  Formats the argument as an integer in base eight. No localization is applied.
                If x is negative then the result will be an unsigned value generated by adding 2n to the value where n is the number of bits in the type as returned by the static SIZE field in the Byte, Short, Integer, or Long classes as appropriate.
                If the '#' flag is given then the output will always begin with the radix indicator '0'.
                If the '0' flag is given then the output will be padded with leading zeros to the field width following any indication of sign.
                If '(', '+', '  ', or ',' flags are given then a FormatFlagsConversionMismatchException will be thrown.
        'x':  Formats the argument as an integer in base sixteen. No localization is applied.
              If x is negative then the result will be an unsigned value generated by adding 2n to the value where n is the number of bits in the type as returned by the static SIZE field in the Byte, Short, Integer, or Long classes as appropriate.
              If the '#' flag is given then the output will always begin with the radix indicator "0x".
              If the '0' flag is given then the output will be padded to the field width with leading zeros after the radix indicator or sign (if present).
              If '(', '  ', '+', or ',' flags are given then a FormatFlagsConversionMismatchException will be thrown.
        Float and Double (see complete spec at https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html#dndec)
        'e':  Requires the output to be formatted using computerized scientific notation. The localization algorithm is applied.
        'g':  Requires the output to be formatted in general scientific notation as described below. The localization algorithm is applied.
        'f':  Requires the output to be formatted using decimal format. The localization algorithm is applied.
                The result is a string that represents the sign and magnitude (absolute value) of the argument. The formatting of the sign is described in the localization algorithm. The formatting of the magnitude m depends upon its value.
                If m NaN or infinite, the literal strings "NaN" or "Infinity", respectively, will be output. These values are not localized.
                The magnitude is formatted as the integer part of m, with no leading zeroes, followed by the decimal separator followed by one or more decimal digits representing the fractional part of m.
                The number of digits in the result for the fractional part of m or a is equal to the precision. If the precision is not specified then the default value is 6. If the precision is less than the number of digits which would appear after the decimal point in the string returned by Float.toString(float) or Double.toString(double) respectively, then the value will be rounded using the round half up algorithm. Otherwise, zeros may be appended to reach the precision. For a canonical representation of the value, use Float.toString(float) or Double.toString(double) as appropriate.
        'a':  Requires the output to be formatted in hexadecimal exponential form. No localization is applied.
         * for all of the above:
                All flags defined for Byte, Short, Integer, and Long apply.
                If the '#' flag is given, then the decimal separator will always be present.
                If no flags are given the default formatting is as follows:

                  The output is right-justified within the width
                  Negative numbers begin with a '-'
                  Positive numbers and positive zero do not include a sign or extra leading space
                  No grouping separators are included
                  The decimal separator will only appear if a digit follows it
                The width is the minimum number of characters to be written to the output. This includes any signs, digits, grouping separators, decimal separators, exponential symbol, radix indicator, parentheses, and strings representing infinity and NaN as applicable. If the length of the converted value is less than the width then the output will be padded by spaces ('\u0020') until the total number of characters equals width. The padding is on the left by default. If the '-' flag is given then the padding will be on the right. If width is not specified then there is no minimum.
                If the conversion is 'e', 'E' or 'f', then the precision is the number of digits after the decimal separator. If the precision is not specified, then it is assumed to be 6.
                If the conversion is 'g' or 'G', then the precision is the total number of significant digits in the resulting magnitude after rounding. If the precision is not specified, then the default value is 6. If the precision is 0, then it is taken to be 1.
                If the conversion is 'a' or 'A', then the precision is the number of hexadecimal digits after the radix point. If the precision is not provided, then all of the digits as returned by Double.toHexString(double) will be output.
      - Percent:
        '%':  The result is a literal '%' ('\u0025')
                The width is the minimum number of characters to be written to the output including the '%'. If the length of the converted value is less than the width then the output will be padded by spaces ('\u0020') until the total number of characters equals width. The padding is on the left. If width is not specified then just the '%' is output.
                The '-' flag defined for General conversions applies. If any other flags are provided, then a FormatFlagsConversionMismatchException will be thrown.
                The precision is not applicable. If the precision is specified an IllegalFormatPrecisionException will be thrown.
      - Line Separator (does not correspond to any argument):
        'n':  The platform-specific line separator as returned by System.getProperty("line.separator").
                Flags, width, and precision are not applicable. If any are provided an IllegalFormatFlagsException, IllegalFormatWidthException, and IllegalFormatPrecisionException, respectively will be thrown.
  */

}
