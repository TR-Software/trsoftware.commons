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

package solutions.trsoftware.commons.server.management.monitoring;

import java.text.NumberFormat;

/**
 * Mar 29, 2011
 *
 * @author Alex
 */
public class IntegerStatType implements StatType {
  private final static NumberFormat intFormatter = NumberFormat.getIntegerInstance();
  private String name;

  public IntegerStatType(String name) {
    this.name = name;
  }

  public NumberFormat getPrintFormatter() {
    return intFormatter;
  }

  public String format(double value) {
    return intFormatter.format(value);
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }
}
