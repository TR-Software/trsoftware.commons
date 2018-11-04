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

package solutions.trsoftware.commons.shared.util;

/**
 * Formats a time delta (in millis) as {@code "[MINUTES]:[SECONDS]"}
 *
 * @author Alex, 8/3/2017
 */
public class TimeFormatter {

  private static int MILLIS_IN_MINUTE = 60000;

  private boolean showFractionOfSecond;

  public TimeFormatter(boolean showFractionOfSecond) {
    this.showFractionOfSecond = showFractionOfSecond;
  }

  public String format(int millis) {
    int minutes = millis / MILLIS_IN_MINUTE;
    millis -= (minutes * MILLIS_IN_MINUTE);
    float secondsFractional = millis / 1000f;
    int seconds;
    if (!showFractionOfSecond)
      seconds = Math.round(secondsFractional);
    else
      seconds = (int)secondsFractional;
    millis -= (seconds * 1000);
    String ret = "";
    if (minutes < 10)
      ret += '0';  // leading zero
    ret += minutes + ":";
    if (seconds < 10)
      ret += '0';  // leading zero
    ret += seconds;
    if (showFractionOfSecond)
      ret += "." + millis;
    return ret;
  }

}
