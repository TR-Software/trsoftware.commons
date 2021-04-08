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

package solutions.trsoftware.commons.server.memquery;

import solutions.trsoftware.commons.server.memquery.aggregations.Aggregation;

/**
 * Defines the function, source attribute name, and renamed attribute name for each aggregation parameter.
 * @author Alex, 1/15/14
 */
public class AggregationSpec implements HasName {

  private final Class<? extends Aggregation> type;
  private final String inputAttrName;
  private final String outputAttrName;

  public AggregationSpec(Class<? extends Aggregation> type, String inputAttrName, String outputAttrName) {
    this.type = type;
    this.inputAttrName = inputAttrName;
    this.outputAttrName = outputAttrName;
  }

  public AggregationSpec(Class<? extends Aggregation> type, String inputAttrName) {
    this(type, inputAttrName, null);
  }

  public Class<? extends Aggregation> getType() {
    return type;
  }

  public String getInputAttrName() {
    return inputAttrName;
  }

  public String getOutputAttrName() {
    return outputAttrName;
  }

  public String getName() {
    return outputAttrName != null ?
        outputAttrName
        : toString();
  }

  @Override
  public String toString() {
    return String.format("%s(%s)", type.getSimpleName(), inputAttrName);
  }
}
