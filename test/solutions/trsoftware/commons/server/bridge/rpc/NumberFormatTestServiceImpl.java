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

package solutions.trsoftware.commons.server.bridge.rpc;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import solutions.trsoftware.commons.client.bridge.rpc.NumberFormatTestService;
import solutions.trsoftware.commons.client.bridge.text.AbstractNumberFormatter;
import solutions.trsoftware.commons.client.bridge.text.NumberFormatter;
import solutions.trsoftware.commons.server.bridge.text.NumberFormatterJavaImpl;

import java.util.Random;

/**
 * Allows comparing the output of GWT's NumberFormat to that of
 * java.text.NumberFormat by running the former client-side
 * and the latter server-side.
 *
 * This class formats numbers using the Java implentation of NumberFormat.
 *
 * @author Alex
 */
public class NumberFormatTestServiceImpl extends RemoteServiceServlet implements NumberFormatTestService {

  /**
   * Returns the output of invoking java.text.NumberFormat with the
   * given parameters.
   * @return The number formatted with java.text.NumberFormat using the given
   * tuning parameters.
   */
  public String formatNumber(double number, int minIntegerDigits, int minFractionalDigits, int maxFractionalDigits, boolean digitGrouping) {
    NumberFormatter formatter = AbstractNumberFormatter.getInstance(minIntegerDigits, minFractionalDigits, maxFractionalDigits, digitGrouping, false);

    // verify that we're using the right implementation
    if (!(formatter instanceof NumberFormatterJavaImpl))
      throw new IllegalStateException("Incorrect NumberFormatter implementation found.");

    return formatter.format(number);
  }

  /**
   * A batch version of formatNumber.
   */
  public String[] formatNumber(double[] number, int[] minIntegerDigits, int[] minFractionalDigits, int[] maxFractionalDigits, boolean[] digitGrouping) {
    String[] results = new String[number.length];
    for (int i = 0; i < results.length; i++) {
      results[i] = formatNumber(number[i], minIntegerDigits[i], minFractionalDigits[i], maxFractionalDigits[i], digitGrouping[i]);
    }
    return results;
  }

  // the following methods check float serialization
  public String[] generateFloats(int n) {
    Random rnd = new Random();
    String[] ret = new String[n];
    float[] floats = new float[n];
    for (int i = 0; i < n; i++) {
      float f = rnd.nextFloat();
      floats[i] = f;
      ret[i] = String.valueOf(f);
    }
    getThreadLocalRequest().getSession().setAttribute("floats", floats);
    return ret;
  }

  public boolean checkFloats(String[] clientStrings) {
    boolean allMatch = false;
    float[] floats = (float[])getThreadLocalRequest().getSession().getAttribute("floats");
    for (int i = 0; i < clientStrings.length; i++) {
      String str = clientStrings[i];
      float f = floats[i];
      System.out.println("Checking " + str);
      boolean match = f == Float.parseFloat(str);
      if (!match)
        System.err.println("" +f+ " doesn't match " + str);
      allMatch |= match;
    }
    return allMatch;
  }


}
