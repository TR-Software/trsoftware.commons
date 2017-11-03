package solutions.trsoftware.commons.server.management.monitoring;

import solutions.trsoftware.commons.server.servlet.filters.RequestStatsFilter;
import solutions.trsoftware.commons.server.stats.HierarchicalCounter;
import solutions.trsoftware.commons.server.stats.SimpleCounter;
import solutions.trsoftware.commons.server.util.Duration;

import java.util.Date;

/**
 * Will be used by {@link RequestStatsFilter} to count the number of incoming requests by URL.
 *
 * TODO: start using this class in {@link RequestStatsFilter}
 *
 * @author Alex, 11/1/2017
 */
public class RequestStats {

  /** The "all-time" request counts, by URI */
  private HierarchicalCounter requestCounts = new HierarchicalCounter(new SimpleCounter("RequestCounts"), null);

  /** Represents stats for a limited time window */
  public static class Sample {
    /** The start time of the window, in millis */
    private long startTime;
    /** The end time of the window, in millis */
    private long endTime;
    /** The length of the window, in millis */
    private long duration;

    private HierarchicalCounter requestCounts;

    public Sample(long startTime, long duration) {
      this.startTime = startTime;
      this.duration = duration;
      this.endTime = startTime + duration;
      String name = getClass().getSimpleName() + "[" + new Date(startTime).toString()
          + " + " + Duration.formatAsClockTime(duration, false) + "]";
      requestCounts = new HierarchicalCounter(new SimpleCounter(name + " RequestCounts"), null);
    }
  }

}
