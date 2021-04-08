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

package solutions.trsoftware.commons.shared.gchart;

import java.util.Map;

/**
 * Date: Jun 22, 2008 Time: 7:22:10 PM
*
* @author Alex
*/
public class RangeAxisLabel extends AxisLabel {
  protected int[] range;

  public RangeAxisLabel(String name, String style, int[] range) {
    super(name, style);
    assert range != null;
    assert range.length == 2;
    this.range = range;
  }

  /** Appends parameter parts for this axis to the overall parameter map */
  @Override
  protected void appendToParamMapImpl(Map<String, String> paramMap) {
    appendAxisParameter(paramMap, GoogleChart.AXIS_LABEL_RANGES, range[0] + "," + range[1], ",", "|");
  }
}
