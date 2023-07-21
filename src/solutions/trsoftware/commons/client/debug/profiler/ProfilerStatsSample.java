package solutions.trsoftware.commons.client.debug.profiler;

import solutions.trsoftware.commons.shared.util.stats.ImmutableStats;

import java.util.Map;

/**
 * @author Alex
 * @since 5/8/2023
 */
public class ProfilerStatsSample {
  private final String name;
  private final ImmutableStats<Double> stats;

  public ProfilerStatsSample(String name, ImmutableStats<Double> stats) {
    this.name = name;
    this.stats = stats;
  }

  public ProfilerStatsSample(Map.Entry<String, ImmutableStats<Double>> entry) {
    this(entry.getKey(), entry.getValue());
  }

  public String getName() {
    return name;
  }

  public ImmutableStats<Double> getStats() {
    return stats;
  }
}
