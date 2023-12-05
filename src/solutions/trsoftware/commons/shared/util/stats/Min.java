package solutions.trsoftware.commons.shared.util.stats;

import java.util.Comparator;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Computes the minimum of a sequence of elements according to the given comparator.
 *
 * @author Alex
 * @since 10/1/2023
 */
public class Min<T> extends AbstractMinMax<T, Min<T>> {

  public Min(Comparator<T> comparator) {
    super(comparator);
  }

  @Override
  int getMultiplier() {
    return -1;
  }

  @Override
  public java.util.stream.Collector<T, ?, Min<T>> getCollector() {
    return new Collector<>(comparator);
  }

  /**
   * Provides a cached collector descriptor that can be passed to {@link Stream#collect}
   * to collect the stream elements into an instance of {@link Min}.
   *
   * @param <T> the input element type
   */
  public static class Collector<T> extends CollectableStats.Collector<T, Min<T>> {

    private final Comparator<T> comparator;

    public Collector(Comparator<T> comparator) {
      this.comparator = comparator;
    }

    @Override
    public Supplier<Min<T>> supplier() {
      return () -> new Min<>(comparator);
    }
  }
}
