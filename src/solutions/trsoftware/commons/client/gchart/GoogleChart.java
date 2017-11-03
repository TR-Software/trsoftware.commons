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

package solutions.trsoftware.commons.client.gchart;

import com.google.gwt.user.client.ui.Image;
import solutions.trsoftware.commons.client.util.MapUtils;
import solutions.trsoftware.commons.client.util.WebUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides an abstraction for the Google Charts API.
 *
 * Provides custom setters for some of the complex parameters.
 * For all others, use the generic setParameter(key, value) method.
 *
 * @author Alex
 * @see <a href="https://developers.google.com/chart/image/">https://developers.google.com/chart/image/</a>
 */
public class GoogleChart {
  // Example: http://chart.apis.google.com/chart?cht=lc&chd=t:60,40,194,890&chds=0,1000&chs=500x300&chl=Oldest|Most%20Recent&chf=bg,s,FBFBFB

  private static final String BASE_URL = "http://chart.apis.google.com/chart?";

  // constants defining parameter names
  public static final String SIZE = "chs";
  public static final String TYPE = "cht";
  public static final String FILL = "chf";
  public static final String TITLE = "chtt";
  public static final String DATA_POINTS = "chd";
  public static final String DATA_SCALING = "chds";
  public static final String AXIS_LABEL_SPEC = "chxt";
  public static final String AXIS_LABEL_TEXTS = "chxl";
  public static final String AXIS_LABEL_POSITIONS = "chxp";
  public static final String AXIS_LABEL_RANGES = "chxr";

  private Map<String, String> parameters;

  /** The axis label parameters are stored separately and will be combined with the param map at the very end */
  private List<AxisLabel> axisLabels = new ArrayList<AxisLabel>();


  public GoogleChart() {
    // set some default values for parameters
    parameters = MapUtils.hashMap(
        TYPE, "lc",
        SIZE, "500x300"
    );
    // use a transparent background by default:
    setBackgroundTransparent();
  }

  public String generateUrl() {
    for (AxisLabel axisLabel : axisLabels) {
      axisLabel.appendToParamMap(parameters);
    }
    MapUtils.removeNullValues(parameters);
    return BASE_URL + WebUtils.urlQueryString(parameters).replaceAll(" ", "+").replaceAll("\n", "|");
  }

  public void applyToImage(Image img) {
    img.setUrl(generateUrl());
  }

  public String setParameter(String key, String value) {
    return parameters.put(key, value);
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  public void addAxisLabel(AxisLabel axisLabel) {
    axisLabel.setIndex(axisLabels.size());
    axisLabels.add(axisLabel);
  }

  public void setChartSize(int width, int height) {
    setParameter(SIZE, "" + width + "x" + height);
  }

  /**
   * Sets the fill type to a solid background having the given color, expressed as 6-digit hex {@code RRGGBB} string
   * or an 8-digit {@code RRGGBBAA}, where the last 2 hex digit give the alpha value ({@code 00} means fully transparent,
   * {@code FF} means fully opaque, and values in-between give partial transparency to the background color.
   */
  public void setBackgroundFill(String hexRGBA) {
    int n = hexRGBA.length();
    assert n == 6 || n == 8;
    setParameter(FILL, "bg,s," + hexRGBA);
  }

  /**
   * Makes the background fully transparent.
   */
  public void setBackgroundTransparent() {
    setBackgroundFill("00000000");
  }

  /**
   * Utility method:
   * Returns at most n values, each giving an average of consecutive points in the input.
   */
  public static int[] averagedSample(int[] input, int n) {
    int windowSize = (int)Math.ceil((double)input.length/n);
    int[] output = new int[input.length/windowSize];

    /// example:
    //  i:       0  1  2  3  4  5
    //  input:  [1][1][2][2][3][3]
    //  output: [1][2][3]
    for (int i = 0; i < output.length; i++) {
      double windowSum = 0;
      int windowStart = i*windowSize;
      int windowEnd = Math.min((i+1)*windowSize, input.length);
      for (int j = windowStart; j < windowEnd; j++) {
        windowSum += input[j];
      }
      output[i] = Math.round((float)(windowSum / (windowEnd - windowStart)));
    }
    return output;
  }


  private static final String gChartExtendedCipher = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-.";

  /**
   * Takes a value in range 0..1 and writes the closest value 0..4095 to the buffer,
   * encoded using Google Charts extended data encoding.
   */
  public static String extendedEncodePercentage(double pct) {
    return extendedEncode((int)(pct * 4095));  // coerce the percentage into range 0-4095
  }

  /**
   * Takes a value in range 0..4095 and returns it's representation
   * encoded using Google Charts extended data encoding.
   */
  public static String extendedEncode(int value) {
    char first = gChartExtendedCipher.charAt(value / 64);
    char second = gChartExtendedCipher.charAt(value % 64);
    return new String(new char[] {first, second});
  }
}