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

package solutions.trsoftware.commons.shared.util.formatter;  // copied from java.util

/**
 * Unchecked exception thrown when a conversion and flag are incompatible.
 *
 * <p> Unless otherwise specified, passing a <tt>null</tt> argument to any
 * method or constructor in this class will cause a {@link
 * NullPointerException} to be thrown.
 *
 * @since 1.5
 */
public class FormatFlagsConversionMismatchException
    extends IllegalFormatException
{
    private static final long serialVersionUID = 19120414L;

    private String f;

    private char c;

    /**
     * Constructs an instance of this class with the specified flag
     * and conversion.
     *
     * @param  f
     *         The flag
     *
     * @param  c
     *         The conversion
     */
    public FormatFlagsConversionMismatchException(String f, char c) {
        if (f == null)
            throw new NullPointerException();
        this.f = f;
        this.c = c;
    }

    /**
     * Returns the incompatible flag.
     *
     * @return  The flag
     */
     public String getFlags() {
        return f;
    }

    /**
     * Returns the incompatible conversion.
     *
     * @return  The conversion
     */
    public char getConversion() {
        return c;
    }

    public String getMessage() {
        return "Conversion = " + c + ", Flags = " + f;
    }
}
