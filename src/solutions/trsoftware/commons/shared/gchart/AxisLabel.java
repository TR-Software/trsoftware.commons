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

import java.util.Map;

/**
 * Date: Jun 22, 2008 Time: 7:21:52 PM
*
* @author Alex
*/
abstract class AxisLabel {
  /** One of x,y,r,t */
  protected String name;
  protected int index;
  protected String style;

  protected AxisLabel(String name, String style) {
    assert name != null;
    this.name = name;
    this.style = style;
  }

  public final void appendToParamMap(Map<String, String> paramMap) {
    // name
    appendAxisParameter(paramMap, GoogleChart.AXIS_LABEL_SPEC, name, null, ",");
    if (style != null)
      appendAxisParameter(paramMap, GoogleChart.AXIS_LABEL_SPEC, style, ",", ",");
    appendToParamMapImpl(paramMap);
  }

  protected abstract void appendToParamMapImpl(Map<String, String> paramMap);


  /** Appends the given parameter value to the existing value for the parameter */
  protected final void appendAxisParameter(Map<String, String> paramMap, String paramName, String value, String indexSeparator, String axisSeparator) {
    String oldValue = paramMap.get(paramName);
    String newValue;
    if (oldValue == null)
      newValue = "";
    else
      newValue = oldValue + axisSeparator;
    if (indexSeparator != null)
      newValue += index + indexSeparator;
    newValue += value;
    paramMap.put(paramName, newValue);
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public int getIndex() {
    return index;
  }
}
