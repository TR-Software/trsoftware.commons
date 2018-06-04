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

import com.sun.management.OperatingSystemMXBean;
import solutions.trsoftware.commons.shared.util.MemoryUnit;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A snapshot of stats representing the "system load" at a particular instance in time.
 * A number of these will be remembered over time for historical display.
 * Instances of this class are immutable.
 */
public class SystemLoadSample implements DataSample {

  /** The value of {@link System#currentTimeMillis()} when this snapshot was created */
  protected final long time;
  /** The number of recent incoming HTTP requests */
  protected short servletRequestCount;
  /** Percentage of CPU cycles used (as a fraction 0..1) */
  protected final float cpu;
  /** Unix system load average for 1 minute */
  protected final float sysLoad;
  protected final float heapUsedMB;
  protected final float heapCommittedMB;
  /** Space used for the tenured memory pool after the last GC, which approximates the size of the objects that cannot be collected (useful for memory leak analysis) */
  protected final float tenuredGenMB;


  /** Creates a new data from various platform MBeans */
  public SystemLoadSample() {
    // compute the data
    OperatingSystemMXBean osBean =
        (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
    RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
    MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    List<MemoryPoolMXBean> poolBeans = ManagementFactory.getMemoryPoolMXBeans();
    this.time = System.currentTimeMillis();
    this.sysLoad = (float)osBean.getSystemLoadAverage();
    int nCPUs = osBean.getAvailableProcessors();
    long upTime = runtimeBean.getUptime();
    long prevUpTime = sPrevUpTime.compareAndSet(0, upTime) ? upTime : sPrevUpTime.getAndSet(upTime);
    long processCpuTime = osBean.getProcessCpuTime();
    long prevProcessCpuTime = sProcessCpuTime.compareAndSet(0, processCpuTime) ? processCpuTime : sProcessCpuTime.getAndSet(processCpuTime);
    // this code for calculating the % CPU usage of the JVM is taken from jconsole
    // (see http://forum.java.sun.com/thread.jspa?threadID=5305095)
    long elapsedCpu = processCpuTime - prevProcessCpuTime;
    long elapsedTime = upTime - prevUpTime;
    // cpuUsage could go higher than 100% because elapsedTime
    // and elapsedCpu are not fetched simultaneously. Limit to 100%
    this.cpu = (float)Math.min(1d, (double)elapsedCpu / (elapsedTime * 1000000d * nCPUs));
    this.heapUsedMB = bytesToMB(memoryBean.getHeapMemoryUsage().getUsed());
    this.heapCommittedMB = bytesToMB(memoryBean.getHeapMemoryUsage().getCommitted());

    long tenuredGenUsageAfterLastGC = 0;
    for (MemoryPoolMXBean pool : poolBeans) {
      if (pool.getName().equals("Tenured Gen")) {
        tenuredGenUsageAfterLastGC = pool.getCollectionUsage().getUsed();
      }
    }
    this.tenuredGenMB = bytesToMB(tenuredGenUsageAfterLastGC);

  }

  public float getCpu() {
    return cpu;
  }
  // TODO: cont here: make this method abstract and implement in TyperacerSystemLoadSample

  public short getServletRequestCount() {
    return servletRequestCount;
  }

  public float getSysLoad() {
    return sysLoad;
  }

  public long getTime() {
    return time;
  }

  public String getName() {
    return "System Load";
  }

  public float getHeapUsedMB() {
    return heapUsedMB;
  }

  public float getHeapCommittedMB() {
    return heapCommittedMB;
  }

  public float getTenuredGenMB() {
    return tenuredGenMB;
  }

  public Number getByStatType(StatType statType) {
    SystemLoadStatType systemLoadStatType = (SystemLoadStatType)statType;
    switch (systemLoadStatType) {
      case REQUEST_COUNT:
        return getServletRequestCount();
      case SYS_LOAD_AVG:
        return getSysLoad();
      case CPU:
        return getCpu();
      case HEAP_USED:
        return getHeapUsedMB();
      case HEAP_COMMITTED:
        return getHeapCommittedMB();
      case HEAP_TENURED_GEN:
        return getTenuredGenMB();
      default:
        throw new IllegalArgumentException();
    }
  }

  public StatType[] getStatTypes() {
    return SystemLoadStatType.values();
  }
  // these values of the application uptime will be used to compute CPU load
  private static final AtomicLong sPrevUpTime = new AtomicLong();

  private static final AtomicLong sProcessCpuTime = new AtomicLong();

  public static float bytesToMB(long bytes) {
    return (float)MemoryUnit.MEGABYTES.fromBytes(bytes);
  }

}
