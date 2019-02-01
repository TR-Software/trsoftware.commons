package solutions.trsoftware.commons.server.management.monitoring;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * @author Alex
 * @since 1/14/2019
 */
public class MonitoringUtils {

  /**
   * Uses {@link com.sun.management.OperatingSystemMXBean} to get the "recent cpu usage" for the JVM process.
   * <p>
   * <em>CAUTION: this method may be slow (sometimes taking as much as 5 seconds before returning).</em>
   *
   * @return The "recent cpu usage" for the JVM process. This value is a double in the [0.0,1.0] interval. A value of
   *     0.0 means that none of the CPUs were running threads from the JVM process during the recent period of time
   *     observed, while a value of 1.0 means that all CPUs were actively running threads from the JVM 100% of the time
   *     during the recent period being observed. Threads from the JVM include the application threads as well as the
   *     JVM internal threads. All values betweens 0.0 and 1.0 are possible depending of the activities going on in the
   *     JVM process and the whole system. If the Java Virtual Machine recent CPU usage is not available, the method
   *     returns a negative value.
   * @see <a href="https://docs.oracle.com/javase/8/docs/jre/api/management/extension/com/sun/management/OperatingSystemMXBean.html#getProcessCpuLoad--">com.sun.management.OperatingSystemMXBean</a>
   */
  public static double getProcessCpuLoad() {
    // see https://docs.oracle.com/javase/8/docs/jre/api/management/extension/com/sun/management/OperatingSystemMXBean.html
    OperatingSystemMXBean osBean =
        (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
    return osBean.getProcessCpuLoad();
  }

  public static class Memory {


    /**
     * Equivalent to {@link MemoryUsage#getUsed()},
     * but uses {@link Runtime} instead of {@link MemoryMXBean}
     * @see Runtime#totalMemory()
     * @see Runtime#freeMemory()
     * @see <a href="https://stackoverflow.com/q/10509942/1965404">StackOverflow: Getting Memory Statistics in Java</a>
     */
    public static long getUsed() {
      Runtime runtime = Runtime.getRuntime();
      return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Equivalent to {@link MemoryUsage#getCommitted()},
     * but uses {@link Runtime} instead of {@link MemoryMXBean}
     * @see Runtime#totalMemory()
     * @see <a href="https://stackoverflow.com/q/10509942/1965404">StackOverflow: Getting Memory Statistics in Java</a>
     */
    public static long getCommitted() {
      return Runtime.getRuntime().totalMemory();
    }
  }

}
