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

package solutions.trsoftware.commons.shared.gchart;

import solutions.trsoftware.commons.shared.util.ArrayUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.util.Map;

/**
 * Date: Jun 22, 2008 Time: 7:23:15 PM
 *
 * @author Alex
 */
public class ManualAxisLabel extends AxisLabel {
  protected String[] labels;
  protected int[] labelPositions;

  /** Labels provided by caller */
  public ManualAxisLabel(String name, String style, String[] labels, int[] labelPositions) {
    super(name, style);
    assert labels != null;
    assert labelPositions == null || labelPositions.length == labels.length;
    this.labels = labels;
    this.labelPositions = labelPositions;
  }

  /** Calculates range labels manually */
  public ManualAxisLabel(String name, String style, int min, int max, int maxTicks) {
    this(name, style, min, max, maxTicks, (max-min+1)/maxTicks);
  }
  private ManualAxisLabel(String name, String style, int min, int max, int maxTicks, int roundingMultiple) {
    super(name, style);
    int range = max - min;
    int ticks = Math.min(maxTicks, range+1);
    if (ticks == range+1)
      roundingMultiple = 1;  // not enough ticks to justify going by more than 1
    int minRounded = roundUpToMultiple(min, roundingMultiple);
    int maxRounded = roundDownToMultiple(max, roundingMultiple);
    int rangeRounded = maxRounded - minRounded;
//    int step = (int)Math.round((double)rangeRounded / (ticks - 1));
    int step = roundToMultiple((double)rangeRounded / (ticks - 1), roundingMultiple);
    ticks = rangeRounded / step + 1;
    labels = new String[ticks];
    labelPositions = new int[ticks];
    for (int i = 0; i < ticks; i++) {
      int tickValue = minRounded + step * i;
      labels[i] = "" + tickValue;
      // a label position is a percentage of the axis length
      labelPositions[i] = Math.round((float)(100d * (double)(tickValue-min) / range));
    }
  }

  private int roundToMultiple(double number, int multiple) {
    int roundedUp = roundUpToMultiple(number, multiple);
    int roundedDown = roundDownToMultiple(number, multiple);
    double distanceUp = Math.abs(number - roundedUp);
    double distanceDown = Math.abs(number - roundedDown);  
    if (distanceUp <= distanceDown)
      return roundedUp;
    return roundedDown;
  }

  private int roundDownToMultiple(double number, int multiple) {
    int rounded = (int)Math.floor(number);
    for (int i = 0; i < multiple; i++) {
      if ((rounded - i) % multiple == 0)
        return rounded - i;
    }
    assert false;  // this statement should never be reached
    return 0;
  }

  private int roundUpToMultiple(double number, int multiple) {
    int rounded = (int)Math.ceil(number);
    for (int i = 0; i < multiple; i++) {
      if ((rounded + i) % multiple == 0)
        return rounded + i;
    }
    assert false;  // this statement should never be reached
    return 0;
  }

  @Override
  protected void appendToParamMapImpl(Map<String, String> paramMap) {
    appendAxisParameter(paramMap, GoogleChart.AXIS_LABEL_TEXTS, StringUtils.join("|", labels), ":|", "|");
    appendAxisParameter(paramMap, GoogleChart.AXIS_LABEL_POSITIONS, ArrayUtils.toString(labelPositions, ","), ",", "|");
  }
}
