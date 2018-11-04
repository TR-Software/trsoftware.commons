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

package solutions.trsoftware.commons.client.util.geometry;

import com.google.gwt.dom.client.Style;

/**
 * Immutable class encapsulating a dimension expressed in CSS units.
 *
 * @author Alex, 6/21/2016
 */
public class Size {

  private final double value;
  private final Style.Unit unit;

  public Size(double value, Style.Unit unit) {
    this.value = value;
    this.unit = unit;
  }

  public double getValue() {
    return value;
  }

  public Style.Unit getUnit() {
    return unit;
  }

  public Size scale(double factor) {
    return new Size(value * factor, unit);
  }

  @Override
  public String toString() {
    return String.valueOf(value) + unit.getType();
  }

  // factory methods:
  public static Size pct(double value) {
    return new Size(value, Style.Unit.PCT);
  }

  public static Size px(double value) {
    return new Size(value, Style.Unit.PX);
  }

}
