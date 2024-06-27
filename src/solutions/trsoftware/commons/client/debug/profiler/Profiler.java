package solutions.trsoftware.commons.client.debug.profiler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.core.shared.GWT;
import solutions.trsoftware.commons.client.jso.JsConsole;
import solutions.trsoftware.commons.client.jso.JsObject;
import solutions.trsoftware.commons.client.jso.JsPerformance;
import solutions.trsoftware.commons.client.util.Duration;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.stats.ImmutableStats;
import solutions.trsoftware.commons.shared.util.stats.NumberSampleOnlineDouble;
import solutions.trsoftware.commons.shared.util.text.SharedNumberFormat;

import javax.annotation.Nonnull;
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
//      instance = getProfiler(Debug.ENABLED);

      // replacement rules defined in solutions.trsoftware.commons.Debug (.gwt.xml) module
      // TODO: what if a module doesn't inherit Debug? GWT.create could throw an exception b/c Profiler is abstract
      instance = GWT.create(Profiler.class);
    }
    return instance;
  }

  @Nonnull
  @VisibleForTesting
  @Deprecated
  public static Profiler getProfiler(boolean enabled) {
    // TODO: clean up code smell in terms of exposing this method publicly and disambiguate with getInstance; write doc
    if (!enabled)
      return new DisabledProfiler();
    else if (performance != null)
      return new HiResProfiler();
    else
      return new LowResProfiler();
  }

  /**
   * @return a high-res timestamp in milliseconds, with microsecond precision
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Performance/now">Performance.now() on MDN</a>
   */
  public static native double now() /*-{
    return performance.now();
  }-*/;

  /**
   * The currently-running timers.
   */
//  protected final LinkedHashMap<String, Duration> currentTimers = new LinkedHashMap<>();

  // TODO(5/30/2024): experiment replace currentTimers Map with JsObject for speed
  protected final JsObject startTimes = JsObject.create();

  /**
   * Stats about completed timings.
   */
  protected final LinkedHashMap<String, NumberSampleOnlineDouble> stats = new LinkedHashMap<>();

  private boolean loggingEnabled;

  public void time(String name) {
    /* TODO(5/29/2024):
         - for faster perf, could store just the timestamps w/o instantiating a Duration, and use a JSO instead of Map
     */
//    currentTimers.computeIfAbsent(name, this::createTimer);
    startTimes.set(name, currentTime());  // TODO: this doesn't handle re-entrant calls; maybe use a stack (double[])?
    console.time(name);
//    console.profile(name);  // TODO: temp (console.profile is non-standard); also useless in GWT DevMode, due to obfuscated func names
  }

  public void timeEnd(String name) {
    console.timeEnd(name);
//    console.profileEnd(name);  // TODO: temp (console.profile is non-standard); also useless in GWT DevMode, due to obfuscated func names
    // TODO(5/30/2024): experiment replace currentTimers Map with JsObject for speed
//    Duration duration = currentTimers.remove(name);
//    if (duration != null) {
    if (startTimes.hasKey(name)) {
      double start = startTimes.getNumber(name);  // TODO: this doesn't handle re-entrant calls; maybe use a stack (double[])?
      startTimes.delete(name);  // TODO: deleting probably won't make any difference since value is overwritten on next time(name) call
      // record and log the elapsed time
//      double elapsed = duration.elapsedMillis();
      double elapsed = currentTime() - start;
      NumberSampleOnlineDouble sample = stats.computeIfAbsent(name, s -> new NumberSampleOnlineDouble());
      sample.update(elapsed);
      if (loggingEnabled) {
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

  public boolean isLoggingEnabled() {
    return loggingEnabled;
  }

  public void setLoggingEnabled(boolean loggingEnabled) {
    this.loggingEnabled = loggingEnabled;
  }

  // TODO: replace usages of Duration with a class similar to Guava Stopwatch

  protected abstract double currentTime();

  protected abstract Duration createTimer(String name);

  protected abstract String formatDuration(double millis);


  /**
   * Uses {@link Duration} for time measurement, which results in low accuracy.
   *
   * @see HiResProfiler
   * @deprecated should use {@link HiResProfiler} instead, since the Performance API has very good browser support,
   *     going as far back as Chrome 24, FF 15, and IE 10.
   */
  static class LowResProfiler extends Profiler {
    private static final SharedNumberFormat NUMBER_FORMAT = new SharedNumberFormat(0);

    @Override
    protected double currentTime() {
      return com.google.gwt.core.client.Duration.currentTimeMillis();
    }

    @Override
    protected Duration createTimer(String name) {
      return new Duration(name);
    }

    @Override
    protected String formatDuration(double millis) {
      return NUMBER_FORMAT.format(millis);
    }
  }

  /**
   * Uses the browser's {@link JsPerformance Performance API} for high-accuracy time measurement
   * @see HiResDuration
   */
  static class HiResProfiler extends Profiler {
    /* TODO(5/7/2024): make better use of the Performance API (use methods such as measure, mark, etc.)
        - see https://developer.mozilla.org/en-US/docs/Web/API/Performance/measure
     */

    private static final SharedNumberFormat NUMBER_FORMAT = new SharedNumberFormat(3);

    public HiResProfiler() {
      if (performance == null) {
        // Note: this is very unlikely, since the Performance API has very good browser support,
        // going as far back as Chrome 24, FF 15, and IE 10
        throw new IllegalStateException("The current browser doesn't support Performance.now()");
      }
    }

    @Override
    protected double currentTime() {
      return performance.now();
    }

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
    protected double currentTime() {
      throw new UnsupportedOperationException("DisabledProfiler.currentTime");
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
