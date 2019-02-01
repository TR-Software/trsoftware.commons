package solutions.trsoftware.commons.server.management.monitoring;

import com.google.common.collect.ImmutableMap;
import solutions.trsoftware.commons.server.io.StringPrintStream;
import solutions.trsoftware.commons.server.util.callables.DelegatedRunnable;
import solutions.trsoftware.commons.shared.util.MemoryUnit;
import solutions.trsoftware.commons.shared.util.stats.ImmutableStats;
import solutions.trsoftware.commons.shared.util.stats.NumberSampleOnlineDouble;
import solutions.trsoftware.commons.shared.util.stats.SampleStatistics;

import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static solutions.trsoftware.commons.server.management.monitoring.SystemLoadStatType.*;

/**
 * Provides in-process monitoring by periodically sampling CPU load and memory usage.
 *
 * @author Alex
 * @since 1/14/2019
 */
public class SystemLoadProfiler {
  // TODO: create our own enum type to avoid checking for membership in a subset of SystemLoadStatType
  private static final EnumSet<SystemLoadStatType> supportedStatTypes = EnumSet.of(HEAP_USED, HEAP_COMMITTED, CPU);


  private ScheduledThreadPoolExecutor executor;
  private Map<SystemLoadStatType, Entry> entries;
  private Map<SystemLoadStatType, ImmutableStats<Double>> summaries;

  private static class Entry {
    private SystemLoadStatType statType;
    private NumberSampleOnlineDouble sample;
    private ScheduledFuture<?> scheduledFuture;

    private Entry(SystemLoadStatType statType) {
      this.statType = statType;
      sample = new NumberSampleOnlineDouble();
    }

    private void update() {
      sample.update(statType.getCurrentValue());
    }
  }

  public SystemLoadProfiler() {
    this(supportedStatTypes.toArray(new SystemLoadStatType[supportedStatTypes.size()]));
  }

  public SystemLoadProfiler(SystemLoadStatType... statTypes) {
    entries = new EnumMap<>(SystemLoadStatType.class);
    for (SystemLoadStatType statType : statTypes) {
      if (supportedStatTypes.contains(statType))
        entries.put(statType, new Entry(statType));
      else
        throw new IllegalArgumentException(statType + " not supported");
    }
  }

  public synchronized void start(long period, TimeUnit unit) {
    start(period, period, unit);
  }

  public synchronized void start(long initialDelay, long period, TimeUnit unit) {
    if (executor != null)
      throw new IllegalStateException(toString() + " already started");
    executor = new ScheduledThreadPoolExecutor(entries.size()); // ensure enough threads to execute all updates concurrently
    for (SystemLoadStatType statType : entries.keySet()) {
      Entry entry = entries.get(statType);
      // schedule a separate task for each stat (because some stats may take longer to compute than others)
      entry.scheduledFuture = executor.scheduleAtFixedRate(
          new DelegatedRunnable(entry::update), initialDelay, period, unit);
    }
  }

  public synchronized Map<SystemLoadStatType, ImmutableStats<Double>> stop() {
    if (executor == null)
      throw new IllegalStateException(toString() + " not started");
    if (executor.isShutdown())
      throw new IllegalStateException(toString() + " already stopped");
    executor.shutdownNow();
    EnumMap<SystemLoadStatType, ImmutableStats<Double>> summaries = new EnumMap<>(SystemLoadStatType.class);
    for (SystemLoadStatType statType : entries.keySet()) {
      Entry entry = entries.get(statType);
      entry.scheduledFuture.cancel(true);
      entry.scheduledFuture = null;
      summaries.put(statType, entry.sample.summarize());
    }
    return this.summaries = ImmutableMap.copyOf(summaries);
  }


  public synchronized String printSummary() {
    assertSummaries();
    return printSummary(summaries);
  }

  public synchronized void printSummary(PrintStream out) {
    assertSummaries();
    printSummary(summaries, out);
  }

  private void assertSummaries() {
    if (summaries == null) {
      if (executor == null)
        throw new IllegalStateException(toString() + " not started");
      else
        throw new IllegalStateException(toString() + " not stopped");
    }
  }

  public static String printSummary(Map<SystemLoadStatType, ? extends SampleStatistics<Double>> stats) {
    StringPrintStream out = new StringPrintStream();
    printSummary(stats, out);
    return out.toString();
  }

  public static void printSummary(Map<SystemLoadStatType, ? extends SampleStatistics<Double>> stats, PrintStream out) {
    for (Map.Entry<SystemLoadStatType, ? extends SampleStatistics<Double>> entry : stats.entrySet()) {
      SystemLoadStatType statType = entry.getKey();
      out.printf("%15s: ", statType);
      SampleStatistics<Double> sample = entry.getValue();
      switch (statType) {
        case HEAP_USED:
        case HEAP_COMMITTED:
          printMemoryStats(out, sample);
          break;
        case CPU:
          printPercentageStats(out, sample);
          break;
        default:
          throw new IllegalStateException();
      }
    }
  }

  private static void printMemoryStats(PrintStream out, SampleStatistics<Double> sample) {
    MemoryUnit unit = MemoryUnit.MEGABYTES;
    out.printf("min=%,7.1f %s  ", unit.fromBytes(sample.min()), unit.abbreviation);
    out.printf("mean=%,7.1f %s  ", unit.fromBytes(sample.mean()), unit.abbreviation);
    out.printf("max=%,7.1f %s  ", unit.fromBytes(sample.max()), unit.abbreviation);
    out.printf("stdev=%,7.1f %s  ", unit.fromBytes(sample.stdev()), unit.abbreviation);
    out.printf("(%,d samples)", sample.size());
    out.println();
  }

  private static void printPercentageStats(PrintStream out, SampleStatistics<Double> sample) {
    NumberFormat pctFmt = NumberFormat.getPercentInstance();
    out.printf("min=%7s  ", pctFmt.format(sample.min()));
    out.printf("mean=%7s  ", pctFmt.format(sample.mean()));
    out.printf("max=%7s  ", pctFmt.format(sample.max()));
    out.printf("stdev=%7s  ", pctFmt.format(sample.stdev()));
    out.printf("(%,d samples)", sample.size());
    out.println();
  }

}
