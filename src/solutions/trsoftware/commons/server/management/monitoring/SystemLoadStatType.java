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

package solutions.trsoftware.commons.server.management.monitoring;

import java.text.NumberFormat;

/**
 * Mar 26, 2011
*
* @author Alex
*/
public enum SystemLoadStatType implements StatType {
  REQUEST_COUNT(NumberFormat.getIntegerInstance(), "RecentRequestCount"),
  CPU(NumberFormat.getPercentInstance(), "JVM CPU Load", 1) {
    @Override
    public double getCurrentValue() {
      return MonitoringUtils.getProcessCpuLoad();
    }
  },
  SYS_LOAD_AVG(NumberFormat.getNumberInstance(), "SystemLoadAverage (1 minute)"),
  HEAP_USED(null, "Heap Used") {
    @Override
    public double getCurrentValue() {
      return MonitoringUtils.Memory.getUsed();
    }
    @Override
    public String format(double value) {
      return String.format("%.2f MB", value);
    }
  },
  HEAP_COMMITTED(null, "Heap Committed") {
    @Override
    public double getCurrentValue() {
      return MonitoringUtils.Memory.getCommitted();
    }
    @Override
    public String format(double value) {
      return String.format("%.2f MB", value);
    }
  },
  HEAP_TENURED_GEN(null, "Tenured Gen Usage (after last GC)") {
    @Override
    public String format(double value) {
      return String.format("%.2f MB", value);
    }
  };

  private NumberFormat printFormatter;
  private final String prettyName;

  SystemLoadStatType(NumberFormat printFormatter, String prettyName) {
    this.printFormatter = printFormatter;
    this.prettyName = prettyName;
  }

  SystemLoadStatType(NumberFormat printFormatter, String prettyName, int maxFractionalDigits) {
    this(printFormatter, prettyName);
    printFormatter.setMaximumFractionDigits(maxFractionalDigits);
  }

  /**
   * Parses the given string into one of the enum constants; allows the name
   * to be specified as any valid prefix, e.g. "r" -> REQUEST_COUNT
   */
  public static SystemLoadStatType parseName(String str) {
    for (SystemLoadStatType stat : values()) {
      if (stat.name().startsWith(str.toUpperCase()))
        return stat;
    }
    return null;
  }

  public NumberFormat getPrintFormatter() {
    return printFormatter;
  }

  public String getName() {
    return prettyName;
  }

  public String format(double value) {
    return printFormatter.format(value);
  }


  @Override
  public String toString() {
    return getName();
  }

  public double getCurrentValue() {
    throw new UnsupportedOperationException();
  }
}
