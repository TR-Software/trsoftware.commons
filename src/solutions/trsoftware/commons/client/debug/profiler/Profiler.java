package solutions.trsoftware.commons.client.debug.profiler;

import com.google.common.collect.ImmutableMap;
import com.google.gwt.core.shared.GWT;
import solutions.trsoftware.commons.client.debug.Debug;
import solutions.trsoftware.commons.client.jso.JsConsole;
import solutions.trsoftware.commons.client.jso.JsPerformance;
import solutions.trsoftware.commons.client.util.Duration;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.stats.ImmutableStats;
import solutions.trsoftware.commons.shared.util.stats.NumberSampleOnlineDouble;
import solutions.trsoftware.commons.shared.util.text.SharedNumberFormat;

import java.util.LinkedHashMap;

import static java.util.Objects.requireNonNull;

/**
 * Can be used to measure and log execution times of arbitrary code sections.
 *
 * @author Alex
 * @since 5/3/2023
 */
public abstract class Profiler {

  private static final JsPerformance performance = JsPerformance.implementsNow() ?
      requireNonNull(JsPerformance.get()) : null;

  private static final JsConsole console = JsConsole.get();


  private static Profiler instance;

  public static Profiler getInstance() {
    if (instance == null) {
      if (!Debug.ENABLED)
        instance = new DisabledProfiler();
      else if (performance != null)
        instance = new HiResProfiler();
      else
        instance = new LowResProfiler();
    }
    return instance;
  }

  /**
   * The currently-running timers.
   */
  protected final LinkedHashMap<String, Duration> currentTimers = new LinkedHashMap<>();
  /**
   * Stats about completed timings.
   */
  protected final LinkedHashMap<String, NumberSampleOnlineDouble> stats = new LinkedHashMap<>();


  /**
   * @return a high-res timestamp in milliseconds, with microsecond precision
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Performance/now">Performance.now() on MDN</a>
   */
  public static native double now() /*-{
    return performance.now();
  }-*/;


  public void time(String name) {
    currentTimers.computeIfAbsent(name, this::createTimer);
    console.time(name);
//    console.profile(name);  // TODO: temp (console.profile is non-standard); also useless in GWT DevMode
  }

  public void timeEnd(String name) {
    console.timeEnd(name);
//    console.profileEnd(name);  // TODO: temp (console.profile is non-standard); also useless in GWT DevMode
    Duration duration = currentTimers.remove(name);
    if (duration != null) {
      // record and log the elapsed time
      double elapsed = duration.elapsedMillis();
      NumberSampleOnlineDouble sample = stats.computeIfAbsent(name, s -> new NumberSampleOnlineDouble());
      sample.update(elapsed);
      String msg = name + " took " + formatDuration(elapsed) + "ms";
      {
        // print the stats summary for every 10 calls
        // TODO: make this configurable
        int nCalls = 10;
        int size = sample.size();
        if (size > 0 && size % nCalls == 0) {
          msg += "; ";
          msg = StringUtils.justifyLeft(msg, 40) + formatStats(sample);
        }
      }
      GWT.log(msg);
      // TODO: also log to console or JUL if logging is enabled
    }
  }

  protected String formatStats(NumberSampleOnlineDouble sample) {
    StringBuilder sb = new StringBuilder();
    sb.append("stats(").append(sample.size()).append("): ");
    sb.append("min=").append(formatDuration(sample.min())).append(", ");
    sb.append("max=").append(formatDuration(sample.max())).append(", ");
    sb.append("mean=").append(formatDuration(sample.mean()));
    return sb.toString();
  }

  public ImmutableMap<String, ImmutableStats<Double>> getStats() {
    ImmutableMap.Builder<String, ImmutableStats<Double>> builder = ImmutableMap.builder();
    stats.forEach((name, sample) ->
        builder.put(name, sample.summarize()));
    return builder.build();
  }

  // TODO: replace usages of Duration with a class similar to Guava Stopwatch

  protected abstract Duration createTimer(String name);

  protected abstract String formatDuration(double millis);


  static class LowResProfiler extends Profiler {
    private static final SharedNumberFormat NUMBER_FORMAT = new SharedNumberFormat(0);

    @Override
    protected Duration createTimer(String name) {
      return new Duration(name);
    }

    @Override
    protected String formatDuration(double millis) {
      return NUMBER_FORMAT.format(millis);
    }
  }

  static class HiResProfiler extends Profiler {
    private static final SharedNumberFormat NUMBER_FORMAT = new SharedNumberFormat(3);

    @Override
    protected Duration createTimer(String name) {
      return new HiResDuration(name);
    }

    @Override
    protected String formatDuration(double millis) {
      return NUMBER_FORMAT.format(millis);
    }
  }

  static class DisabledProfiler extends Profiler {

    @Override
    public void time(String name) {
      // no-op
    }

    @Override
    public void timeEnd(String name) {
      // no-op
    }

    @Override
    protected Duration createTimer(String name) {
      throw new UnsupportedOperationException("DisabledProfiler.createTimer");  // TODO(5/26/2023): exception not reported to UncaughtExceptionHandler in DevMode
    }

    @Override
    protected String formatDuration(double millis) {
      throw new UnsupportedOperationException("DisabledProfiler.formatDuration");
    }
  }

  /**
   * Uses the high-res timestamp returned by {@code performance.now()} instead of {@code Date.now()}
   */
  public static class HiResDuration extends Duration {
    public HiResDuration(String name) {
      super(name);
    }
    @Override
    public double currentTimeMillis() {
      return performance.now();
    }
  }

}
