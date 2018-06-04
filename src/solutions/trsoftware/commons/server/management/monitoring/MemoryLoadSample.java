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

import solutions.trsoftware.commons.shared.util.MemoryUnit;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.*;

/**
 * A number of these will be remembered over time for historical display.
 * Instances of this class are immutable.
 */
public class MemoryLoadSample {
  private final MemoryUsage heap;
  private final SortedMap<String, MemoryUsage> pools = new TreeMap<>();

  public MemoryLoadSample() {
    MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    heap = memoryBean.getHeapMemoryUsage();
    List<MemoryPoolMXBean> poolBeans = ManagementFactory.getMemoryPoolMXBeans();
    for (MemoryPoolMXBean pool : poolBeans) {
      pools.put(pool.getName(), pool.getUsage());
    }
  }

  public MemoryUsage getHeapUsage() {
    return heap;
  }

  public MemoryUsage getPoolUsage(String poolName) {
    return pools.get(poolName);
  }

  public Set<String> getPoolNames() {
    return pools.keySet();
  }

  private static StringBuilder printPoolUsage(String name, MemoryUsage usage, StringBuilder buf) {
    buf.append(name).append("[");
    long init = usage.getInit();
    long used = usage.getUsed();
    long committed = usage.getCommitted();
    long max = usage.getMax();
    buf.append("init = ").append(init).append(' ').append(prettyPrintBytesUsed(init)).append(", ");
    buf.append("used = ").append(used).append(' ').append(prettyPrintBytesUsed(used)).append(", ");
    buf.append("committed = ").append(committed).append(' ').append(prettyPrintBytesUsed(committed)).append(", ");
    buf.append("max = ").append(max).append(' ').append(prettyPrintBytesUsed(max)).append(", ");
    return buf.append("]");
  }

  public static String prettyPrintBytesUsed(long bytes) {
    MemoryUnit unit = MemoryUnit.bestForHuman(bytes);
    return String.format("(%,.3f %s)", unit.fromBytes(bytes), unit.abbreviation);
  }


  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("MemoryLoadSample(");
    printPoolUsage("Heap", heap, sb);
    for (Map.Entry<String, MemoryUsage> poolEntry : pools.entrySet()) {
      sb.append(", ");
      printPoolUsage(poolEntry.getKey(), poolEntry.getValue(), sb);
    }
    sb.append(')');
    return sb.toString();
  }

  // main method for manual testing
  public static void main(String[] args) {
    System.out.println(new MemoryLoadSample());
  }
}