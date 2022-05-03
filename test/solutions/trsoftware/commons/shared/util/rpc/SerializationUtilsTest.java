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

import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.user.client.rpc.SerializationException;
import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.testutil.MockSerializationStreamReader;
import solutions.trsoftware.commons.shared.testutil.MockSerializationStreamWriter;
import solutions.trsoftware.commons.shared.util.MathUtils;
import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.util.Arrays;
import java.util.Random;

import static solutions.trsoftware.commons.shared.util.rpc.SerializationUtils.*;

/**
 * @author Alex
 * @since 4/23/2022
 */
public class SerializationUtilsTest extends TestCase {

  public void testWriteRoundedDouble() throws Exception {
    MockSerializationStreamWriter writer = new MockSerializationStreamWriter();
    double original = 123.45678;
    testWriteRoundedDouble(writer, original, 3, "123457");
    testWriteRoundedDouble(writer, original, 2, "12346");
    testWriteRoundedDouble(writer, original, 1, "1235");
    testWriteRoundedDouble(writer, original, 0, "123");
    assertEquals(Arrays.asList("123457", "12346", "1235", "123"), writer.getTokenList());
    System.out.println("writer.toString() = " + writer.toString());
    MockSerializationStreamReader reader = new MockSerializationStreamReader(writer);
    testReadRoundedDouble(reader, 3, 123.457);
    testReadRoundedDouble(reader, 2, 123.46);
    testReadRoundedDouble(reader, 1, 123.5);
    testReadRoundedDouble(reader, 0, 123.0);
  }

  private void testWriteRoundedDouble(MockSerializationStreamWriter writer, double value, int precision, String expectedToken) throws SerializationException {
    writeRoundedDouble(writer, value, precision);
    String token = writer.getLastToken();
    System.out.println(StringUtils.methodCallToStringWithResult("writeRoundedDouble", token, value, precision));
    assertEquals(expectedToken, token);
  }

  private void testReadRoundedDouble(MockSerializationStreamReader reader, int precision, double expected) throws SerializationException {
    double actual = readRoundedDouble(reader, precision);
    System.out.println(StringUtils.methodCallToStringWithResult("readRoundedDouble", actual, precision));
//    assertEquals(expected, actual, Math.pow(10, -precision-1));
    assertEquals(expected, actual);
  }

  @GwtIncompatible @SuppressWarnings("NonJREEmulationClassesInClientCode")
  public void testDoubleToScaledInt() throws Exception {
    // review some basic examples manually
    {
      assertEquals(135, doubleToScaledInt(1.345, 2));
      assertEquals(-134, doubleToScaledInt(-1.345, 2));  // Math.round is "half-down" for negative values
      assertEquals(-135, -doubleToScaledInt(-(-1.345), 2));  // can force "half-up" by negating the input and output
    }
    // now test with some random doubles
    Random rnd = new Random(1);
    int n = 1000;
    double[] inputs = new double[n];
    for (int i = 0; i < inputs.length; i++) {
      inputs[i] = RandomUtils.nextDoubleInRange(rnd,0, 300);
    }
    int maxPrecision = 5;
    for (int precision = 0; precision <= maxPrecision; precision++) {
      for (double x : inputs) {
        int scaledInt = doubleToScaledInt(x, precision);
        double unscaled = doubleFromScaledInt(scaledInt, precision);
        System.out.printf("%2d %20s %10s     %s%n", precision, x, scaledInt, unscaled);
        assertEquals(MathUtils.round(x, precision), unscaled, Math.pow(10, -precision-1));
      }
    }
  }

  public void testWriteRoundedDoubleVersusWriteDouble() throws Exception {
    // just to see how much space can be saved, write the original as a normal double
    MockSerializationStreamWriter writer = new MockSerializationStreamWriter();
    for (double original : new double[]{28.48680753904732, 28.5}) {
      writer.writeDouble(original);
      String originalEncoding = writer.getLastToken();
      for (int i = 0; i < 5; i++) {
        writeRoundedDouble(writer, original, i);
        String result = writer.getLastToken();
        int savings = originalEncoding.length() - result.length();
        System.out.println(
            StringUtils.methodCallToStringWithResult("writeRoundedDouble", result, original, i)
            + " (" + (savings >= 0 ? "saved " : "wasted ") + Math.abs(savings) + " chars)"
        );
      }
    }
    System.out.println("writer.toString() = " + writer.toString());
  }
}